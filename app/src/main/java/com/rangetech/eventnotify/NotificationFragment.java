package com.rangetech.eventnotify;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

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
        getActivity().findViewById(R.id.event_location_fab).setVisibility(View.INVISIBLE);
        View view = inflater.inflate(R.layout.fragment_notification, container, false);
        event_list = new ArrayList<>();
        event_list_view = view.findViewById(R.id.event_list_view);
        firebaseAuth = FirebaseAuth.getInstance();
        eventRecyclerAdapter = new MyEventRecyclerAdapter(event_list);
        event_list_view.setLayoutManager(new LinearLayoutManager(container.getContext()));
        event_list_view.setAdapter(eventRecyclerAdapter);
        firebaseFirestore = FirebaseFirestore.getInstance();



        return view;


    }

    @Override
    public void onStart() {
        super.onStart();
    }


    @Override
    public void onPause() {
        super.onPause();
        Log.i(NOTIFICATION_FRAGMENT,"on Pause called");
    }

    @Override
    public void onResume() {
        super.onResume();

        event_list.clear();
        Log.i(NOTIFICATION_FRAGMENT,"on Resume called");
        if(firebaseAuth.getCurrentUser()!=null) {

            currentUser=firebaseAuth.getUid();
            firebaseFirestore.collection("Users")
                    .document(currentUser)
                    .collection("MyEvents").
                    addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    EventPost eventPost = doc.getDocument().toObject(EventPost.class);
                                    event_list.add(eventPost);
                                    eventRecyclerAdapter.notifyDataSetChanged();

                                }
                            }

                        }
                    });
        }

    }
}
