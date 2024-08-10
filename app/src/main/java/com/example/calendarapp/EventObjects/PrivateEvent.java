package com.example.calendarapp.EventObjects;

import androidx.annotation.NonNull;
import androidx.room.Entity;

import com.example.calendarapp.EventObjects.Event;
import com.example.calendarapp.EventObjects.EventType;

import java.time.LocalDateTime;
import java.util.Date;


//                  Private Event Object which is stored in local database
@Entity(tableName = "event_table")
public class PrivateEvent extends Event {

    private Boolean allDay;

    private boolean hasAlert;
    private LocalDateTime alertTime;

    public Boolean getAllDay() {
        return allDay;
    }

    public void setAllDay(Boolean allDay) {
        this.allDay = allDay;
    }

    public boolean isAlert() {
        return hasAlert;
    }

    public void setAlert(boolean alert) {
        this.hasAlert = alert;
    }

    public LocalDateTime getAlertTime() {
        return alertTime;
    }


    public void setHasAlert(boolean hasAlert) {
        this.hasAlert = hasAlert;
    }

    public void setAlertTime(LocalDateTime alertTime) {
        this.alertTime = alertTime;
    }

    public PrivateEvent(String title, String eventId, String uid, String location, Date date, boolean allDay, LocalDateTime startTime, LocalDateTime endTime, boolean hasAlert, LocalDateTime alertTime, EventType eventType, String description) {
        super(title, eventId, uid, location, date, startTime, endTime, eventType, description);
        this.hasAlert = hasAlert;
        this.alertTime = alertTime;
        this.allDay = allDay;
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString() + "PrivateEvent{" +
                "allDay=" + allDay +
                ", hasAlert=" + hasAlert +
                ", alertTime=" + alertTime +
                '}';
    }
}
