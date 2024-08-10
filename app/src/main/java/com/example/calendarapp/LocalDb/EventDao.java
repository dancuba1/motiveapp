package com.example.calendarapp.LocalDb;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.TypeConverters;
import androidx.room.Update;

import com.example.calendarapp.Utils.DateTypeConverter;
import com.example.calendarapp.EventObjects.PrivateEvent;

import java.util.Date;
import java.util.List;

@Dao
@TypeConverters(DateTypeConverter.class)
public interface EventDao {
    String COLUMN_DATE = "date_column";
    @Insert
    void insert(PrivateEvent event);

    @Delete
    void delete(PrivateEvent event);

    @Update
    void update(PrivateEvent event);

    @Query("DELETE FROM event_table")
    void deleteAllEvents();

    @Query("DELETE FROM event_table WHERE eventId = :id")
    void deleteById(String id);

    @Query("SELECT * FROM event_table WHERE eventId = :id")
    LiveData<PrivateEvent> getById(String id);

    @Query("SELECT * FROM event_table WHERE uid= :uid ORDER BY startTime DESC")
    LiveData<List<PrivateEvent>> getAllEvents(String uid);

    @Query("SELECT * FROM event_table WHERE TRIM(SUBSTR(" + COLUMN_DATE + ", 1, 10)) = STRFTIME('%Y-%m-%d', :date) AND uid = :uid ORDER BY endTime ASC, startTime ASC")
    LiveData<List<PrivateEvent>> getEventsByDate(Date date, String uid);


    @Query("SELECT * FROM event_table WHERE date_column BETWEEN :startDay AND :endDay AND uid = :uid")
    LiveData<List<PrivateEvent>> getEventsForMonth(Date startDay, Date endDay, String uid);
}

