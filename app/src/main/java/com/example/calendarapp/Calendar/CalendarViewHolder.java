package com.example.calendarapp.Calendar;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calendarapp.Calendar.CalendarAdapter;
import com.example.calendarapp.R;

//Simple ViewHolder for the day of month, and recyclerview for events
public class CalendarViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public final TextView dayOfMonth;
    private final CalendarAdapter.OnItemListener onItemListener;
    ImageView eventIndicator;
    RecyclerView eventPreviewRecyclerView;
    public CalendarViewHolder(@NonNull View itemView, CalendarAdapter.OnItemListener onItemListener)
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