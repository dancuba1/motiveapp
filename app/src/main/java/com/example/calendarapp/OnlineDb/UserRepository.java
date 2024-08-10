package com.example.calendarapp.OnlineDb;

import static android.content.ContentValues.TAG;

import android.util.Log;

import com.example.calendarapp.EventObjects.PublicEvent;
import com.example.calendarapp.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserRepository {


    public interface UsernameCheckCallback {
        void onUsernameChecked(boolean isTaken);

        void onError(Exception e);
    }



    public static void isUsernameTaken(String username, UsernameCheckCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot result = task.getResult();
                        boolean isTaken = result != null && !result.isEmpty();
                        // Use callback to report the result
                        callback.onUsernameChecked(isTaken);
                    } else {
                        // Use callback to report an error
                        callback.onError(task.getException());
                    }
                });
    }


    public interface ValidationCallback {
        void onValidationResult(boolean isValid);
    }

    public static void removeAttendeeFromEvent(String eventId, String attendeeId) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.d("RemoveAttendee", "No user logged in");
            return;
        }
        Log.d("RemoveAttendeeFromEvent", "Starting to remove attendee");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference attendeesRef = db.collection("Attendees").document(eventId);

        // First, get the document to check if it exists
        attendeesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    // Document exists, update it by removing the userId
                    attendeesRef.update("attendeeUids", FieldValue.arrayRemove(attendeeId))
                            .addOnSuccessListener(aVoid -> Log.d("Attendees Update", "Attendee removed successfully"))
                            .addOnFailureListener(e -> Log.e("Attendees Update", "Error removing attendee", e));
                } else {
                    // Document does not exist. In this context, nothing to remove from.
                    Log.d("Attendees Update", "No document found to remove attendee from");
                }
            } else {
                Log.e("Firestore Error", "Failed to fetch attendees document", task.getException());
            }
        });
    }


    public static void deleteAccountAssets(FirebaseUser user) {
        String userId = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();

        // Step 1: Delete user document from Users collection
        db.collection("Users").document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d("Delete Account Assets", "User document successfully deleted!"))
                .addOnFailureListener(e -> Log.w("Delete Account Assets", "Error deleting user document", e));

        //Delete all documents from PublicEvents where creatorId is the user's id
        db.collection("PublicEvents")
                .whereEqualTo("creatorId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            db.collection("PublicEvents").document(document.getId())
                                    .delete()
                                    .addOnSuccessListener(aVoid -> Log.d("Delete Account Assets", "Event document successfully deleted!"))
                                    .addOnFailureListener(e -> Log.w("Delete Account Assets", "Error deleting event document", e));
                        }
                    } else {
                        Log.d("Delete Account Assets", "Error getting documents: ", task.getException());
                    }
                });

        // Step 3: Delete the user's profile image from Firebase Storage
        StorageReference profileImageRef = storage.getReference().child("profileImages/" + userId + ".jpg");
        profileImageRef.delete()
                .addOnSuccessListener(aVoid -> Log.d("Delete Account Assets", "Profile Image successfully deleted!"))
                .addOnFailureListener(e -> Log.w("Delete Account Assets", "Error deleting profile image " + "profileImages/" + userId + ".jpg" , e));
    }

    public interface UsersCallback{
        void usersRetrieved(ArrayList<User> users);
        void usersNotRetrieved();
    }

    public interface UserCallback{
        void userRetrieved(User user);
    }

    public static void getUserDetails(String uid, UserCallback callback){
        Log.d("In loadProfileData", uid);
        DocumentReference userRef = FirebaseFirestore.getInstance().collection("Users")
                .document(uid);
        userRef.addSnapshotListener((value, error) -> {
            if(error!=null){
                return;
            }if(value.exists()){
                User user = value.toObject(User.class);
                Log.d("follow getUserDetails", user.toString());
                callback.userRetrieved(user);
            }
            else{
                callback.userRetrieved(null);
            }
        });
    }

    public static void addAttendeeToEvent(String eventId) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String attendeeId = user.getUid();
        Log.d("In addAttendeeToEvent", "start of Add Attendee");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference attendeesRef = db.collection("Attendees").document(eventId);

        attendeesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Document exists, update it by adding the new userId
                    attendeesRef.update("attendeeUids", FieldValue.arrayUnion(attendeeId))
                            .addOnSuccessListener(aVoid -> Log.d("Attendees Update", "Attendee added successfully"))
                            .addOnFailureListener(e -> Log.e("Attendees Update", "Error adding attendee", e));
                } else {
                    // Document does not exist, create it and set the userId
                    Map<String, Object> newAttendees = new HashMap<>();
                    ArrayList<String> initialAttendeeList = new ArrayList<>();
                    initialAttendeeList.add(attendeeId);
                    newAttendees.put("attendeeUids", initialAttendeeList);

                    attendeesRef.set(newAttendees)
                            .addOnSuccessListener(aVoid -> Log.d("Attendees Creation", "Attendees document created and attendee added"))
                            .addOnFailureListener(e -> Log.e("Attendees Creation", "Error creating attendees document", e));
                }
            } else {
                Log.e("Firestore Error", "Failed to fetch attendees document", task.getException());
            }
        });
    }




    public static void getFollowers(String userId, UsersCallback callback){
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            DocumentReference followersRef = db.collection("Followers").document(userId);

            ArrayList<User> users = new ArrayList<>();
            followersRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        // Assuming 'followerIds' is an array field in the document

                        List<String> followerIds = (List<String>) document.get("followers");
                        Log.d("follower id", followerIds.toString());
                        if (followerIds != null) {
                            final int[] counter = {followerIds.size()}; // Use array for mutable integer
                            for(int i=0;i<followerIds.size();i++){
                                getUserDetails(followerIds.get(i), new UserCallback() {
                                    @Override
                                    public void userRetrieved(User user) {
                                        users.add(user);
                                        Log.d("follow getUser",user.toString());
                                        counter[0]--;
                                        Log.d("followCounter", Arrays.toString(counter));
                                        if (counter[0]==0) {
                                            Log.d("follow Users", users.toString());
                                            callback.usersRetrieved(users);
                                        }
                                    }
                                });

                            }


                        } else {
                            callback.usersNotRetrieved();
                        }
                    } else {
                        callback.usersNotRetrieved();
                    }
                } else {
                    callback.usersNotRetrieved();
                }
            });
        }


    public static void getFollowing(String userId, UsersCallback callback){
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference followersRef = db.collection("Following").document(userId);

        ArrayList<User> users = new ArrayList<>();
        followersRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    // Assuming 'followerIds' is an array field in the document

                    List<String> followedUsers = (List<String>) document.get("followedUsers");
                    Log.d("following id", followedUsers.toString());
                    if (followedUsers != null) {
                        final int[] counter = {followedUsers.size()}; // Use array for mutable integer
                        for(int i=0;i<followedUsers.size();i++){
                            getUserDetails(followedUsers.get(i), new UserCallback() {
                                @Override
                                public void userRetrieved(User user) {
                                    users.add(user);
                                    Log.d("follow getUser",user.toString());
                                    counter[0]--;
                                    Log.d("followCounter", Arrays.toString(counter));
                                    if (counter[0]==0) {
                                        Log.d("follow Users", users.toString());
                                        callback.usersRetrieved(users);
                                    }
                                }
                            });

                        }


                    } else {
                        callback.usersNotRetrieved();
                    }
                } else {
                    callback.usersNotRetrieved();
                }
            } else {
                callback.usersNotRetrieved();
            }
        });
    }




    public static void getAttendeePreviews(PublicEvent event, final AttendeePreviewCallback callback, String userId, ArrayList<String> followerIds) {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String eventId = event.getEventId();
    //Access the Attendees document
    db.collection("Attendees").document(eventId).get().addOnCompleteListener(task -> {
        //If Firebase allows access
        if (task.isSuccessful()) {
            DocumentSnapshot document = task.getResult();
            //If document exists
            if (document.exists()) {
                List<String> attendeeUids = (List<String>) document.get("attendeeUids");
                //If the event has attendees
                if (attendeeUids != null) {

                    ArrayList<AttendeePreview> attendeePreviews = new ArrayList<>();
                    final int[] counter = {attendeeUids.size()};

                    for (String uid : attendeeUids) {
                        // Check if the attendee is followed by the user
                        if (followerIds.contains(uid)) {
                            db.collection("Users").document(uid).get().addOnCompleteListener(userTask -> {
                                if (userTask.isSuccessful()) {
                                    DocumentSnapshot userDoc = userTask.getResult();
                                    if (userDoc.exists()) {
                                        String username = userDoc.getString("username");
                                        String profilePicUrl = userDoc.getString("profileImage");
                                        AttendeePreview attendeePreview = new AttendeePreview();
                                        attendeePreview.setEventId(eventId);
                                        attendeePreview.setAttendeeUid(uid);
                                        attendeePreview.setAttendeeName(username);
                                        attendeePreview.setProfilePicUrl(profilePicUrl);

                                        if (!(uid.equals(userId))) {
                                            attendeePreviews.add(attendeePreview);
                                        }
                                    }
                                } else {
                                    Log.d("getAttendeePreviews", "Failed to fetch user document: " + userTask.getException());
                                }
                                counter[0]--;
                                if (counter[0] == 0) {
                                    // All user details have been fetched
                                    callback.onEventPreviewsFetched(attendeePreviews);
                                }
                            });
                        } else {
                            counter[0]--; // Decrement counter for non-followed users
                            if (counter[0] == 0) {
                                // All user details have been fetched
                                callback.onEventPreviewsFetched(attendeePreviews);
                            }
                        }
                    }
                }else {
                    Log.d("getAttendeePreviews", "Failed to fetch document: " + task.getException());
                }
            } else {
                // Handle no document found
            }
        } else {
            // Log or handle error
        }
    });
}

    public static void notAttended(String eventId, String uid, final CheckAttendanceCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference eventAttendeesRef = db.collection("Attendees").document(eventId);

        eventAttendeesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    List<String> attendees = (List<String>) document.get("attendeeUids");
                    // Check if the uid is not in the attendees list
                    boolean isNotAttending = attendees == null || !attendees.contains(uid);
                    callback.onCheckCompleted(isNotAttending);
                } else {
                    // Document does not exist, meaning no one is attending yet
                    callback.onCheckCompleted(true);
                }
            } else {
                Log.w(TAG, "Error checking attendance", task.getException());
            }
        });
    }



    public interface CheckAttendanceCallback {
        void onCheckCompleted(boolean isNotAttending);
    }

    public interface AttendeePreviewCallback {
        void onEventPreviewsFetched(ArrayList<AttendeePreview> attendeePreviews);
    }
}
