package com.example.calendarapp.LocalDb;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.calendarapp.Utils.DateTypeConverter;
import com.example.calendarapp.Utils.LocalDateTimeConverter;
import com.example.calendarapp.EventObjects.PrivateEvent;

@Database(entities = {PrivateEvent.class}, version = 6)@TypeConverters({LocalDateTimeConverter.class, DateTypeConverter.class})
public abstract class EventDatabase extends RoomDatabase {
    private static EventDatabase instance;
    public abstract EventDao eventDao();

    public static synchronized EventDatabase getInstance(Context context){
        if (instance == null){
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    EventDatabase.class, "event_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
