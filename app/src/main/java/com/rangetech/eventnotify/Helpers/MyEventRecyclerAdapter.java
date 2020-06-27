package com.rangetech.eventnotify.Helpers;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rangetech.eventnotify.Activities.EventDisplayActivity;
import com.rangetech.eventnotify.Models.EventPost;
import com.rangetech.eventnotify.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyEventRecyclerAdapter extends RecyclerView.Adapter<MyEventRecyclerAdapter.ViewHolder> {

    private static final String MY_EVENTS = "My Events";
    public List<EventPost> event_list;
    public Context context;

    private FirebaseFirestore firebaseFirestore,firebaseFirestoreUsers;

    public MyEventRecyclerAdapter(List<EventPost> event_list) {
        this.event_list = event_list;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_list_item, parent, false);
        context = parent.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestoreUsers=FirebaseFirestore.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        try {
            String event_id=event_list.get(position).getAlbum_id();
            String participated=event_list.get(position).getParticipated();
            firebaseFirestore.collection("Posts")
                    .document(event_id)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                final String desc_data = task.getResult().getString("title");
                                final String image_url = task.getResult().getString("image_url");
                                final String user_id =  task.getResult().getString("user_id");
                                final String details=task.getResult().getString("desc");
                                final String locationName=task.getResult().getString("location_name");

                                firebaseFirestoreUsers.collection("Users").document(user_id).get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()){
                                                    String userName = task.getResult().getString("name");
                                                    String userImage = task.getResult().getString("image");
                                                    holder.setUserData(userName,userImage);
                                                } else {

                                                }
                                            }

                                        });

                                String dateString = "";
                                try {
                                    Date timestamp=task.getResult().getDate("timestamp");
                                    long millisecond=timestamp.getTime();
                                    dateString= new SimpleDateFormat("dd/MM/yyyy").format(new Date(millisecond));
                                    dateString=task.getResult().getString("event_date");
                                    holder.setTime(dateString);

                                    if(event_list.get(position).getExpired().contentEquals("yes")){
                                        holder.setExpired(false);
                                    }

                                }catch (NullPointerException e){
                                    e.printStackTrace();
                                }
                                final String finalDateString = dateString;
                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent displayIntent=new Intent(context, EventDisplayActivity.class);
                                        displayIntent.putExtra("EventName",desc_data);
                                        displayIntent.putExtra("EventDetails",details);
                                        displayIntent.putExtra("EventCover",image_url);
                                        displayIntent.putExtra("EventDate", finalDateString);
                                        displayIntent.putExtra("UserID",user_id);
                                        displayIntent.putExtra("AlbumId",event_list.get(position).getAlbum_id());
                                        displayIntent.putExtra("EventLocation",locationName);
                                        context.startActivity(displayIntent);
                                    }
                                });
                                holder.setTitleText(desc_data);
                                holder.setEventImage(image_url);
                                holder.setLocationText(locationName);
                                holder.setDetails(details);

                            }
                        }
                    });
            if(participated.contentEquals("yes")){
                holder.setParticipated(true);
            }else{
                holder.setParticipated(false);
            }
        }catch (NullPointerException e){
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return event_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;

        private TextView descView;
        private ImageView eventImageView;
        private TextView eventDate;
        private TextView eventUserName;
        private CircleImageView eventUserImage;
        private TextView textViewLocation;
        private TextView detailsText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }
        public void setTitleText(String descText) {
            descView = mView.findViewById(R.id.event_title);
            descView.setText(descText);
        }
        public void setEventImage(String downloadUri) {
            eventImageView = mView.findViewById(R.id.event_image);
            RequestOptions placeholderOption = new RequestOptions();
            placeholderOption.placeholder(R.drawable.rectangle);
            Glide.with(context).applyDefaultRequestOptions(placeholderOption).load(downloadUri).into(eventImageView);
        }
        public void setTime(String date) {
            eventDate = mView.findViewById(R.id.event_date);
            eventDate.setText(date);
        }
        public void setParticipated(Boolean val) {
            if (val) {
                mView.findViewById(R.id.event_participated).setVisibility(View.VISIBLE);
            } else {
                mView.findViewById(R.id.event_participated).setVisibility(View.INVISIBLE);
            }
        }

            public void setLocationText(String locationName){
            textViewLocation=mView.findViewById(R.id.location);
            textViewLocation.setText(locationName);
        }
        public void setUserData(String name , String image) {
            eventUserImage = mView.findViewById(R.id.event_user_image);
            eventUserName = mView.findViewById(R.id.event_user_name);
            eventUserName.setText(name);
            RequestOptions placeholderOption = new RequestOptions();
            placeholderOption.placeholder(R.drawable.ellipse);
            Glide.with(context).applyDefaultRequestOptions(placeholderOption).load(image).into(eventUserImage);
        }

        public void setExpired(boolean participated){
           if(!participated) {
               mView.findViewById(R.id.event_participated).setVisibility(View.VISIBLE);
               ImageView img=mView.findViewById(R.id.event_participated_img);
               RequestOptions placeholderOption = new RequestOptions();
               placeholderOption.placeholder(R.drawable.rectangle);
               Glide.with(context)
                       .applyDefaultRequestOptions(placeholderOption)
                       .load(R.drawable.ic_expired)
                       .into(img);
           }
        }

        public void setDetails(String details) {
            detailsText=mView.findViewById(R.id.desc);
            detailsText.setText(details);
        }
    }
}

