package com.example.calendarapp.LocalDb;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.example.calendarapp.EventObjects.PrivateEvent;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventRepository {
    private EventDao eventDao;
    private LiveData<List<PrivateEvent>> allEvents;
    private ExecutorService executor = Executors.newSingleThreadExecutor();


    public EventRepository(Application application){
        EventDatabase eventDatabase = EventDatabase.getInstance(application);
        eventDao = eventDatabase.eventDao();
    }

    public void insert(PrivateEvent event){
        new InsertEventAsyncTask(eventDao).execute(event);
    }
    public void delete(PrivateEvent event){
        new DeleteEventAsyncTask(eventDao).execute(event);

    }
    public void update(PrivateEvent event){
        new UpdateEventAsyncTask(eventDao).execute(event);

    }

    public LiveData<List<PrivateEvent>> getAllEvents(String uid){
        allEvents = eventDao.getAllEvents(uid);
        return allEvents;
    }

    public void deleteEntryById(String id) {
        executor.execute(() -> {
            eventDao.deleteById(id);
        });
    }

    public LiveData<List<PrivateEvent>> getEventsByDate(Date date, String uid){
        return eventDao.getEventsByDate(date, uid);
    }

    public LiveData<List<PrivateEvent>> getEventsForMonth(Date startDay, Date endDay, String uid) {
        return eventDao.getEventsForMonth(startDay, endDay, uid);
    }


    public static class InsertEventAsyncTask extends AsyncTask<PrivateEvent, Void, Void> {
        private EventDao eventDao;

        private InsertEventAsyncTask(EventDao eventDao){
            this.eventDao = eventDao;
        }
        @Override
        protected Void doInBackground(PrivateEvent... events) {
            eventDao.insert(events[0]);
            return null;
        }

    }

    private static class UpdateEventAsyncTask extends AsyncTask<PrivateEvent, Void, Void>{
        private EventDao eventDao;

        @Override
        protected Void doInBackground(PrivateEvent... events) {
            eventDao.update(events[0]);
            return null;
        }

        private UpdateEventAsyncTask(EventDao eventDao){
            this.eventDao =eventDao;
        }
    }

    private static class DeleteEventAsyncTask extends AsyncTask<PrivateEvent, Void, Void>{
        private EventDao eventDao;
        @Override
        protected Void doInBackground(PrivateEvent... events) {
            eventDao.delete(events[0]);
            return null;
        }

        private DeleteEventAsyncTask(EventDao eventDao){
            this.eventDao = eventDao;
        }
    }
}
