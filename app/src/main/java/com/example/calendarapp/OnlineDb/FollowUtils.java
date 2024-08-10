package com.example.calendarapp.OnlineDb;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FollowUtils {

    private static FirebaseFirestore db = FirebaseFirestore.getInstance();

    /*
    public static void followUser(String userIdToFollow, final FollowCheckListener listener) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (currentUser == null) {
            listener.onFollowCheck(false);
            return;
        }

        String currentUserId = currentUser.getUid();

        if (currentUserId.equals(userIdToFollow)) {
            // Prevent users from following themselves
            listener.onFollowCheck(false);
            return;
        }

        // Document references
        DocumentReference followedUserDocRef = db.collection("Users").document(userIdToFollow);
        DocumentReference newFollowerDocRef = followedUserDocRef.collection("Followers").document(currentUserId);

        db.runTransaction((Transaction.Function<Void>) transaction -> {
            DocumentSnapshot followedUserSnapshot = transaction.get(followedUserDocRef);

            // Initialize or increment the followCount
            long followCount = followedUserSnapshot.contains("followCount") ? followedUserSnapshot.getLong("followCount") : 0;
            followCount = followCount == 0 ? 1 : followCount + 1; // If it doesn't exist or is 0, start from 1; otherwise, increment

            // Check if the follower document exists
            DocumentSnapshot followerSnapshot = transaction.get(newFollowerDocRef);
            if (!followerSnapshot.exists()) {
                // Only increment and create the follower document if it doesn't exist
                transaction.set(newFollowerDocRef, new HashMap<>()); // Creates the new follower document
                transaction.update(followedUserDocRef, "followCount", followCount); // Update the follow count
            }

            return null; // Transaction must return null because it's of type Function<Void>
        }).addOnSuccessListener(unused -> {
            Log.d("Follow", "Transaction success: Following user " + userIdToFollow);
            listener.onFollowCheck(true);
        }).addOnFailureListener(e -> {
            Log.e("Follow", "Transaction failure: ", e);
            listener.onFollowCheck(false);
        });
    }


     */
    public static interface FollowActionListener {
        void onFollowActionComplete(boolean isFollowed);
    }

    public static void followUser(String targetUserId, final FollowCheckListener listener) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference followingRef = db.collection("Following").document(currentUserId);
        DocumentReference followersRef = db.collection("Followers").document(targetUserId);

        db.runTransaction((Transaction.Function<Void>) transaction -> {
            DocumentSnapshot followingSnapshot = transaction.get(followingRef);
            DocumentSnapshot followersSnapshot = transaction.get(followersRef);

            // Handle the Following document updates
            List<String> followedUsers;
            long followingCount = 0;
            if (followingSnapshot.exists()) {
                followedUsers = (List<String>) followingSnapshot.get("followedUsers");
                if (followedUsers == null) {
                    followedUsers = new ArrayList<>();
                }
                followingCount = followingSnapshot.getLong("followingCount") != null ? followingSnapshot.getLong("followingCount") : 0;
            } else {
                followedUsers = new ArrayList<>();
            }
            if (!followedUsers.contains(targetUserId)) {
                followedUsers.add(targetUserId);
                followingCount++;
            }

            // Handle the Followers document updates
            List<String> followers;
            long followerCount = 0;
            if (followersSnapshot.exists()) {
                followers = (List<String>) followersSnapshot.get("followers");
                if (followers == null) {
                    followers = new ArrayList<>();
                }
                followerCount = followersSnapshot.getLong("followerCount") != null ? followersSnapshot.getLong("followerCount") : 0;
            } else {
                followers = new ArrayList<>();
            }
            if (!followers.contains(currentUserId)) {
                followers.add(currentUserId);
                followerCount++;
            }

            // Apply the updates
            Map<String, Object> followingUpdates = new HashMap<>();
            followingUpdates.put("followedUsers", followedUsers);
            followingUpdates.put("followingCount", followingCount);
            transaction.set(followingRef, followingUpdates);
            Map<String, Object> followerUpdates = new HashMap<>();
            followerUpdates.put("followers", followers);
            followerUpdates.put("followerCount", followerCount);
            transaction.set(followersRef, followerUpdates);

            return null;
        }).addOnSuccessListener(aVoid -> {
            listener.onFollowCheck(true);
            Log.d("FollowUser", "Success");
        }).addOnFailureListener(e -> {
            listener.onFollowCheck(false);
            Log.d("FollowUser", "Failure", e);
        });
    }


    public static void getFollowingCount(String userId, final FollowingCountListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference followingRef = db.collection("Following").document(userId);

        followingRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot snapshot = task.getResult();
                if (snapshot != null && snapshot.exists()) {
                    // Get the followingCount from the document, default to 0 if not present
                    long followingCount = snapshot.getLong("followingCount") != null ? snapshot.getLong("followingCount") : 0;
                    listener.onFollowingCountReceived(true, followingCount);
                } else {
                    // Document does not exist, implying the user is not following anyone
                    listener.onFollowingCountReceived(true, 0);
                }
            } else {
                // Handle the failure in fetching the following count
                listener.onFollowingCountReceived(false, 0);
            }
        });
    }

    public interface FollowingCountListener {
        void onFollowingCountReceived(boolean success, long count);
    }


    public static void unfollowUser(String targetUserId, final FollowCheckListener listener) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // References for both the Following and Followers documents
        DocumentReference followingRef = db.collection("Following").document(currentUserId);
        DocumentReference followersRef = db.collection("Followers").document(targetUserId);

        db.runTransaction((Transaction.Function<Void>) transaction -> {
            DocumentSnapshot followingSnapshot = transaction.get(followingRef);
            DocumentSnapshot followersSnapshot = transaction.get(followersRef);

            // Handle the Following document updates
            List<String> followedUsers;
            long followingCount = 0;
            if (followingSnapshot.exists()) {
                followedUsers = (List<String>) followingSnapshot.get("followedUsers");
                followingCount = followingSnapshot.getLong("followingCount") != null ? followingSnapshot.getLong("followingCount") : 0;
                if (followedUsers != null && followedUsers.remove(targetUserId)) {
                    followingCount = Math.max(0, followingCount - 1);
                }
            }else{
                followedUsers = new ArrayList<>();
            }

            // Handle the Followers document updates
            List<String> followers;
            long followerCount = 0;
            if (followersSnapshot.exists()) {
                followers = (List<String>) followersSnapshot.get("followers");
                followerCount = followersSnapshot.getLong("followerCount") != null ? followersSnapshot.getLong("followerCount") : 0;
                if (followers != null && followers.remove(currentUserId)) {
                    followerCount = Math.max(0, followerCount - 1);
                }
            }else{
                followers = new ArrayList<>();
            }

            Map<String, Object> followingUpdates = new HashMap<>();
            followingUpdates.put("followedUsers", followedUsers);
            followingUpdates.put("followingCount", followingCount);
            transaction.set(followingRef, followingUpdates);
            Map<String, Object> followerUpdates = new HashMap<>();
            followerUpdates.put("followers", followers);
            followerUpdates.put("followerCount", followerCount);
            transaction.set(followersRef, followerUpdates);


            return null; // Void function, so return null
        }).addOnSuccessListener(aVoid -> {
            listener.onFollowCheck(false);
            Log.d("UnfollowUser", "Success");
        }).addOnFailureListener(e -> {
            listener.onFollowCheck(true);
            Log.d("UnfollowUser", "Failure", e);
        });
    }




    public static void getFollowerCount(String userId, final FollowerCountListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference followersRef = db.collection("Followers").document(userId);

        followersRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    // Retrieve the followerCount field from the document
                    Long followerCount = document.getLong("followerCount");
                    if (followerCount != null) {
                        listener.onFollowerCountReceived(true, followerCount.intValue());
                    } else {
                        // If the field is not present, assume the user has no followers
                        listener.onFollowerCountReceived(true, 0);
                    }
                } else {
                    // Document does not exist, indicating no followers or an issue
                    listener.onFollowerCountReceived(false, 0);
                }
            } else {
                // Task failed to execute properly
                listener.onFollowerCountReceived(false, 0);
            }
        });
    }

    public interface FollowerCountListener {
        void onFollowerCountReceived(boolean success, int count);
    }




    public static void isUserFollowed(String targetUserId, final FollowCheckListener listener) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Get current user's UID
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Following").document(currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            // Assuming 'followedUsers' is an array of userIds the current user is following
                            // Check if targetUserId exists in the array
                            List<String> followedUsers = (List<String>) document.get("followedUsers");
                            if (followedUsers != null && followedUsers.contains(targetUserId)) {
                                listener.onFollowCheck(true);
                            } else {
                                listener.onFollowCheck(false);
                            }
                        } else {
                            // Document does not exist, meaning the current user is not following anyone
                            listener.onFollowCheck(false);
                        }
                    } else {
                        // Handle task failure
                        listener.onFollowCheck(false);
                    }
                });
    }




    public interface FollowCheckListener {
        void onFollowCheck(boolean isFollowed);
    }
}
