package com.example.calendarapp.Calendar;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calendarapp.EventObjects.PrivateEvent;
import com.example.calendarapp.LocalDb.EventViewModel;
import com.example.calendarapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;


//Similar to Calendar Adapter, however slightly different
public class CalendarPreviewAdapter extends RecyclerView.Adapter<CalendarPreviewAdapter.CalendarPreviewViewHolder>{

    private final ArrayList<String> daysOfMonth;
    private final CalendarAdapter.OnItemListener onItemListener;
    private final String monthYear;
    private List<PrivateEvent> monthlyEvents;
    private final Context context;
    private final Date date;
    private EventViewModel eventViewModel;
    private final Application application;
    private final LifecycleOwner lifecycleOwner;

    private PrivateEventPreviewAdapter privateEventPreviewAdapter;
    public CalendarPreviewAdapter(ArrayList<String> daysOfMonth, CalendarAdapter.OnItemListener onItemListener, String monthYear, Context context, Date date, Application application, LifecycleOwner lifecycleOwner){
        this.daysOfMonth = daysOfMonth;
        this.onItemListener = onItemListener;
        this.monthYear = monthYear;
        this.date = date;
        // needs application, lifecycleowner and context additionally
        this.application = application;
        this.lifecycleOwner = lifecycleOwner;
        this.context = context;

    }



    private Pair<Date, Date> getStartandEndDate(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date beforeDate = calendar.getTime();

        calendar.add(Calendar.DAY_OF_MONTH, 2);
        Date afterDate = calendar.getTime();

        return new Pair<>(beforeDate, afterDate);
    }

    @NonNull
    @Override
    public CalendarPreviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_calendar_preview, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int) (parent.getHeight());
        return new CalendarPreviewViewHolder(view, onItemListener);}


    @Override
    public void onBindViewHolder(@NonNull CalendarPreviewViewHolder holder, int position)
    {
        Log.d("CalendarAdapter", daysOfMonth.get(holder.getBindingAdapterPosition()));

        //set day text
        holder.dayOfMonth.setText(daysOfMonth.get(holder.getBindingAdapterPosition()));
        Pair<Date, Date> startAndEndDates = getStartandEndDate(date);
        if(startAndEndDates != null){
            Date startOfMonth = startAndEndDates.first;
            Date endOfMonth = startAndEndDates.second;
            Log.d("Are start and end dates found", "yes " + startOfMonth.toString(), null);

            //get current date
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startOfMonth);
            calendar.add(Calendar.DAY_OF_MONTH, holder.getBindingAdapterPosition());
            Date currentDate = calendar.getTime();

            //get events for day and set them as a recyclerview
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            eventViewModel = new EventViewModel(application);
            eventViewModel.getEventsByDate(currentDate, user.getUid()).observe(lifecycleOwner, privateEvents -> {
                for(int i=0;i<privateEvents.size();i++){
                    Log.d("Event found in month", privateEvents.get(i).toString(), null);
                }
                privateEventPreviewAdapter = new PrivateEventPreviewAdapter(privateEvents);
                holder.eventPreviewRecyclerView.setAdapter(privateEventPreviewAdapter);
                Animation animation = AnimationUtils.loadAnimation(application.getApplicationContext(), R.anim.float_in);
                holder.eventPreviewRecyclerView.startAnimation(animation);
            });

            holder.eventPreviewRecyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext(), LinearLayoutManager.VERTICAL, false));

        }


        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                longClick(v, date, holder.getBindingAdapterPosition());
                return true;
            }
        });
    }

    private void longClick(View v, Date date, int position) {
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
        transaction.commit();;
    }





    @Override
    public int getItemCount() {
        return daysOfMonth.size();
    }

    public void setMonthlyEvents(List<PrivateEvent> privateEvents) {
        this.monthlyEvents = privateEvents;
        notifyDataSetChanged(); // Notify the adapter to rebind all views with the new data
    }




    public class CalendarPreviewViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView dayOfMonth;
        private final CalendarAdapter.OnItemListener onItemListener;
        ImageView eventIndicator;
        RecyclerView eventPreviewRecyclerView;
        public CalendarPreviewViewHolder(@NonNull View itemView, CalendarAdapter.OnItemListener onItemListener)
        {
            super(itemView);
            eventIndicator = itemView.findViewById(R.id.eventIndicator);
            dayOfMonth = itemView.findViewById(R.id.calendarCell);
            eventPreviewRecyclerView = itemView.findViewById(R.id.eventPreviewRecyclerView);
            this.onItemListener = onItemListener;
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View view)
        {
            onItemListener.onItemClick(getAdapterPosition(), (String) dayOfMonth.getText());
        }
    }
}

