package com.example.calendarapp;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.calendarapp.EventObjects.PublicEvent;
import com.example.calendarapp.LocalDb.EventViewModel;
import com.example.calendarapp.Utils.LocalDateTimeConverter;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HomeEventsAdapter extends RecyclerView.Adapter<HomeEventsAdapter.HomeEventHolder>{
    @NonNull
    private List<PublicEvent> events = new ArrayList<>();
    private EventViewModel eventViewModel;
    private Context context;

    private ViewModelStoreOwner viewModelStoreOwner;

    private OnItemClickListener listener;

    public HomeEventsAdapter(Context context){
        this.context = context;
    }

    @Override
    public HomeEventHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_event, parent, false);
        return new HomeEventHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeEventHolder holder, int position) {
        PublicEvent currentEvent = events.get(position);

        Log.d("Event in adapter", currentEvent.toString(), null);


        // Lookup view for data population
        holder.titleTextView.setText(currentEvent.getTitle());
        Log.d("Location", currentEvent.getLocation(), null);
        holder.locationTextView.setText(currentEvent.getLocation());
        holder.dateTextView.setText(LocalDateTimeConverter.convertDateToFormattedString(currentEvent.getDate()));
        Log.d("price", currentEvent.getPrice());
        if(currentEvent.getPrice().equalsIgnoreCase("£0.00")){
            holder.priceTextView.setText("FREE");
        }else{
            holder.priceTextView.setText(currentEvent.getPrice());
        }
        holder.priceTextView.setText(currentEvent.getPrice());
        holder.startTimeTextView.setText(LocalDateTimeConverter.toTimeString(currentEvent.getStartTime()));
        holder.endTimeTextView.setText(LocalDateTimeConverter.toTimeString(currentEvent.getEndTime()));
        loadImageIntoView(context, currentEvent.getBannerUrl(), holder.bannerImageView);






    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public void setEvents(@NonNull List<PublicEvent> events) {
        this.events = events;
        notifyDataSetChanged();
    }

    class HomeEventHolder extends RecyclerView.ViewHolder{

        private MaterialCardView eventBackgroundCard;
        private TextView titleTextView;
        private TextView locationTextView;
        private TextView startTimeTextView;
        private TextView endTimeTextView;
        private TextView dateTextView;
        private TextView priceTextView;
        private ImageView bannerImageView;
        public HomeEventHolder(View itemView){
            super(itemView);
            eventBackgroundCard = itemView.findViewById(R.id.eventBackgroundCard);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            locationTextView = itemView.findViewById(R.id.locationTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            priceTextView = itemView.findViewById(R.id.priceTextView);
            startTimeTextView = itemView.findViewById(R.id.startTimeTextView);
            endTimeTextView = itemView.findViewById(R.id.endTimeTextView);
            bannerImageView = itemView.findViewById(R.id.bannerImageView);

            itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    int position = getAbsoluteAdapterPosition();
                    if (listener != null && position != RecyclerView.NO_POSITION){
                        listener.onItemClick(events.get(position));
                    }
                }
            });
        }
    }


    public void setViewModelStoreOwner(ViewModelStoreOwner owner) {
        this.viewModelStoreOwner = owner;
    }


    public interface OnItemClickListener{
        void onItemClick(PublicEvent event);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }

    private void loadImageIntoView(Context context, String imageUrl, ImageView imageView){
        Glide.with(context)
                .load(imageUrl)
                .into(imageView);
    }
}
