package com.rangetech.eventnotify.Fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.rangetech.eventnotify.Helpers.CheckExpiry;
import com.rangetech.eventnotify.Helpers.MyEventRecyclerAdapter;
import com.rangetech.eventnotify.Models.EventPost;
import com.rangetech.eventnotify.R;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
/**
 * A simple {@link Fragment} subclass.
 */
public class NotificationFragment extends Fragment {
    private static final String NOTIFICATION_FRAGMENT = "NOTIFICATION FRAGMENT";
    private RecyclerView event_list_view;
    private List<EventPost> event_list;
    private FirebaseFirestore firebaseFirestore;
    private MyEventRecyclerAdapter eventRecyclerAdapter;
    private FirebaseAuth firebaseAuth;
    private String currentUser;

    public NotificationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
         View view = inflater.inflate(R.layout.fragment_notification, container, false);
        event_list = new ArrayList<>();
        event_list_view = view.findViewById(R.id.event_list_view);
        firebaseAuth = FirebaseAuth.getInstance();
        eventRecyclerAdapter = new MyEventRecyclerAdapter(event_list);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(container.getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        event_list_view.setLayoutManager(linearLayoutManager);
        event_list_view.setAdapter(eventRecyclerAdapter);
        firebaseFirestore = FirebaseFirestore.getInstance();
         return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        getActivity().findViewById(R.id.event_location_fab).setVisibility(View.INVISIBLE);
        getActivity().findViewById(R.id.event_refresh_fab).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.event_refresh_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onResume();
            }
        });
    }




    @Override
    public void onResume()throws NullPointerException {
        super.onResume();
        event_list.clear();
        if(firebaseAuth.getCurrentUser()!=null) {
            getActivity().findViewById(R.id.location_progress).setVisibility(View.VISIBLE);
            currentUser=firebaseAuth.getUid();
            firebaseFirestore.collection("Users")
                    .document(currentUser)
                    .collection("MyEvents").
                    addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                           try {
                               for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                   if (doc.getType() == DocumentChange.Type.ADDED) {
                                       EventPost eventPost = doc.getDocument().toObject(EventPost.class);
                                        firebaseFirestore.collection("Posts")
                                               .document(eventPost.album_id)
                                               .get()
                                               .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                          @Override
                                                                          public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                              if (task.isSuccessful()) {
                                                                                  String evtndate=task.getResult().getString("event_date");
                                                                                  if(isExpired(evtndate)){
                                                                                      eventPost.setExpired("yes");
                                                                                  }else{
                                                                                      eventPost.setExpired("no");
                                                                                  }
                                                                                  event_list.add(eventPost);
                                                                                  eventRecyclerAdapter.notifyDataSetChanged();
                                                                                  getActivity().findViewById(R.id.location_progress).setVisibility(View.INVISIBLE);

                                                                              }
                                                                          }
                                                                      });


                                                }
                                        }
                           }catch (NullPointerException e1){
                               e1.printStackTrace();
                           }
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
