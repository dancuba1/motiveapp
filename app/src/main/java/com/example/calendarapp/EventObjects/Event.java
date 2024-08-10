package com.example.calendarapp.EventObjects;

import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.calendarapp.Utils.DateTypeConverter;

import java.time.LocalDateTime;
import java.util.Date;


//          Abstract Event object, used for the Private and Public Event objects
@TypeConverters(DateTypeConverter.class)
public abstract class Event {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String uid;
    private String title;
    private String location;

    @ColumnInfo(name = "date_column")
    private Date date;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private String description;
    private EventType eventType;
    private String eventId;


    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public Event(String title, String eventId, String uid, String location, Date date, LocalDateTime startTime, LocalDateTime endTime, EventType eventType, String description) {
        this.title = title;
        this.eventId = eventId;
        this.uid = uid;
        this.location = location;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.eventType = eventType;
        this.description = description;
    }

    public Event() {

    }


    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", eventId=" + eventId + '\'' +
                ", uid='" + uid + '\'' +
                ", location='" + location + '\'' +
                ", date='" + date + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", description='" + description + '\'' +
                ", eventType=" + eventType +
                '}';
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }



    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }



    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String descriptions) {
        this.description = descriptions;
    }


}
