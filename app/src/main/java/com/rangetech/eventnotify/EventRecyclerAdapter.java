package com.rangetech.eventnotify;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class EventRecyclerAdapter extends RecyclerView.Adapter<EventRecyclerAdapter.ViewHolder> {

    public List<EventPost> event_list;
    public Context context;
    public String my_lat,my_long;
    private FirebaseFirestore firebaseFirestore;

    public EventRecyclerAdapter(List<EventPost> event_list) {
        this.event_list = event_list;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_list_item, parent, false);
        context = parent.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        SharedPreferences sharedPreferences=context.getSharedPreferences("LOCATION.pref",Context.MODE_PRIVATE);
        my_lat=sharedPreferences.getString("LAT",000000+"");
        my_long=sharedPreferences.getString("LONG",00000+"");
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        try {
            final String desc_data = event_list.get(position).getTitle();
            holder.setTitleText(desc_data);
            final String image_url = event_list.get(position).getImage_url();
            holder.setEventImage(image_url);


            final String location_latitude = event_list.get(position).getLocation_lat();
            final String location_longitude = event_list.get(position).getLocation_long();


            Double loc_lat = Double.valueOf(location_latitude);
            Double loc_long = Double.valueOf(location_longitude);
            Double my_lati = Double.valueOf(my_lat);
            Double my_longi = Double.valueOf(my_long);

            LocationDistanceCalculator locationDistanceCalculator = new LocationDistanceCalculator();
            Double val = locationDistanceCalculator.getDistance(my_lati, my_longi, loc_lat, loc_long);
            val=val/1000;
            int value = (int) Math.round(val);
            String distance = value + "";

            final String location_details = event_list.get(position).getLocation_name();
            holder.setLocationText(location_details, distance);
            holder.setDetails(event_list.get(position).getDesc());


            String user_id = event_list.get(position).getUser_id();
            firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        String userName = task.getResult().getString("name");
                        String userImage = task.getResult().getString("image");
                        holder.setUserData(userName, userImage);
                    } else {

                    }
                }
            });
            String dateString = "";
            try {
                long millisecond = event_list.get(position).getTimestamp().getTime();
                dateString = new SimpleDateFormat("dd/MM/yyyy").format(new Date(millisecond));
                dateString = event_list.get(position).getEvent_date();
                holder.setTime(dateString);

            } catch (NullPointerException e) {
                e.printStackTrace();
            }

            final String finalDateString = dateString;
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent displayIntent = new Intent(context, EventDisplayActivity.class);
                    displayIntent.putExtra("EventName", desc_data);
                    displayIntent.putExtra("EventDetails", event_list.get(position).desc);
                    displayIntent.putExtra("EventCover", image_url);
                    displayIntent.putExtra("EventDate", finalDateString);
                    displayIntent.putExtra("AlbumId", event_list.get(position).getAlbum_id());
                    displayIntent.putExtra("UserID", event_list.get(position).getUser_id());
                    displayIntent.putExtra("EventLocation", location_details);
                    context.startActivity(displayIntent);
                }
            });
            holder.setParticipated(false);

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
        public void setParticipated(Boolean val){
            if(val){
                mView.findViewById(R.id.event_participated).setVisibility(View.VISIBLE);
            }else{
                mView.findViewById(R.id.event_participated).setVisibility(View.INVISIBLE);
            }

        }
        public void setLocationText(String locationName,String dist){
           textViewLocation=mView.findViewById(R.id.location);
           textViewLocation.setText(locationName+" "+dist+" km away");
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
        public void setUserData(String name , String image) {
            eventUserImage = mView.findViewById(R.id.event_user_image);
            eventUserName = mView.findViewById(R.id.event_user_name);
            eventUserName.setText(name);
            RequestOptions placeholderOption = new RequestOptions();
            placeholderOption.placeholder(R.drawable.ellipse);
            Glide.with(context).applyDefaultRequestOptions(placeholderOption).load(image).into(eventUserImage);
        }

        public void setDetails(String desc) {
            detailsText=mView.findViewById(R.id.desc);
            detailsText.setText(desc);
        }
    }
}
