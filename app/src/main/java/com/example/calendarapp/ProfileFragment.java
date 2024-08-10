package com.example.calendarapp;


import static android.view.View.GONE;

import static com.example.calendarapp.OnlineDb.FollowUtils.followUser;
import static com.example.calendarapp.OnlineDb.FollowUtils.getFollowerCount;
import static com.example.calendarapp.OnlineDb.FollowUtils.getFollowingCount;
import static com.example.calendarapp.OnlineDb.FollowUtils.isUserFollowed;
import static com.example.calendarapp.OnlineDb.FollowUtils.unfollowUser;
import static com.example.calendarapp.OnlineDb.PublicEventRepository.convertDocumentToEvent;
import static com.example.calendarapp.OnlineDb.PublicEventRepository.getUserEvents;
import static com.example.calendarapp.OnlineDb.SavedEventRepository.getSavedEvents;
import static com.example.calendarapp.OnlineDb.UserRepository.getFollowers;
import static com.example.calendarapp.OnlineDb.UserRepository.getFollowing;
import static com.example.calendarapp.OnlineDb.UserRepository.getUserDetails;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.calendarapp.EventObjects.EventGenre;
import com.example.calendarapp.EventObjects.EventType;
import com.example.calendarapp.EventObjects.PublicEvent;
import com.example.calendarapp.OnlineDb.AttendeeAdapter;
import com.example.calendarapp.OnlineDb.AttendeePreview;
import com.example.calendarapp.OnlineDb.FollowUtils;
import com.example.calendarapp.OnlineDb.UserRepository;
import com.example.calendarapp.SignUpLogin.EventSignupFragment;
import com.example.calendarapp.SignUpLogin.LoginRegisterActivity;
import com.example.calendarapp.Utils.TimestampLocalDateTimeConverter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private String username, email;
    private ProgressBar progressBar;
    private TextView emailTextView, usernameTextView, descriptionTextView, myEventsTextView;
    private ImageView profilePic;
    private FirebaseAuth mAuth;
    private MaterialButton noEventsButton, followingButton;
    private MaterialCardView myEventsCard, savedEventsCard;
    private MaterialTextView followingTV, followingCountTV, followerCountTV;
    private FirebaseUser user;
    private RecyclerView eventsRecyclerView;
    private ImageView settingsButton, savedEventsButton;
    private boolean isFollowing = false;
    private boolean ownSaved;
    private boolean yourProfile;
    private String uid;
    private LinearLayout followerLayout, followingLayout;
    private FragmentManager fragmentManager;
    private ArrayList<String> eventIds;
    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);


        Bundle args = getArguments();
        ownSaved = true;


        init(view, args);

        setOnClickListeners();


        return view;
    }

    private void init(View view, Bundle args){
        user = FirebaseAuth.getInstance().getCurrentUser();
        if(getArguments()!=null){
            uid = getArguments().getString("userId");
            yourProfile = false;
        }else {
            uid = user.getUid();
            yourProfile = true;
        }
        fragmentManager = getParentFragmentManager();
        progressBar = view.findViewById(R.id.progressBarProfile);
        savedEventsCard = view.findViewById(R.id.savedEventsCard);
        myEventsCard = view.findViewById(R.id.myEventsCard);
        followingButton = view.findViewById(R.id.followMatButton);
        usernameTextView = view.findViewById(R.id.usernameTextView);
        descriptionTextView = view.findViewById(R.id.descriptionTextView);
        profilePic = view.findViewById(R.id.profilePicImageView);
        eventsRecyclerView = view.findViewById(R.id.events);
        settingsButton = view.findViewById(R.id.settingsButton);
        myEventsTextView = view.findViewById(R.id.myEventsTextView);
        followingCountTV = view.findViewById(R.id.followingCountTV);
        followerCountTV = view.findViewById(R.id.followerCountTV);

        followerLayout = view.findViewById(R.id.followerLayout);
        followingLayout = view.findViewById(R.id.followingLayout);

        noEventsButton = view.findViewById(R.id.noEventsAddEventButton);

        if (yourProfile) {
          getFollowingCount(uid, (success, count) -> {
              if(success){
                  if(count != 0){
                      followingCountTV.setText((String.valueOf(count)));
                  }
              }else{
                  Log.d("Get following count", "Failed");
              }
          });
          getFollowerCount(uid, (success, count) -> {
              if(success){
                  if(count != 0){
                      followerCountTV.setText((String.valueOf(count)));
                  }
              }else{
                  Log.d("Get following count", "Failed");
              }
          });
            loadProfileData(view, uid);
        }


        mAuth = FirebaseAuth.getInstance();

        if(!yourProfile){ //when loading a different user to your own.
           getFollowingCount(uid, (success, count) -> {
               if(success){
                   if(count != 0){
                       followingCountTV.setText((String.valueOf(count)));
                   }
               }else{
                   Log.d("Get following count", "Failed");
               }
           });
            getFollowerCount(uid, (success, count) -> {
                if(success){
                    if(count != 0){
                        followerCountTV.setText((String.valueOf(count)));
                    }
                }else{
                    Log.d("Get following count", "Failed");
                }
            });

            loadProfileData(view, uid);
            followingButton.setVisibility(View.VISIBLE);
            settingsButton.setVisibility(GONE);
            myEventsTextView.setText("Events");
            noEventsButton.setVisibility(GONE);
            //savedEventsCard.setVisibility(GONE);

            MaterialCardView proficPicUsernameEmailCard = view.findViewById(R.id.proficPicUsernameEmailCard);
        }
    }



    private void setOnClickListeners() {

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventSignupFragment fragment = new EventSignupFragment();
                Bundle args = new Bundle();
                args.putString("edit", "yes");
                fragment.setArguments(args);
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainerView, fragment)
                        .addToBackStack("profile")
                        .commit();

            }
        });


        followerLayout.setOnClickListener(v -> {

            getFollowers(uid, new UserRepository.UsersCallback() {
                @Override
                public void usersRetrieved(ArrayList<User> users) {
                    setUsersRV(users);


                }
                @Override
                public void usersNotRetrieved() {
                    Log.d("getFollowers", "No followers retrieved");
                }
            });
        });
        followingLayout.setOnClickListener(v ->{
            getFollowing(uid, new UserRepository.UsersCallback() {
                    @Override
                    public void usersRetrieved(ArrayList<User> users) {
                       setUsersRV(users);
                    }
                    @Override
                    public void usersNotRetrieved() {
                        Log.d("getFollowers", "No followers retrieved");
                    }
                });
            });

        noEventsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddEditPublicFragment fragment = new AddEditPublicFragment();
                com.example.calendarapp.Utils.FragmentManager.changeFragment(fragment, getActivity().getSupportFragmentManager());

            }
        });

        savedEventsCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HomeEventsAdapter adapter = (HomeEventsAdapter) getSavedEventsAdapter();
                    setAdapterOnItemClick(adapter);
                    eventsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                    eventsRecyclerView.setHasFixedSize(true);
                    eventsRecyclerView.setAdapter(adapter);
                    ownSaved = false;
                }
            }
        );

        myEventsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                HomeEventsAdapter adapter;
                if(yourProfile){
                   adapter = (HomeEventsAdapter) getMyEventsAdapter(user.getUid());
                }else{
                    adapter = (HomeEventsAdapter) getMyEventsAdapter(uid);
                }
                setAdapterOnItemClick(adapter);
                eventsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                eventsRecyclerView.setHasFixedSize(true);
                eventsRecyclerView.setAdapter(adapter);
                ownSaved = true;


            }
        });


    }

    private void setUsersRV(ArrayList<User> users) {
        ArrayList<AttendeePreview> previews = new ArrayList<>();
        for(User user : users){
            Log.d("followers", user.toString());
            AttendeePreview attendeePreview = new AttendeePreview();
            attendeePreview.setAttendeeUid(user.getUid());
            attendeePreview.setAttendeeName(user.getUsername());
            attendeePreview.setProfilePicUrl(user.getProfileImage());
            Log.d("follower attendeepreview", attendeePreview.toString());
            previews.add(attendeePreview);
        }


        AttendeeAdapter adapter = new AttendeeAdapter(getContext(), previews);
        adapter.setOnItemClickListener(new AttendeeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(AttendeePreview attendee) {
                Bundle arguments = new Bundle();
                arguments.putString("userId", attendee.getAttendeeUid());
                ProfileFragment profileFragment = new ProfileFragment();
                profileFragment.setArguments(arguments);
                fragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainerView, profileFragment)
                        .addToBackStack("publicEventView")
                        .commit();
            }
        });
        eventsRecyclerView.setAdapter(adapter);
    }

    private RecyclerView.Adapter getMyEventsAdapter(String uid) {
        final HomeEventsAdapter adapter = new HomeEventsAdapter(getActivity());
        getUserEvents(new UserEventsCallback() {
            @Override
            public void onCallback(ArrayList<PublicEvent> userEvents) {
                // This callback will be called with the fetched events
                if (userEvents != null && !userEvents.isEmpty()) {
                    for (PublicEvent event : userEvents) {
                        Log.d("User's Events", event.toString());
                    }
                    // Now that you have the events, set them on the adapter
                    adapter.setEvents(userEvents);
                    // Notify the adapter that the data set has changed to refresh the RecyclerView
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);

                }else{
                    progressBar.setVisibility(View.GONE); // Hide the ProgressBar
                }
                eventsRecyclerView.setVisibility(View.VISIBLE); // Show the RecyclerView

            }
        }, uid);
        return adapter;
    }

    private RecyclerView.Adapter getSavedEventsAdapter() {
        final HomeEventsAdapter adapter = new HomeEventsAdapter(getActivity());
        getSavedEvents(new UserEventsCallback() {
            @Override
            public void onCallback(ArrayList<PublicEvent> savedEvents) {
                // This callback will be called with the fetched events
                if (savedEvents != null && !savedEvents.isEmpty()) {
                    for (PublicEvent event : savedEvents) {
                        Log.d("Saved Events", event.toString());
                    }
                    // Now that you have the events, set them on the adapter
                    adapter.setEvents(savedEvents);
                    // Notify the adapter that the data set has changed to refresh the RecyclerView
                    adapter.notifyDataSetChanged();
                    eventsRecyclerView.setVisibility(View.VISIBLE);
                }else{
                    myEventsTextView.setVisibility(View.VISIBLE);
                }
            }
        }, user.getUid());
        return adapter;
    }


    public void onResume() {
        super.onResume();
        // Check if the user is still logged in
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            //loadProfileData(getView(), user.getUid());
        }
    }




    private void loadProfileData(View view, String uid){
        yourProfile = uid.equals(user.getUid());
        getUserDetails(uid, new UserRepository.UserCallback() {
            @Override
            public void userRetrieved(User user) {
                String username = user.getUsername();
                String description = user.getDescription();
                descriptionTextView.setText(description);
                usernameTextView.setText(username);
                String profileImageUrl = user.getProfileImage();
                if (profileImageUrl != null) {
                    Log.d("Profile Image Url", profileImageUrl);
                    loadImageIntoView(profileImageUrl, view, profilePic);
                }
            }
        });
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        eventsRecyclerView.setHasFixedSize(true);

        final HomeEventsAdapter adapter = new HomeEventsAdapter(getActivity());
        eventsRecyclerView.setAdapter(adapter);
        getUserEvents(userEvents -> {
            // This callback will be called with the fetched events
            if (userEvents != null && !userEvents.isEmpty()) {
                for (PublicEvent event : userEvents) {
                    Log.d("User's Events", event.toString());
                }
                // Now that you have the events, set them on the adapter
                adapter.setEvents(userEvents);
                // Notify the adapter that the data set has changed to refresh the RecyclerView
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                eventsRecyclerView.setVisibility(View.VISIBLE); // Show the RecyclerView

            }else{
                progressBar.setVisibility(View.GONE); // Hide the ProgressBar
            }
        }, uid);

        setAdapterOnItemClick(adapter);

        if(!user.getUid().equals(uid)){
            followingLogic(uid);
        }

    }

    private void setAdapterOnItemClick(HomeEventsAdapter adapter){
        adapter.setOnItemClickListener(new HomeEventsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(PublicEvent event) {
                // Create an instance of the fragment
                PublicEventViewFragment fragment = new PublicEventViewFragment();
                Bundle args = new Bundle();

                Log.d("Event clicked", event.toString(), null);
                args.putString("eventId", event.getEventId());

                fragment.setArguments(args);

                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainerView, fragment)
                        .addToBackStack("profile")
                        .commit();
            }
        });

    }

    private void followingLogic(String uid) {
        Handler handler = new Handler();
        FollowUtils.FollowCheckListener followCheckListener = isFollowedCheck -> {
            if(isFollowedCheck){
                isFollowing=true;
                updateFollowingButtonAppearance();
            }else{
                isFollowing=false;
                updateFollowingButtonAppearance();
            }
        };
        isUserFollowed(uid, followCheckListener);
        followingButton.setOnClickListener(v -> {
            Animation wiggle = AnimationUtils.loadAnimation(getContext(), R.anim.wiggle);
            v.startAnimation(wiggle);
            isUserFollowed(uid, followCheckListener);
            if(!isFollowing){
                followUser(uid, followCheckListener);
                String newFollowerCount = String.valueOf((Long.valueOf(followerCountTV.getText().toString()) + 1));
                followerCountTV.setText(newFollowerCount);
            }else{
                unfollowUser(uid, followCheckListener);
                String newFollowerCount = String.valueOf((Long.valueOf(followerCountTV.getText().toString()) - 1));
                followerCountTV.setText(newFollowerCount);
            }
            followingButton.setEnabled(false);
            updateFollowingButtonAppearance();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    followingButton.setEnabled(true);
                }
            }, 2000);
        });
    }

    private void updateFollowingButtonAppearance() {
        if (isFollowing) {
            followingButton.setText(getString(R.string.following));
            followingButton.setIconResource(R.drawable.tick_symbol); // Set to your follow icon
            followingButton.setBackgroundColor(getResources().getColor(R.color.colorAccent)); // Optional: change button color when following
        } else {
            followingButton.setText(getString(R.string.follow));
            followingButton.setIconResource(R.drawable.ic_add); // Set to your follow icon
            followingButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary)); // Optional: change button color back
        }
    }



    public interface UserEventsCallback {
        void onCallback(ArrayList<PublicEvent> userEvents);
    }


    private void loadImageIntoView(String imageUrl, View view, ImageView imageView){
        if(getActivity()!=null){
            Glide.with(this)
                    .load(imageUrl)
                    .circleCrop()
                    .into(imageView);
        }

    }
}


