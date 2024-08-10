package com.example.calendarapp.Calendar;

import static android.app.PendingIntent.getActivity;

import static androidx.core.content.ContentProviderCompat.requireContext;

import static com.example.calendarapp.Calendar.CalendarFragment.monthYearFromDate;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calendarapp.EventObjects.PrivateEvent;
import com.example.calendarapp.R;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

//      Adapter for main calendar's recyclverview
public class CalendarAdapter extends RecyclerView.Adapter<CalendarViewHolder> {
    private final ArrayList<String> daysOfMonth;
    private final OnItemListener onItemListener;
    private final String monthYear;
    private List<PrivateEvent> monthlyEvents;
    private Context context;
    private boolean calendarPreview;
    private int itemHeight;



    public CalendarAdapter(ArrayList<String> daysOfMonth, OnItemListener onItemListener, String monthYear, Context context, RecyclerView recyclerView){
        this.daysOfMonth = shiftDaysOfWeek(daysOfMonth);
        this.onItemListener = onItemListener;
        this.monthYear = monthYear;
        this.monthlyEvents = new ArrayList<>();
        this.context = context;
        //determine the heigh of a calendar cell
        calculateItemHeight(recyclerView);
    }

    @Override
    public CalendarViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.calendar_cell_layout, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = 293;
        view.setLayoutParams(layoutParams);
        return new CalendarViewHolder(view, onItemListener);
    }

    private void calculateItemHeight(RecyclerView recyclerView) {
        // Ensure the RecyclerView is laid out before calculating dimensions
        recyclerView.post(() -> {
            int height = recyclerView.getHeight();
            itemHeight = (int) (height * 0.166666666);
            Log.d("Item height", String.valueOf(itemHeight));
            notifyDataSetChanged();  // Update the adapter once height is calculated
        });
    }




    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position)
    {
        Log.d("CalendarAdapter", daysOfMonth.get(position));
        holder.dayOfMonth.setText(daysOfMonth.get(position));

        try{
            //get the events for the current day
            ArrayList<PrivateEvent> eventsForDay = filterEventsForDay(monthlyEvents, Integer.parseInt(daysOfMonth.get(position)));
            for(int i=0;i<eventsForDay.size();i++){
                Log.d("Monthly Events in Adapter:", monthlyEvents.get(i).toString());
            }
            if (!eventsForDay.isEmpty()) {
                // if there are events, display them
                PrivateCalendarEventPreviewAdapter privateCalendarEventPreviewAdapter = new PrivateCalendarEventPreviewAdapter(eventsForDay);
                holder.eventPreviewRecyclerView.setAdapter(privateCalendarEventPreviewAdapter);

                Animation animation = AnimationUtils.loadAnimation(context, R.anim.float_in);
                holder.eventPreviewRecyclerView.startAnimation(animation);


            } else {
                //log to check for events
                Log.d("VisibilityChange", "No events for day, hiding event indicator at position: " + position);
            }
        }catch (Exception e){
            holder.eventIndicator.setVisibility(View.GONE); // Or View.INVISIBLE as appropriate

            Log.d("Exception", e.getMessage());
        }

        // set the event preview's layout
        holder.eventPreviewRecyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext(), LinearLayoutManager.VERTICAL, false));


        // set on long click, which opens addeditfragment with date selected loaded

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String selectedDate = daysOfMonth.get(position); // Get the selected date

                AddEditFragment addEditFragment = new AddEditFragment();

                Bundle bundle = new Bundle();
                bundle.putString("day", selectedDate);
                bundle.putString("monthYear", monthYear);
                addEditFragment.setArguments(bundle);

                Context context = v.getContext();

                Log.d("LongClick", "log click confirmed" + daysOfMonth.get(position) + monthYear, null);
                FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();


                FragmentTransaction transaction = fragmentManager.beginTransaction();

                transaction.setCustomAnimations(
                        R.anim.scale_up,
                        R.anim.scale_down,
                        R.anim.scale_up,
                        R.anim.scale_down
                );

                transaction.replace(R.id.fragmentContainerView, addEditFragment);

                transaction.addToBackStack(null);
                transaction.commit();
                return true;
            }
        });
    }




    private ArrayList<PrivateEvent> filterEventsForDay(List<PrivateEvent> monthlyEvents, int day) {
        ArrayList<PrivateEvent> filteredEvents = new ArrayList<>();

        //get event where date matches
        for (PrivateEvent event : monthlyEvents) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(event.getDate());
            int eventDay = calendar.get(Calendar.DAY_OF_MONTH);

            if (eventDay == day) {
                filteredEvents.add(event);
            }
        }

        return filteredEvents;
    }


    @Override
    public int getItemCount() {
        return daysOfMonth.size();
    }

    public void setMonthlyEvents(List<PrivateEvent> privateEvents) {
        this.monthlyEvents = privateEvents;
        notifyDataSetChanged(); // Notify the adapter to rebind all views with the new data
    }



    public interface  OnItemListener
    {
        void onItemClick(int position, String dayText);
    }

    //days of week start on sunday, so must be adjusted.
    private ArrayList<String> shiftDaysOfWeek(ArrayList<String> daysOfMonth) {
        // Shifting the days to start from Monday (index 1)
        int firstDayIndex = 1;
        ArrayList<String> shiftedDays = new ArrayList<>();

        for (int i = firstDayIndex; i < daysOfMonth.size(); i++) {
            shiftedDays.add(daysOfMonth.get(i));
        }

        for (int i = 0; i < firstDayIndex; i++) {
            shiftedDays.add(daysOfMonth.get(i));
        }

        return shiftedDays;
    }
}
