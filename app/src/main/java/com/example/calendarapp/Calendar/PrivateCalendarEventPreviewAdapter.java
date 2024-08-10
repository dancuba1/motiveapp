package com.example.calendarapp.Calendar;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calendarapp.EventObjects.PrivateEvent;
import com.example.calendarapp.R;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

//      Private Event Preview, for the Full Calendar

public class PrivateCalendarEventPreviewAdapter extends RecyclerView.Adapter<PrivateCalendarEventPreviewAdapter.ViewHolder> {

        private List<PrivateEvent> events;

        public PrivateCalendarEventPreviewAdapter(List<PrivateEvent> events) {
            this.events = events;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_day_preview, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            //sets event type colour
            if(events.get(position)!=null){
                PrivateEvent event = events.get(position);
                Log.d("Event Found in PrivateEventPreviewAdapter", event.toString() + String.valueOf(position));
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


            }


        }

        @Override
        public int getItemCount() {
            return events.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            MaterialCardView eventBackgroundCard;
            public ViewHolder(View itemView) {
                super(itemView);
                eventBackgroundCard = itemView.findViewById(R.id.eventBackgroundCard);
            }
        }
}


