package com.example.calendarapp;

import static com.example.calendarapp.OnlineDb.PublicEventRepository.getPublicEventFromDocument;
import static com.example.calendarapp.OnlineDb.PublicEventRepository.parseJSONToEvents;
import static com.example.calendarapp.OnlineDb.PublicEventRepository.searchEvents;
import static com.example.calendarapp.Utils.LocalDateTimeConverter.convertDateToFormattedString;

import android.graphics.Color;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

/*
import com.algolia.DefaultSearchClient;
import com.algolia.search.SearchClient;
import com.algolia.search.SearchIndex;

 */
import com.algolia.search.saas.Client;
import com.algolia.search.saas.CompletionHandler;
import com.algolia.search.saas.Index;
import com.example.calendarapp.EventObjects.EventGenre;
import com.example.calendarapp.EventObjects.EventType;
import com.example.calendarapp.EventObjects.PublicEvent;
import com.example.calendarapp.OnlineDb.PublicEventRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.slider.Slider;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import mabbas007.tagsedittext.TagsEditText;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Slider minPriceSlider, maxPriceSlider, distanceSlider;
    private TagsEditText tagsEditText;
    private Spinner eventGenreSpinner;
    private MaterialButton searchButton;
    private MaterialTextView minPriceTV, maxPriceTV, eventGenreTV, dateTextView;
    private RecyclerView eventsRV;
    private MaterialTextView noEventsTV, startUpTV;
    private SearchView searchEditText;
    private HomeEventsAdapter adapter;
    private ImageView dropdown;
    private final String API_KEY = BuildConfig.API_KEY;
    private String APPLICATION_ID = "5CM98LV13U";
    private MaterialButton createDatePicker;
    private Date date;
    private boolean advancedSearch, startUp;

    private Index index;
    public SearchFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SearchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SearchFragment newInstance(String param1, String param2) {
        SearchFragment fragment = new SearchFragment();
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
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        init(view);
        setOnClickListeners();
        return view;
    }
    private void init(View view) {
        searchEditText = view.findViewById(R.id.searchView);
        minPriceTV = view.findViewById(R.id.minPriceTV);
        maxPriceTV = view.findViewById(R.id.maxPriceTV);
        eventGenreTV = view.findViewById(R.id.eventGenreLabel);
        noEventsTV = view.findViewById(R.id.noEventsTV);
        minPriceSlider = view.findViewById(R.id.minPriceSlider);
        maxPriceSlider = view.findViewById(R.id.maxPriceSlider);
        distanceSlider = view.findViewById(R.id.locationDistanceSlider);
        tagsEditText = view.findViewById(R.id.searchTags);
        eventGenreSpinner = view.findViewById(R.id.eventGenreSpinnerSearch);
        searchButton = view.findViewById(R.id.searchButton);
        eventsRV = view.findViewById(R.id.searchEventsRV);
        dropdown = view.findViewById(R.id.dropdown);
        createDatePicker =view.findViewById(R.id.createDatePicker);
        dateTextView = view.findViewById(R.id.dateTextView);
        startUp = true;
        startUpTV = view.findViewById(R.id.startUpTV);
        eventsRV.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        eventsRV.setHasFixedSize(true);
        adapter = new HomeEventsAdapter(getContext());

        advancedSearch=false;
        collapseSearch();
        setOnItemClickListener();

        try{
            Client client =  new Client(APPLICATION_ID, API_KEY);
            index = client.getIndex("PublicEvents");
        }catch (Exception e){
            Log.d("SearchFragment", "Client instantiation failed", e);
        }
    }



    private void setOnClickListeners() {
        searchEditText.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            CompletionHandler completionHandler = (jsonObject, e) -> {
                if(jsonObject!=null){
                    try {
                        parseJSONToEvents(jsonObject, new PublicEventRepository.EventSearchCallback() {
                            @Override
                            public void onEventsFound(ArrayList<PublicEvent> events) {
                                adapter.setEvents(events);
                                collapseSearch();
                                adapter.notifyDataSetChanged();
                                setOnItemClickListener();
                                eventsRV.setAdapter(adapter);
                            }
                        });
                    } catch (Exception exception) {
                        Log.d("parseJson Error", "Error:", exception);
                    }
                    Log.d("Search Object", jsonObject.toString());
                }else{
                    Log.d("Search Object", "json null");
                }
            };
            @Override
            public boolean onQueryTextSubmit(String s) {
                index.searchAsync(new com.algolia.search.saas.Query(s), completionHandler);
                return true;

            }

            @Override
            public boolean onQueryTextChange(String s) {
                if(startUp){
                    startUpTV.setVisibility(View.GONE);
                }
                index.searchAsync(new com.algolia.search.saas.Query(s), completionHandler);
                return true;

            }
        });
        searchButton.setOnClickListener(v -> {
            if(searchIsValid()){
                composeSearch(new searchEvents() {
                    @Override
                    public void onEventsFound(ArrayList<PublicEvent> events) {
                        if (events != null && !events.isEmpty()) {
                            for (PublicEvent event : events) {
                                Log.d("All Search Events", event.toString());
                            }
                            adapter.setEvents(events);
                            collapseSearch();
                            adapter.notifyDataSetChanged();
                            setOnItemClickListener();
                            eventsRV.setAdapter(adapter);
                        }
                    }

                    @Override
                    public void onNoEventsFound() {
                        Log.d("Search", "No events found");
                        adapter.setEvents(new ArrayList<>());
                        noEventsTV.setVisibility(View.VISIBLE);
                        displaySearch();
                        adapter.notifyDataSetChanged();
                        setOnItemClickListener();
                        searchButton.setIcon(Objects.requireNonNull(ContextCompat.getDrawable(ContextCompat.createDeviceProtectedStorageContext(getContext()), R.drawable.search)));
                        eventsRV.setAdapter(adapter);
                    }
                });
            }else{
                Log.d("Search", "Search Invalid");
                Toast.makeText(getContext(), "Search Invalid", Toast.LENGTH_SHORT).show();
            }
            if(startUp){
                startUpTV.setVisibility(View.GONE);
            }
        });
        createDatePicker.setOnClickListener(v -> {
            MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Date")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();
            materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
                @Override
                public void onPositiveButtonClick(Long selection) {
                    date = new Date(selection);
                    dateTextView.setText(convertDateToFormattedString(date));
                    dateTextView.setVisibility(View.VISIBLE);
                }
            });
            materialDatePicker.show(getActivity().getSupportFragmentManager(), "tag");
        });

        dropdown.setOnClickListener(v -> {
            if(advancedSearch){
                collapseSearch();
                advancedSearch = false;
            }else{
                displaySearch();
                advancedSearch = true;
            }
        });

    }



    private void setOnItemClickListener() {
        adapter.setOnItemClickListener(event -> {
            PublicEventViewFragment fragment = new PublicEventViewFragment();
            Bundle args = new Bundle();
            Log.d("onItemClick", event.toString());
            args.putString("eventId", event.getEventId());

            fragment.setArguments(args);
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainerView, fragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void collapseSearch() {
        noEventsTV.setVisibility(View.GONE);
        minPriceTV.setVisibility(View.GONE);
        maxPriceTV.setVisibility(View.GONE);
        minPriceSlider.setVisibility(View.GONE);
        maxPriceSlider.setVisibility(View.GONE);
        tagsEditText.setVisibility(View.GONE);
        eventGenreTV.setVisibility(View.GONE);
        eventGenreSpinner.setVisibility(View.GONE);
        searchButton.setVisibility(View.GONE);
        createDatePicker.setVisibility(View.GONE);
        searchButton.setIcon(Objects.requireNonNull(ContextCompat.getDrawable(ContextCompat.createDeviceProtectedStorageContext(getContext()), R.drawable.ic_arrow_down)));
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noEventsTV.setVisibility(View.GONE);
                displaySearch();
                setOnClickListeners();
            }
        });
    }

    private void displaySearch(){
        minPriceTV.setVisibility(View.VISIBLE);
        maxPriceTV.setVisibility(View.VISIBLE);
        minPriceSlider.setVisibility(View.VISIBLE);
        maxPriceSlider.setVisibility(View.VISIBLE);
        searchButton.setVisibility(View.VISIBLE);
        tagsEditText.setVisibility(View.VISIBLE);
        eventGenreTV.setVisibility(View.VISIBLE);
        eventGenreSpinner.setVisibility(View.VISIBLE);
        createDatePicker.setVisibility(View.VISIBLE);

        searchButton.setIcon(Objects.requireNonNull(ContextCompat.getDrawable(ContextCompat.createDeviceProtectedStorageContext(getContext()), R.drawable.search)));
    }

    private void composeSearch(searchEvents callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance(); // Ensure Firestore is initialized
        CollectionReference eventsRef = db.collection("PublicEvents");
        Query query = eventsRef;
        double minPrice = minPriceSlider.getValue();
        double maxPrice = maxPriceSlider.getValue();
        query = query.whereGreaterThanOrEqualTo("priceDouble", minPrice)
                .whereLessThanOrEqualTo("priceDouble", maxPrice);

        // Adjust the query based on the selected event genre, if any
        Object selectedItem = eventGenreSpinner.getSelectedItem();
        if (selectedItem != null) {
            EventGenre eventGenre;
            String genreString = selectedItem.toString();
            if(!genreString.equals("Any")){
                for (EventGenre genre : EventGenre.values()) {
                    if (genre.getDisplayName().equals(genreString)) {
                        eventGenre = genre;
                        query = query.whereEqualTo("eventGenre", eventGenre);
                        break;
                    }
                }
            }
        }

        ArrayList<String> tags = (ArrayList<String>) tagsEditText.getTags();
        if (tags != null && !tags.isEmpty() && tags.size() <= 10) {
            query = query.whereArrayContainsAny("tags", tags);
        } else if (tags != null && tags.size() > 10) {
            Log.d("Search", "Firestore 'whereIn'/'whereArrayContainsAny' queries support up to 10 elements. Implement additional logic to handle more tags.");
        }


        if(date!=null){
            query = query.whereEqualTo("date", date);
        }
        // Execute the query
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ArrayList<PublicEvent> events = new ArrayList<>();
                if(!task.getResult().isEmpty()){
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        PublicEvent event = getPublicEventFromDocument(document);
                        event.setEventId(document.getId());
                        events.add(event);
                    }
                    callback.onEventsFound(events);
                }else{
                    Log.d("Search", "No events found");
                    callback.onNoEventsFound();
                }

            } else {
                Log.d("Search", "Error getting documents: ", task.getException());
            }
        }).addOnFailureListener(e -> {
            Log.d("Search", "Error: "+ e.getMessage());
        }
        );

    }




    private boolean searchIsValid() {
        boolean isValid = true;
        if(minPriceSlider.getValue()>maxPriceSlider.getValue()){
            isValid = false;
            Toast.makeText(getContext(), "Minimum Price must be lesser than Maximum", Toast.LENGTH_SHORT).show();
        }
        return isValid;
    }

    public interface searchEvents{
        void onEventsFound(ArrayList<PublicEvent> events);
        void onNoEventsFound();
    }

}