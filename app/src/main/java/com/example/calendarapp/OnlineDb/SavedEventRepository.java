package com.example.calendarapp.OnlineDb;

import static android.content.ContentValues.TAG;
import static com.example.calendarapp.OnlineDb.PublicEventRepository.getEventById;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.calendarapp.EventObjects.PublicEvent;
import com.example.calendarapp.ProfileFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class SavedEventRepository {
    public static void saveEvent(String eventId) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();
        Log.d("In saveEvent", "start of Save Event");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference savedEventsRef = db.collection("SavedEvents").document(userId);

        savedEventsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Document exists, update it by adding the new userId
                    savedEventsRef.update("savedEventIds", FieldValue.arrayUnion(eventId))
                            .addOnSuccessListener(aVoid -> Log.d("Saved Events Update", "Event added successfully"))
                            .addOnFailureListener(e -> Log.e("Saved Events Update", "Error adding Event", e));
                } else {
                    // Document does not exist, create it and set the userId
                    Map<String, Object> newEvents = new HashMap<>();
                    ArrayList<String> initialSavedEvents = new ArrayList<>();
                    initialSavedEvents.add(eventId);
                    newEvents.put("savedEventIds", initialSavedEvents);

                    savedEventsRef.set(newEvents)
                            .addOnSuccessListener(aVoid -> Log.d("Saved Events Creation", "Saved Events document created and event added"))
                            .addOnFailureListener(e -> Log.e("Saved Events Creation", "Error creating Saved Events document", e));
                }
            } else {
                Log.e("Firestore Error", "Failed to fetch Saved Events document", task.getException());
            }
        });
    }

    public static void removeSavedEvent(String eventId, String userId) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.d("removeSavedEvent", "No user logged in");
            return;
        }
        Log.d("RemoveSavedEvent", "Starting to remove saved event");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference savedEventRef = db.collection("SavedEvents").document(userId);

        // First, get the document to check if it exists
        savedEventRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    // Document exists, update it by removing the userId
                    savedEventRef.update("savedEventIds", FieldValue.arrayRemove(eventId))
                            .addOnSuccessListener(aVoid -> Log.d("Saved Event Update", "Saved Event removed successfully"))
                            .addOnFailureListener(e -> Log.e("Saved Event Update", "Error removing Saved Event", e));
                } else {
                    // Document does not exist. In this context, nothing to remove from.
                    Log.d("Saved Event Update", "No document found to remove saved event from");
                }
            } else {
                Log.e("Firestore Error", "Failed to fetch Saved Event document", task.getException());
            }
        });
    }


    public static void getSavedEvents(ProfileFragment.UserEventsCallback callback, String uid) {
        FirebaseFirestore.getInstance().collection("SavedEvents").document(uid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists() && document.contains("savedEventIds")) {
                                ArrayList<String> savedEventIds = (ArrayList<String>) document.get("savedEventIds");
                                if (savedEventIds != null && !savedEventIds.isEmpty()) {
                                    ArrayList<PublicEvent> savedEvents = new ArrayList<>();
                                    AtomicInteger eventsToFetch = new AtomicInteger(savedEventIds.size()); // Counter for fetched events
                                    for (String eventId : savedEventIds) {
                                        getEventById(eventId, new PublicEventRepository.EventFetchListener() {
                                            @Override
                                            public void onEventFetched(PublicEvent event) throws ParseException {
                                                event.setEventId(event.getEventId());
                                                savedEvents.add(event);
                                                if (eventsToFetch.decrementAndGet() == 0) { // Decrement the counter and check if all are fetched
                                                    callback.onCallback(savedEvents); // Callback is now in the right place
                                                }
                                            }
                                        });
                                    }
                                } else {
                                    callback.onCallback(new ArrayList<>()); // No events to fetch, return empty list
                                }
                                Log.d("Event Fetch", "DocumentSnapshot data: " + document.getData());
                            } else {
                                Log.d("Event Fetch", "No such document");
                            }
                        } else {
                            Log.d("Event Fetch", "get failed with ", task.getException());
                        }
                    }
                });
    }

    public static void notSaved(String eventId, String uid, final CheckSavedCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference eventAttendeesRef = db.collection("SavedEvents").document(uid);

        eventAttendeesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    List<String> savedEventIds = (List<String>) document.get("savedEventIds");
                    // Check if the uid is not in the attendees list
                    boolean isNotSaved = savedEventIds == null || !savedEventIds.contains(eventId);
                    callback.onCheckCompleted(isNotSaved);
                } else {
                    // Document does not exist, meaning no one is attending yet
                    callback.onCheckCompleted(true);
                }
            } else {
                Log.w(TAG, "Error checking saved events", task.getException());
            }
        });
    }

    public interface CheckSavedCallback {
        void onCheckCompleted(boolean isNotSaved);
    }
}
