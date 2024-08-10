package com.example.calendarapp.Calendar;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calendarapp.EventObjects.PrivateEvent;
import com.example.calendarapp.R;
import com.google.android.material.card.MaterialCardView;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.List;

//          Adapter for the year view, showing the months of the year
public class MonthsInYearAdapter extends RecyclerView.Adapter<MonthsInYearAdapter.MonthsinYearViewHolder> {
    private ArrayList<String> months;
    private int year;
    private OnMonthClickListener listener;
    private Context context;

    @NonNull
    @Override
    public MonthsinYearViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_year_view, parent, false);
        ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
        layoutParams.height = (int) (parent.getHeight() * 0.133333);
        return new MonthsinYearViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MonthsinYearViewHolder holder, int position) {
        Log.d("month", months.get(position));

        Animation animation = AnimationUtils.loadAnimation(context, R.anim.float_in);
        holder.itemView.startAnimation(animation);
        holder.monthTV.setText(months.get(position));
        holder.itemView.setOnClickListener(v ->{
                if(listener!=null) {
                    // position is always added by 1, as index starts at 0
                    listener.onMonthClick(position + 1, year);
                }
        });
    }

    public void setMonthTexts(@NonNull ArrayList<String> months) {
        this.months = months;
        notifyDataSetChanged();
    }

    public void setYear(@NonNull int year) {
        this.year = year;
    }
    public void setContext(@NonNull Context context){
        this.context = context;
    }

    public interface OnMonthClickListener {
        void onMonthClick(int month, int year);
    }

    public void setOnMonthClickListener(OnMonthClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return months.size();
    }

    public class MonthsinYearViewHolder extends RecyclerView.ViewHolder{

        private MaterialCardView yearViewCard;
        private TextView monthTV;
        public MonthsinYearViewHolder(@NonNull View itemView) {
            super(itemView);
            yearViewCard = itemView.findViewById(R.id.monthCardView);
            monthTV = itemView.findViewById(R.id.monthTV);
        }
    }
}
