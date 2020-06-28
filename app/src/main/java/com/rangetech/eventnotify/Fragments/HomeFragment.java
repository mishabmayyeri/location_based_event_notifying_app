package com.rangetech.eventnotify.Fragments;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.crowdfire.cfalertdialog.CFAlertDialog;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.rangetech.eventnotify.Helpers.CheckExpiry;
import com.rangetech.eventnotify.Helpers.EventRecyclerAdapter;
import com.rangetech.eventnotify.Helpers.LocationDistanceCalculator;
import com.rangetech.eventnotify.Models.EventPost;
import com.rangetech.eventnotify.R;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static androidx.core.content.ContextCompat.checkSelfPermission;


public class HomeFragment extends Fragment  {

    private static final String LOCATION_TAG = "location";
    private static final int RADIUS = 30;
    private RecyclerView event_list_view;
    private List<EventPost> event_list;
    private FirebaseFirestore firebaseFirestore;
    private EventRecyclerAdapter eventRecyclerAdapter;
    private FirebaseAuth firebaseAuth;
    private Query firstQuery;
    private ViewGroup container;
    private LocationDistanceCalculator locationDistanceCalculator;
    private FusedLocationProviderClient client;

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        event_list = new ArrayList<>();
        event_list_view = view.findViewById(R.id.event_list_view);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        locationDistanceCalculator = new LocationDistanceCalculator();
        eventRecyclerAdapter = new EventRecyclerAdapter(event_list);
        this.container = container;
        event_list_view.setLayoutManager(new LinearLayoutManager(container.getContext()));
        event_list_view.setAdapter(eventRecyclerAdapter);
        firstQuery = firebaseFirestore.collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING);

        getActivity().findViewById(R.id.event_location_fab).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.event_refresh_fab).setVisibility(View.INVISIBLE);
        getActivity().findViewById(R.id.event_location_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStart();
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (checkSelfPermission(getContext(), ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getActivity().findViewById(R.id.location_progress).setVisibility(View.VISIBLE);
            //proceed(location.getLatitude(),location.getLongitude());
            client=new FusedLocationProviderClient(getActivity());
            client.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if(task.isSuccessful()){
                        if(task.getResult()==null){
                            CFAlertDialog.Builder builder = new CFAlertDialog.Builder(getActivity(), R.style.AppTheme)
                                    .setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET)
                                    .setTitle("No Location found. ")
                                    .setIcon(R.drawable.ic_warning_black_24dp)
                                    .setMessage("Please Turn ON Location and open app again.")
                                    .setCancelable(false)
                                    .addButton("Turn ON Location ", -1, Color.parseColor("#3e3d63"), CFAlertDialog.CFAlertActionStyle.POSITIVE,
                                            CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(final DialogInterface dialog, int which) {
                                                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                                    startActivity(intent);
                                                    getActivity().finishAffinity();
                                                }
                                            }).addButton("    CANCEL   ", Color.parseColor("#3e3d63"), Color.parseColor("#e0e0e0"), CFAlertDialog.CFAlertActionStyle.DEFAULT,
                                            CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            });
                            builder.show();

                        }else{
                            proceed(task.getResult().getLatitude(),task.getResult().getLongitude());
                        }
                    }
                }
            });
        }
        else
        {
            PermissionListener permissionlistener = new PermissionListener() {
                @Override
                public void onPermissionGranted() {
                    onStart();
                }

                @Override
                public void onPermissionDenied(List<String> deniedPermissions) {
                    Toast.makeText(getActivity(), "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
                }


            };
            TedPermission.with(getActivity())
                    .setPermissionListener(permissionlistener)
                    .setDeniedMessage("If you reject the permission we can't access your location to get fetch nearby events.\n\nPlease turn on permissions at [Setting] > [Permission]")
                    .setPermissions(ACCESS_FINE_LOCATION)
                    .check();
        }

    }


    private void proceed(double latitude, double longitude) {
        createRecyclerView(latitude,longitude);
    }

    private void createRecyclerView(double latitude, double longitude) {
        if (firebaseAuth.getCurrentUser() != null) {
            firstQuery.addSnapshotListener((queryDocumentSnapshots, e) -> {
                try {
                    event_list.clear();
                    for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {
                            EventPost eventPost = doc.getDocument().toObject(EventPost.class);
                            double d = locationDistanceCalculator.getDistance(latitude,
                                    longitude,
                                    Double.parseDouble(eventPost.getLocation_lat()),
                                    Double.parseDouble(eventPost.getLocation_long()));
                            d = d / 1000;
                            int value = (int) Math.round(d);
                            if (value <= RADIUS) {
                                if(isExpired(eventPost.event_date)){
                                    eventPost.setExpired("yes");
                                }else{
                                    eventPost.setExpired("no");
                                }
                                String loc = eventPost.getLocation_name();
                                loc = loc + " " + value + "";
                                eventPost.setLocation_name(loc);
                                event_list.add(eventPost);
                            }
                            eventRecyclerAdapter.notifyDataSetChanged();
                            getActivity().findViewById(R.id.location_progress).setVisibility(View.INVISIBLE);
                        }
                    }
                } catch (NullPointerException e1) {
                    e1.printStackTrace();

                }
            });
        }
    }

    private boolean isExpired(String event_date) {
        CheckExpiry checkExpiry=new CheckExpiry(event_date,0);
        try {
            if(checkExpiry.isExpired())
                return true;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }








}


