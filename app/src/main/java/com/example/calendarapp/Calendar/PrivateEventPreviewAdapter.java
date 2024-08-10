package com.example.calendarapp.Calendar;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calendarapp.EventObjects.PrivateEvent;
import com.example.calendarapp.R;
import com.example.calendarapp.Utils.LocalDateTimeConverter;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class PrivateEventPreviewAdapter extends RecyclerView.Adapter<PrivateEventPreviewAdapter.EventPreviewHolder> {

    private final List<PrivateEvent> events;

    public PrivateEventPreviewAdapter(List<PrivateEvent> events) {
        this.events = events;
    }

    @NonNull
    @Override
    public EventPreviewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event_preview, parent, false);
        return new EventPreviewHolder(view);
    }

    @Override
    public void onBindViewHolder(EventPreviewHolder holder, int position) {
        //set the background colour and start and end time
        if(events.get(position)!=null){
            PrivateEvent event = events.get(position);
            Log.d("Event Found in PrivateEventPreviewAdapter", event.toString() + position);
            switch (event.getEventType()){
                case WORK:
                    holder.eventBackgroundCard.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.workEventColor));
                    break;
                case SOCIAL:
                    holder.eventBackgroundCard.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.socialEventColor));
                    break;
                case PERSONAL:
                    holder.eventBackgroundCard.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.personalEventColor));
                    break;
                default:
                    Log.d("No event types found", event.getEventType().toString());
                    break;
            }

            holder.startTimeTextView.setText(LocalDateTimeConverter.toTimeString(event.getStartTime()));
            holder.endTimeTextView.setText(LocalDateTimeConverter.toTimeString(event.getEndTime()));

        }


    }

    @Override
    public int getItemCount() {
        return events.size();
    }


    public class EventPreviewHolder extends RecyclerView.ViewHolder {

        MaterialCardView eventBackgroundCard;
        TextView startTimeTextView, endTimeTextView;
        public EventPreviewHolder(View itemView) {
            super(itemView);
            eventBackgroundCard = itemView.findViewById(R.id.eventBackgroundCard);
            startTimeTextView = itemView.findViewById(R.id.startTimeTextView);
            endTimeTextView = itemView.findViewById(R.id.endTimeTextView);
        }
    }
}


