package com.example.calendarapp.Utils;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

public class DayOfWeekComparator implements Comparator<Date> {
    public static Date compare(Date date, int desiredDay) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        // Get the day of the week for the provided date
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int difference;

        if(desiredDay == 1){
            desiredDay = 8;
            difference = desiredDay - dayOfWeek;
        } else if (dayOfWeek == 1) {
            dayOfWeek = 8;
            difference = desiredDay - dayOfWeek;

        } else{
            difference = desiredDay - dayOfWeek;
        }



        // Add the difference in days to the provided date
        calendar.add(Calendar.DAY_OF_MONTH, difference);

        return calendar.getTime();
    }

    @Override
    public int compare(Date o1, Date o2) {
        return 0;
    }
}
