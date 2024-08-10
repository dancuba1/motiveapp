package com.example.calendarapp.OnlineDb;


import android.util.Log;

import androidx.annotation.NonNull;

import com.example.calendarapp.EventObjects.EventGenre;
import com.example.calendarapp.EventObjects.EventType;
import com.example.calendarapp.EventObjects.PrivateEvent;
import com.example.calendarapp.EventObjects.PublicEvent;
import com.example.calendarapp.HomeFragment;
import com.example.calendarapp.ProfileFragment;
import com.example.calendarapp.Utils.TimestampLocalDateTimeConverter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PublicEventRepository {

    public static ArrayList<PublicEvent> getAllEvents(HomeFragment.EventsCallback userEventsCallback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        ArrayList<PublicEvent> allEvents = new ArrayList<>();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();String userId = user.getUid();
        db.collection("PublicEvents")
                .limit(100)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            PublicEvent event = getPublicEventFromDocument(document);
                            event.setEventId(document.getId());
                            Log.d("All events found", event.toString(), null);
                            Log.d("Is event id found", event.toString(), null);
                            allEvents.add(event);
                        }
                        userEventsCallback.onCallback(allEvents);
                    } else {
                        Log.d("No events found", "no documents found", null);

                    }
                });
        if (allEvents != null) {
            for (int i = 0; i < allEvents.size(); i++) {
                Log.d("All Events", allEvents.get(i).toString(), null);
            }
        }
        return allEvents;
    }
    public static PublicEvent convertDocumentToEvent(QueryDocumentSnapshot document) {
        String title = document.getString("title");
        Log.d("convertToEvent", title, null);
        String location = document.getString("location");
        String link = document.getString("link");
        String price = document.getString("price");
        String description = document.getString("description");
        String bannerUrl = document.getString("bannerImage");
        LocalDateTime startTime = TimestampLocalDateTimeConverter.convertTimestampToLocalDateTime((Timestamp) document.get("startTime"));
        LocalDateTime endTime = TimestampLocalDateTimeConverter.convertTimestampToLocalDateTime((Timestamp) document.get("endTime"));
        Date date = ((Timestamp) document.get("date")).toDate();
        String eventGenreString = (String) document.get("eventGenre");
        EventGenre eventGenre = EventGenre.valueOf(eventGenreString);
        String uid = document.getString("creatorId");
        ArrayList<String> images = new ArrayList<>();
        if (document.contains("images")) {
            List<String> firestoreList = (List<String>) document.get("images");
            if (firestoreList != null && !firestoreList.isEmpty()) {
                images.addAll(firestoreList);
            }
        }
        ArrayList<String> tags = new ArrayList<>();
        if(document.contains("tags")){
            List<String> firestoreList = (List<String>) document.get("tags");
            if (firestoreList != null && !firestoreList.isEmpty()) {
                tags.addAll(firestoreList);
            }
        }
        ArrayList<String> attendees = new ArrayList<>();
        if(document.contains("attendees")){
            List<String> firestoreList = (List<String>) document.get("attendees");
            if (firestoreList != null && !firestoreList.isEmpty()) {
                attendees.addAll(firestoreList);
            }
        }
        String eventId = document.getId();
        Log.d("PublicEvent:", title + eventId + location + link + price + description + bannerUrl + uid, null);
        return new PublicEvent(title, eventId, uid, location, date, startTime, endTime, EventType.SOCIAL, eventGenre, description, price, link, bannerUrl, images, tags, attendees);
    }

    public static void getEventById(String eventId, EventFetchListener eventFetchListener) {
        Log.d("In getEventById", "at start", null);

        FirebaseFirestore.getInstance().collection("PublicEvents").document(eventId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                PublicEvent event = getPublicEventFromDocument(document);
                                event.setEventId(document.getId());
                                Log.d("Event Fetch", "DocumentSnapshot data: " + document.getData());
                                try {
                                    eventFetchListener.onEventFetched(event);
                                } catch (ParseException e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                Log.d("Event Fetch", "No such document");
                            }
                        } else {
                            Log.d("Event Fetch", "get failed with ", task.getException());
                        }
                    }
                });
    }

    public static void getFollowingEvents(HomeFragment.EventsCallback userEventsCallback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        ArrayList<PublicEvent> userEvents = new ArrayList<>();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        String userId = user.getUid();

        // First, get the list of user IDs the current user is following
        db.collection("Following").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> followedUserIds = (List<String>) documentSnapshot.get("followedUsers");
                if (followedUserIds == null || followedUserIds.isEmpty()) {
                    userEventsCallback.onCallback(userEvents); // No followed users, return empty list
                    return;
                }

                // Now fetch public events where the creatorId is in the list of followedUserIds
                db.collection("PublicEvents")
                        .whereIn("creatorId", followedUserIds)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    PublicEvent event = getPublicEventFromDocument(document);
                                    event.setEventId(document.getId());
                                    userEvents.add(event);
                                }
                                userEventsCallback.onCallback(userEvents);
                            } else {
                                Log.d("No user events found", "Error getting documents: ", task.getException());
                                userEventsCallback.onCallback(userEvents); // Return potentially partial results in case of error
                            }
                        });
            } else {
                Log.d("Following", "No following document found for user");
                userEventsCallback.onCallback(userEvents); // No following document, return empty list
            }
        }).addOnFailureListener(e -> {
            Log.d("Following", "Failed to fetch following document", e);
            userEventsCallback.onCallback(userEvents); // In case of failure, return empty list
        });
    }

    public static void parseJSONToEvents(JSONObject jsonObject, EventSearchCallback callback) throws JSONException {
        JSONArray hits = jsonObject.getJSONArray("hits");

        ArrayList<PublicEvent> events = new ArrayList<>();
        for (int i = 0; i < hits.length(); i++) {
            JSONObject hit = hits.getJSONObject(i);
            Log.d("Json object", "hit " + hit.toString());
            PublicEvent event = convertHitToPublicEvent(hit);
            Log.d("Json event", event.toString());
            events.add(event);
        }
        callback.onEventsFound(events);
    }

    private static PublicEvent convertHitToPublicEvent(JSONObject hit) {
        try {
            String title = hit.optString("title"); // Using optString to avoid JSONException
            String location = hit.optString("location");
            String description = hit.optString("description");
            String link = hit.optString("link");
            String bannerUrl = hit.optString("bannerImage");
            String uid = hit.optString("creatorId");
            String genreStr = hit.optString("eventGenre");
            EventGenre genre = EventGenre.valueOf(genreStr);
            String typeStr = hit.optString("eventType");
            EventType eventType = EventType.SOCIAL;
            if(!typeStr.isEmpty()){
                eventType = EventType.valueOf(typeStr);
            }

            long startTimeStamp = (long) hit.optDouble("startTime");
            LocalDateTime startTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(startTimeStamp), ZoneId.systemDefault());
            long endTimeStamp = (long) hit.optDouble("endTime");
            LocalDateTime endTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(endTimeStamp), ZoneId.systemDefault());

            long dateTimeStamp = (long) hit.optDouble("date");
            Date date = new Date(dateTimeStamp);

            String eventId = hit.optString("eventId");
            Log.d("Json event", "object id " + eventId);

            JSONArray tagsArray = null;
            try {
                tagsArray = hit.getJSONArray("tags");
            } catch (JSONException e) {
                Log.d("Failed event creation", "tags array", e);
            }
            ArrayList<String> tags = new ArrayList<>();
            if(tagsArray!=null){
                for (int i = 0; i < tagsArray.length(); i++) {
                    tags.add(tagsArray.getString(i));
                }
            }


            JSONArray attendeesArray = null;
            try {
                attendeesArray = hit.getJSONArray("attendees");
            } catch (JSONException e) {
                Log.d("Failed event creation", "attendee array", e);
            }
            ArrayList<String> attendees = new ArrayList<>();
            if (attendeesArray!=null){
                for (int i = 0; i < attendeesArray.length(); i++) {
                    attendees.add(attendeesArray.getString(i));
                }
            }


            JSONArray imagesArray = null;
            try {
                imagesArray = hit.getJSONArray("images");
            } catch (JSONException e) {
                Log.d("Failed event creation", "image array", e);
            }
            ArrayList<String> imageUrls = new ArrayList<>();
            if(imagesArray!=null){
                for (int i = 0; i < imagesArray.length(); i++) {
                    imageUrls.add(attendeesArray.getString(i));
                }
            }

            String price = hit.optString("price");


            PublicEvent publicEvent = new PublicEvent(title, null, uid, location, date, startTime, endTime, eventType, genre, description, price, link, bannerUrl, imageUrls, tags, attendees);
            publicEvent.setEventId(eventId);
            return publicEvent;

        }catch (Exception e){
            Log.d("Failed event creation", "failed", e);
            return null;
        }
    }
    public static PublicEvent getPublicEventFromDocument(DocumentSnapshot document) {
        Log.d("In getPublicEventFromDocument", "at start", null);

        String title = document.getString("title");
        String location = document.getString("location");
        String description = document.getString("description");
        String link = document.getString("link");
        String bannerUrl = document.getString("bannerImage");
        String price = document.getString("price");
        String uid = document.getString("creatorId");
        String genreStr = document.getString("eventGenre");
        EventGenre genre = EventGenre.valueOf(genreStr);
        String typeStr = document.getString("eventType");
        EventType eventType = EventType.SOCIAL;
        if(typeStr!=null){
            Log.d("TypeString", typeStr);
            eventType = EventType.valueOf(typeStr);
        }
        Timestamp startTimeStamp = document.getTimestamp("startTime");
        Timestamp endTimeStamp = document.getTimestamp("endTime");
        Timestamp dateTimeStamp = document.getTimestamp("date");

        LocalDateTime startTime = null;
        LocalDateTime endTime = null;
        Date date = null;
        if (startTimeStamp != null && endTimeStamp != null) {
            startTime = (startTimeStamp.toDate().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDateTime());
            endTime = (endTimeStamp.toDate().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDateTime());
            date = (dateTimeStamp.toDate());
        }
        String eventId = document.getId();
        Log.d("EventId", eventId);

        ArrayList<String> imageUrls = null;
        if(document.get("images")!=null){
            List<String> imageUrlsList = (List<String>) document.get("images");
            imageUrls = new ArrayList<>(imageUrlsList);
        }
        ArrayList<String> tags = null;
        if(document.get("tags")!=null){
            List<String> imageUrlsList = (List<String>) document.get("tags");
            tags = new ArrayList<>(imageUrlsList);
        }
        ArrayList<String> attendees = null;
        if(document.get("attendees")!=null){
            List<String> imageUrlsList = (List<String>) document.get("attendees");
            attendees = new ArrayList<>(imageUrlsList);
        }
        return new PublicEvent(title, eventId, uid, location, date, startTime, endTime, eventType, genre, description, price, link, bannerUrl, imageUrls, tags, attendees);
    }
    public interface EventFetchListener {
        void onEventFetched(PublicEvent event) throws ParseException;
    }

    public interface EventSearchCallback{
        void onEventsFound(ArrayList<PublicEvent> events);
    }

    public static void searchEvents(String searchTerm, EventSearchCallback eventSearchCallback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Query titleQuery = db.collection("PublicEvents")
                .whereGreaterThanOrEqualTo("title_lower", searchTerm.toLowerCase())
                .whereLessThanOrEqualTo("title_lower", searchTerm.toLowerCase() + "\uf8ff");

        Query descriptionQuery = null;
        descriptionQuery = db.collection("PublicEvents")
                    .whereGreaterThanOrEqualTo("description_lower", searchTerm.toLowerCase())
                    .whereLessThanOrEqualTo("description_lower", searchTerm.toLowerCase() + "\uf8ff");


        List<Task<QuerySnapshot>> tasks = new ArrayList<>();
        tasks.add(titleQuery.get());
        if (descriptionQuery != null) {
            tasks.add(descriptionQuery.get());
        }


        Task<List<QuerySnapshot>> allTasks = Tasks.whenAllSuccess(tasks);
        allTasks.addOnCompleteListener(new OnCompleteListener<List<QuerySnapshot>>() {
            @Override
            public void onComplete(@NonNull Task<List<QuerySnapshot>> task) {
                if (task.isSuccessful()) {
                    ArrayList<PublicEvent> events = new ArrayList<>();
                    for (QuerySnapshot snapshot : task.getResult()) {
                        for (DocumentSnapshot document : snapshot.getDocuments()) {
                            PublicEvent event = getPublicEventFromDocument(document);
                            events.add(event);
                        }
                        eventSearchCallback.onEventsFound(events);
                    }
                } else {
                    Log.d("FirestoreSearch", "Search failed", task.getException());
                }
            }
        });
    }


    public static PrivateEvent publicToPrivate(PublicEvent event) {
        return new PrivateEvent(event.getTitle(), event.getEventId(), event.getUid(), event.getLocation(), event.getDate(), false, event.getStartTime(), event.getEndTime(), false, null, event.getEventType(), event.getDescription());
    }




    public static void getUserEvents(ProfileFragment.UserEventsCallback userEventsCallback, String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        ArrayList<PublicEvent> userEvents = new ArrayList<>();
        db.collection("PublicEvents")
                .whereEqualTo("creatorId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d("User events found", "document found", null);
                            PublicEvent event = convertDocumentToEvent(document);
                            event.setEventId(document.getId());
                            userEvents.add(event);
                            Log.d("Document event", event.toString());
                        }
                        userEventsCallback.onCallback(userEvents);
                    } else {
                        Log.d("No user events found", "no documents found", null);

                    }
                });
        if(userEvents!=null){
            for(int i=0; i<userEvents.size(); i++){
                Log.d("User's Events", userEvents.get(i).toString(), null);
            }
        }
    }

}