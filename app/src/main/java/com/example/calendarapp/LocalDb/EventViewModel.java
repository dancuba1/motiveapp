package com.example.calendarapp.LocalDb;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.calendarapp.EventObjects.PrivateEvent;

import java.util.Date;
import java.util.List;

public class EventViewModel extends AndroidViewModel {
    private EventRepository repository;
    private LiveData<List<PrivateEvent>> allEvents;
    public EventViewModel(@NonNull Application application) {
        super(application);
        repository = new EventRepository(application);
    }
    public void insert(PrivateEvent event){
        repository.insert(event);
    }

    public void update(PrivateEvent event){
        repository.update(event);
    }

    public void delete(PrivateEvent event){
        repository.delete(event);
    }

    public void deleteEventById(String id){
        repository.deleteEntryById(id);
    }


    public LiveData<List<PrivateEvent>> getAllEvents(String uid){
        allEvents = repository.getAllEvents(uid);
        return allEvents;
    }

    public LiveData<List<PrivateEvent>> getEventsByDate(Date date, String uid){
        return repository.getEventsByDate(date, uid);
    }

    public LiveData<List<PrivateEvent>> getEventsForMonth(Date startDay, Date endDay, String uid){
        return repository.getEventsForMonth(startDay, endDay, uid);
    }
}
