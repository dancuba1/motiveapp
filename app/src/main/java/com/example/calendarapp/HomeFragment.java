package com.example.calendarapp;

import static android.view.View.GONE;
import static com.example.calendarapp.OnlineDb.PublicEventRepository.getAllEvents;
import static com.example.calendarapp.OnlineDb.PublicEventRepository.getFollowingEvents;
import static com.example.calendarapp.OnlineDb.PublicEventRepository.getPublicEventFromDocument;
import static com.example.calendarapp.OnlineDb.PublicEventRepository.searchEvents;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SearchView;

import com.example.calendarapp.EventObjects.PublicEvent;
import com.example.calendarapp.OnlineDb.PublicEventRepository;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private RecyclerView homeEventsRecyclerView;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private SearchView searchEditText;

    private ProgressBar progressBar;
    private MaterialTextView discoverButton, followingButton, noFollowingTextView;
    private boolean discoverFollowingToggle;
    private ImageView motiveLogo;
    private boolean noFollowingVisible;


    private HomeEventsAdapter homeEventsAdapter;
    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        discoverFollowingToggle = true;
        init(view);
        setOnClickListeners();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateData(); // Will decide internally which method to call based on toggle
    }

    private void init(View view) {
        // Initialize your UI components here
        setInitialUIState(view);
        motiveLogo = view.findViewById(R.id.motiveLogo);
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.float_in);
        homeEventsRecyclerView.startAnimation(animation);
        motiveLogo.startAnimation(animation);
        homeEventsRecyclerView.setAdapter(homeEventsAdapter);
        fetchAndDisplayEvents(); // Handles fetching and displaying events for the first time
        noFollowingVisible = false;

    }

    private void updateData() {
        fetchAndDisplayEvents();
    }

    private void fetchAndDisplayEvents() {
        if (discoverFollowingToggle) {
            // Fetch all events
            fetchEvents(this::handleEventsResult);
            setItemClickListener(0);
        } else {
            // Fetch following events
            fetchFollowingEvents(this::handleEventsResult);
            setItemClickListener(0);
        }
        updateUIBasedOnToggle(); // Handles UI updates based on toggle state
    }

    private void handleEventsResult(List<PublicEvent> userEvents) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (userEvents == null || userEvents.isEmpty() || user == null) {
            displayNoEventsFound();
            return;
        }

        List<PublicEvent> filteredEvents = userEvents.stream()
                .filter(event -> !event.getUid().equals(user.getUid()))
                .collect(Collectors.toList());

        homeEventsAdapter.setEvents(filteredEvents);
        homeEventsAdapter.notifyDataSetChanged();

        progressBar.setVisibility(View.GONE);
        homeEventsRecyclerView.setVisibility(filteredEvents.isEmpty() ? View.INVISIBLE : View.VISIBLE);
        noFollowingTextView.setVisibility(filteredEvents.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void setInitialUIState(View view) {
        user = FirebaseAuth.getInstance().getCurrentUser();
        noFollowingTextView = view.findViewById(R.id.noFollowingTextView);
        discoverButton =view.findViewById(R.id.discoverButton);
        followingButton = view.findViewById(R.id.followingHomeButton);
        homeEventsRecyclerView = view.findViewById(R.id.homeEventsRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        homeEventsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        homeEventsRecyclerView.setHasFixedSize(true);
        homeEventsAdapter = new HomeEventsAdapter(getActivity());
    }

    private void updateUIBasedOnToggle() {
        int shadowColor = ContextCompat.getColor(getActivity(), R.color.shadow);
        if (discoverFollowingToggle) {
            discoverButton.setShadowLayer(10, 0, 0, shadowColor);
            followingButton.setShadowLayer(0, 0, 0, Color.TRANSPARENT);
        } else {
            followingButton.setShadowLayer(10, 0, 0, shadowColor);
            discoverButton.setShadowLayer(0, 0, 0, Color.TRANSPARENT);
        }
    }

    private void displayNoEventsFound() {
        progressBar.setVisibility(View.GONE);
        noFollowingTextView.setVisibility(View.VISIBLE);
        homeEventsRecyclerView.setVisibility(View.INVISIBLE);
    }

    // Stub methods to represent fetching events. Implement these based on your backend logic.
    private void fetchEvents(Consumer<List<PublicEvent>> callback) {
        getAllEvents(callback::accept);
    }

    private void fetchFollowingEvents(Consumer<List<PublicEvent>> callback) {
        getFollowingEvents(callback::accept);
    }




    private void setItemClickListener(int code){
        homeEventsAdapter.setOnItemClickListener(event -> {
            // Create an instance of the fragment
            PublicEventViewFragment fragment = new PublicEventViewFragment();

            Bundle args = new Bundle();

            args.putString("eventId", event.getEventId());
            fragment.setArguments(args);
            String backStack = null;
            if(code==0){
                backStack = "home_discover";
            }else{
                backStack = "home_following";
            }

            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainerView, fragment)
                    .addToBackStack(backStack)
                    .commit();
        });
    }


    private void setOnClickListeners(){
        setItemClickListener(0);



        discoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!discoverFollowingToggle){
                    if(noFollowingVisible){
                        noFollowingTextView.setVisibility(GONE);
                    }
                    ArrayList<PublicEvent> events = getAllEvents(allEvents -> {
                        // This callback will be called with the fetched events
                        if (allEvents != null && !allEvents.isEmpty()) {
                            for (PublicEvent event : allEvents) {
                                Log.d("All Events", event.toString());
                            }
                            // Now that you have the events, set them on the adapter
                            homeEventsAdapter.setEvents(allEvents);
                            // Notify the adapter that the data set has changed to refresh the RecyclerView
                            homeEventsAdapter.notifyDataSetChanged();
                        }
                    });
                    homeEventsRecyclerView.setAdapter(homeEventsAdapter);
                    setItemClickListener(0);
                    int shadowColor = ContextCompat.getColor(getActivity(), R.color.shadow);
                    discoverButton.setShadowLayer(10, 0, 0, shadowColor);
                    followingButton.setShadowLayer(0, 0, 0, Color.TRANSPARENT);
                    discoverFollowingToggle = true;
                }
            }
        });
        followingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(discoverFollowingToggle){
                    homeEventsAdapter = new HomeEventsAdapter(getActivity());
                    getFollowingEvents(userEvents -> {
                        // This callback will be called with the fetched events
                        if (userEvents != null && !userEvents.isEmpty()) {
                            for (PublicEvent event : userEvents) {
                                Log.d("Following Events", event.toString());
                            }
                            // Now that you have the events, set them on the adapter
                            homeEventsAdapter.setEvents(userEvents);
                            // Notify the adapter that the data set has changed to refresh the RecyclerView
                            homeEventsAdapter.notifyDataSetChanged();
                        }else{
                            noFollowingTextView.setVisibility(View.VISIBLE);
                            noFollowingVisible = true;
                        }
                    });

                    homeEventsRecyclerView.setAdapter(homeEventsAdapter);
                    setItemClickListener(1);
                    int shadowColor = ContextCompat.getColor(getActivity(), R.color.shadow);
                    followingButton.setShadowLayer(10, 0, 0, shadowColor);
                    discoverButton.setShadowLayer(0, 0, 0, Color.TRANSPARENT);
                    discoverFollowingToggle= false;
                }
            }
        });
    }


    public interface EventsCallback {
        void onCallback(ArrayList<PublicEvent> Events);
    }




}