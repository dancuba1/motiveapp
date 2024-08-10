package com.example.calendarapp.Calendar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calendarapp.LocalDb.EventViewModel;
import com.example.calendarapp.Utils.LocalDateTimeConverter;
import com.example.calendarapp.EventObjects.PrivateEvent;
import com.example.calendarapp.R;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

// Adapter for displaying events in day view
public class DayViewAdapter extends RecyclerView.Adapter<DayViewAdapter.EventHolder>{
    @NonNull
    private List<PrivateEvent> events = new ArrayList<>();
    private EventViewModel eventViewModel;
    private Context context;

    private ViewModelStoreOwner viewModelStoreOwner;

    private OnItemClickListener listener;

    public DayViewAdapter(Context context){
        this.context = context;
    }

    @Override
    public EventHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EventHolder holder, int position) {

        //gets current event in list
        PrivateEvent currentEvent = events.get(position);

        Log.d("Event in adapter", currentEvent.toString(), null);



        // Sets event details
        holder.titleTextView.setText(currentEvent.getTitle());
        holder.locationTextView.setText(currentEvent.getLocation());
        holder.startTimeTextView.setText(LocalDateTimeConverter.toTimeString(currentEvent.getStartTime()));
        holder.endTimeTextView.setText(LocalDateTimeConverter.toTimeString(currentEvent.getEndTime()));
        holder.descriptionTextView.setText(currentEvent.getDescription());



        // Sets event background colours, dependant on event type
        int backgroundTint = 0;
        switch (currentEvent.getEventType()) {
            case SOCIAL:
                backgroundTint = ContextCompat.getColor(context, R.color.socialEventColor); // Replace with your color resource
                break;
            case WORK:
                backgroundTint = ContextCompat.getColor(context, R.color.workEventColor); // Replace with your color resource
                break;
            case PERSONAL:
                backgroundTint = ContextCompat.getColor(context, R.color.personalEventColor); // Replace with your color resource
                break;
            default:
                break;
        }

        holder.eventBackgroundCard.setCardBackgroundColor(backgroundTint);

        //set delete button listener
        holder.deleteEventBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPromptDialog(currentEvent);
            }
        });





    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public void setEvents(@NonNull List<PrivateEvent> events) {
        this.events = events;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    //Simple ViewHolder for event details and delete button
    class EventHolder extends RecyclerView.ViewHolder{

        private MaterialCardView eventBackgroundCard;
        private TextView titleTextView;
        private TextView locationTextView;
        private TextView startTimeTextView;
        private TextView endTimeTextView;
        private ImageView deleteEventBtn;
        private TextView descriptionTextView;
        public EventHolder(View itemView){
            super(itemView);
            eventBackgroundCard = itemView.findViewById(R.id.eventBackgroundCard);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            locationTextView = itemView.findViewById(R.id.locationTextView);
            startTimeTextView = itemView.findViewById(R.id.startTimeTextView);
            endTimeTextView = itemView.findViewById(R.id.endTimeTextView);
            deleteEventBtn = itemView.findViewById(R.id.deleteEventBtn);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);

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

    //Delete confirmation popup
    private void showPromptDialog(PrivateEvent event) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Deletion confirmation")
                .setMessage("Are you sure you would like to delete?")
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked Confirm
                        deleteEventFromDatabase(event);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })
                .show();

    }

    //must set viewmodelstoreowner, to delete events using ViewModel
    public void setViewModelStoreOwner(ViewModelStoreOwner owner) {
        this.viewModelStoreOwner = owner;
    }


    private void deleteEventFromDatabase(PrivateEvent event) {
        Log.d("DeleteEvent", "confirmedDelete", null);

        if (viewModelStoreOwner != null) {
            EventViewModel eventViewModel = new ViewModelProvider(viewModelStoreOwner).get(EventViewModel.class);
            eventViewModel.delete(event);
        } else {
            Log.e("DeleteEvent", "ViewModelStoreOwner is null", null);
        }
    }

    public interface OnItemClickListener{
        void onItemClick(PrivateEvent event);
     }



}
