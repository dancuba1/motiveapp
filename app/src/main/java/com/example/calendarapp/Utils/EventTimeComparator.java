package com.example.calendarapp.Utils;

import com.example.calendarapp.EventObjects.Event;

import java.util.Comparator;

public class EventTimeComparator implements Comparator<Event> {
    @Override
    public int compare(Event event1, Event event2) {
        int startTimeComparison = event1.getStartTime().compareTo(event2.getStartTime());

        if (startTimeComparison != 0) {
            // Events have different start times, use the start time for comparison
            return startTimeComparison;
        } else {
            // Events have the same start time, compare by end time
            return event1.getEndTime().compareTo(event2.getEndTime());
        }
    }
}

