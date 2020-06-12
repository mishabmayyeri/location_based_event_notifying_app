package com.rangetech.eventnotify;

import android.content.Context;
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

    private FirebaseFirestore firebaseFirestore;

    public EventRecyclerAdapter(List<EventPost> event_list) {
        this.event_list = event_list;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_list_item, parent, false);
        context = parent.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        String desc_data = event_list.get(position).getDesc();
        holder.setDescText(desc_data);
        String image_url = event_list.get(position).getImage_url();
        holder.setEventImage(image_url);

        String user_id =  event_list.get(position).getUser_id();
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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

        long millisecond = event_list.get(position).getTimestamp().getTime();
        String dateString = new SimpleDateFormat("dd/MM/yyyy").format(new Date(millisecond));
        holder.setTime(dateString);
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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }
        public void setDescText(String descText) {
            descView = mView.findViewById(R.id.event_desc);
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
        public void setUserData(String name , String image) {
            eventUserImage = mView.findViewById(R.id.event_user_image);
            eventUserName = mView.findViewById(R.id.event_user_name);
            eventUserName.setText(name);
            RequestOptions placeholderOption = new RequestOptions();
            placeholderOption.placeholder(R.drawable.ellipse);
            Glide.with(context).applyDefaultRequestOptions(placeholderOption).load(image).into(eventUserImage);
        }
    }
}
