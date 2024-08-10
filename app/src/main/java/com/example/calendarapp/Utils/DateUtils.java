package com.example.calendarapp.Utils;

import androidx.core.util.Pair;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {
    public static Pair<Date, Date> getStartAndEndDateOfMonth(String monthYearStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy");
        try {
            Date date = sdf.parse(monthYearStr);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            // Set to the first day of the month
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
            Date startOfMonth = calendar.getTime();

            // Set to the last day of the month
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            Date endOfMonth = calendar.getTime();

            return new Pair<>(startOfMonth, endOfMonth);
        } catch (ParseException e) {
            e.printStackTrace();
            return null; // or handle the error as appropriate
        }
    }


    public static Date getDateWithDay(Date inputDate, String dayString) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(inputDate);

        int day = Integer.parseInt(dayString);
        int inputDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int lastDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Check if input date is the last day of the month and dayString is "1"
        if (inputDayOfMonth == lastDayOfMonth && day == 1) {
            // Move to the first day of the next month
            calendar.add(Calendar.MONTH, 1);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
        } else {
            // Set the day of the month, keeping the same month and year
            calendar.set(Calendar.DAY_OF_MONTH, day);
        }

        return calendar.getTime();
    }
}
