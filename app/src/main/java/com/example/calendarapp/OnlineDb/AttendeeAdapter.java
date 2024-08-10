package com.example.calendarapp.OnlineDb;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.calendarapp.R;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

public class AttendeeAdapter extends RecyclerView.Adapter<AttendeeAdapter.AttendeeHolder> {

    private ArrayList<AttendeePreview> attendeePreviews;
    private OnItemClickListener listener;
    private Context context;

    public AttendeeAdapter(Context context, ArrayList<AttendeePreview> attendeePreviews) {
        this.attendeePreviews = attendeePreviews;
        this.context = context;
    }

    @NonNull
    @Override
    public AttendeeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.attendee_item, parent, false);
        return new AttendeeHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendeeHolder holder, int position) {
        AttendeePreview attendeePreview = attendeePreviews.get(position);
        if(attendeePreview!=null){
            Log.d("AttendeeAdapter", attendeePreview.toString());
            if(attendeePreview.getProfilePicUrl()!=null){
                Glide.with(context).load(attendeePreview.getProfilePicUrl()).apply(RequestOptions.circleCropTransform()).into(holder.profilePic);
            }else{
                Glide.with(context).load(R.drawable.circle_background).apply(RequestOptions.circleCropTransform()).into(holder.profilePic);
            }
            holder.usernameTextView.setText(attendeePreview.getAttendeeName());
        }else{
            Log.d("AttendeeAdapter", "no attendee");
        }

    }

    public void setContext(Context context){
        this.context = context;
    }
    public void setAttendees(ArrayList<AttendeePreview> attendeePreviews){
        this.attendeePreviews = attendeePreviews;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return attendeePreviews.size();
    }
    class AttendeeHolder extends RecyclerView.ViewHolder{

        private MaterialCardView attendeeBackgroundCard;
        private TextView usernameTextView;
        private ImageView profilePic;
        public AttendeeHolder(View itemView){
            super(itemView);
            attendeeBackgroundCard = itemView.findViewById(R.id.attendeeCardView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            profilePic = itemView.findViewById(R.id.attendeeProfilePic);

            itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    int position = getAbsoluteAdapterPosition();
                    if (listener != null && position != RecyclerView.NO_POSITION){
                        listener.onItemClick(attendeePreviews.get(position));
                    }
                }
            });
        }
    }
    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }

    public interface OnItemClickListener{
        void onItemClick(AttendeePreview attendee);
    }

}
