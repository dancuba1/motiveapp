package com.example.calendarapp;

import static android.view.View.GONE;
import static com.example.calendarapp.OnlineDb.PublicEventRepository.getEventById;
import static com.example.calendarapp.OnlineDb.SavedEventRepository.notSaved;
import static com.example.calendarapp.OnlineDb.SavedEventRepository.removeSavedEvent;
import static com.example.calendarapp.OnlineDb.SavedEventRepository.saveEvent;
import static com.example.calendarapp.OnlineDb.UserRepository.addAttendeeToEvent;
import static com.example.calendarapp.OnlineDb.UserRepository.getFollowers;
import static com.example.calendarapp.OnlineDb.UserRepository.getFollowing;
import static com.example.calendarapp.OnlineDb.UserRepository.notAttended;
import static com.example.calendarapp.OnlineDb.PublicEventRepository.publicToPrivate;
import static com.example.calendarapp.OnlineDb.UserRepository.getAttendeePreviews;
import static com.example.calendarapp.OnlineDb.UserRepository.removeAttendeeFromEvent;
import static com.example.calendarapp.Utils.DateUtils.getDateWithDay;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.calendarapp.Calendar.CalendarAdapter;
import com.example.calendarapp.Calendar.CalendarPreviewAdapter;
import com.example.calendarapp.Calendar.DayViewFragment;
import com.example.calendarapp.Calendar.DividerItemDecoration;
import com.example.calendarapp.EventObjects.PrivateEvent;
import com.example.calendarapp.EventObjects.PublicEvent;
import com.example.calendarapp.LocalDb.EventViewModel;
import com.example.calendarapp.OnlineDb.AttendeeAdapter;
import com.example.calendarapp.OnlineDb.AttendeePreview;
import com.example.calendarapp.OnlineDb.PublicEventRepository;
import com.example.calendarapp.OnlineDb.UserRepository;
import com.example.calendarapp.Utils.LocalDateTimeConverter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PublicEventViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PublicEventViewFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private PublicEvent event;
    private PrivateEvent privateEvent;
    private EventViewModel eventViewModel;
    private ScrollView infoScrollView;
    private Bundle args;
    private Uri bannerImage;
    private ImageView bannerImageView, locationImage, shareButton, thumbnailImage1, thumbnailImage2, thumbnailImage3, thumbnailImage4, profilePic;
    private MaterialTextView headingTextView, linksTextView, locationTextView, descriptionTextView, priceTextView, yourCalendarTV;
    private TextView dateTextView, ellipsisIndicator;
    private RecyclerView attendeesRecyclerView;
    private String eventId;
    private MaterialButton attendButton;
    private MaterialCardView calendarCardView;
    private boolean attendance, isSaved;
    private GridLayout pictureCollageLayout;
    private RecyclerView yourCalendarPreviewRV;
    private LinearLayout descriptionImagesLayout;
    private FirebaseUser user;
    private CalendarAdapter calendarAdapter;
    private FragmentManager fragmentManager;
    private CalendarPreviewAdapter calendarPreviewAdapter;
    private MaterialCardView bannerHeading;

    public PublicEventViewFragment() {
        // Required empty public constructor
    }

    public static PublicEventViewFragment newInstance(String param1, String param2) {
        PublicEventViewFragment fragment = new PublicEventViewFragment();
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstance) {
        super.onViewCreated(view, savedInstance);
        setOnClickListeners();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_public_event, container, false);
        init(view);


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        args = getArguments();
        //if found an event load in view
        if(args != null){
            eventId = args.getString("eventId");
            Log.d("Public Event View Args", eventId, null);
            if(eventId != null){
                getEventById(eventId, new PublicEventRepository.EventFetchListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onEventFetched(PublicEvent fetchedEvent) {
                        //Fetch an event
                        event = fetchedEvent;
                        setCalendarView();
                        event.setEventId(eventId);
                        isOwnUsersEvent(event.getUid(), user.getUid());
                        setViews();
                    }
                });
            }
        }
        return view;
    }

    private void init(View view){
        user = FirebaseAuth.getInstance().getCurrentUser();
        bannerHeading = view.findViewById(R.id.bannerHeading);
        fragmentManager = getActivity().getSupportFragmentManager();
        bannerImageView = view.findViewById(R.id.bannerImage);
        calendarCardView = view.findViewById(R.id.calendarCardView);
        shareButton = view.findViewById(R.id.shareButton);
        headingTextView= view.findViewById(R.id.headingTextView);
        linksTextView = view.findViewById(R.id.linksTextView);
        priceTextView = view.findViewById(R.id.priceTextView);
        locationTextView = view.findViewById(R.id.locationTextView);
        locationImage = view.findViewById(R.id.locationImageView);
        attendButton = view.findViewById(R.id.attendButton);
        descriptionTextView = view.findViewById(R.id.descriptionTextView);
        thumbnailImage1 = view.findViewById(R.id.thumbnailImageView1);
        thumbnailImage2 = view.findViewById(R.id.thumbnailImageView2);
        thumbnailImage3 = view.findViewById(R.id.thumbnailImageView3);
        thumbnailImage4 = view.findViewById(R.id.thumbnailImageView4);
        dateTextView = view.findViewById(R.id.dateTextView);
        attendeesRecyclerView = view.findViewById(R.id.attendeesRecyclerView);
        profilePic = view.findViewById(R.id.profilePic);
        ellipsisIndicator = view.findViewById(R.id.ellipsisIndicator);
        yourCalendarPreviewRV = view.findViewById(R.id.yourCalendarPreview);
        DividerItemDecoration horiDividerItemDecoration = new DividerItemDecoration(getContext(), R.drawable.divider_line, LinearLayoutManager.HORIZONTAL);
        DividerItemDecoration vertDividerItemDecoration = new DividerItemDecoration(getContext(), R.drawable.divider_line, LinearLayoutManager.VERTICAL);
        yourCalendarPreviewRV.addItemDecoration(vertDividerItemDecoration);
        yourCalendarPreviewRV.addItemDecoration(horiDividerItemDecoration);
        yourCalendarTV = view.findViewById(R.id.yourCalendarTextView);
        descriptionImagesLayout = view.findViewById(R.id.descriptionImagesLayout);
        infoScrollView =view.findViewById(R.id.infoScrollView);
    }

    private void setOnClickListeners() {
        locationImage.setOnClickListener(v -> {
            Log.d("Location clicked", event.getLocation());
            if (event.getLocation() != null && !event.getLocation().isEmpty()) {
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(event.getLocation()));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);

            }
        });
        shareButton.setOnClickListener(v -> {
            String url = event.getLink();
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this event: " + url);

            startActivity(Intent.createChooser(shareIntent, "Share Event"));
        });

        linksTextView.setOnClickListener(v -> {
            String link = event.getLink();
            if (link != null && !link.isEmpty()) {
                Log.d("Link clicked", "link found " + link);
                Uri webpage = Uri.parse(link.startsWith("http://") || link.startsWith("https://") ? link : "http://" + link);
                Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
                try{
                    startActivity(Intent.createChooser(webIntent, "Website link"));
                } catch (Exception e){
                    Log.d("Link clicked", "No activity to handle web intent", e);
                }
            }else{
                Log.d("Link clicked", "link empty");
            }
        });

    }

    private void setViews() {
        if(event.getBannerUrl()!=null){
            Log.d("bannerImage", "bannerImage found", null);
            loadImageIntoView(event.getBannerUrl(), 0);
        }else{
            Log.d("bannerImage", "bannerImage not found", null);

        }
        if(event.getImageUrls()!=null){
            for (int i=0;i<event.getImageUrls().size();i++){
                loadImageIntoView(event.getImageUrls().get(i), i+1);
            }
            if(event.getImageUrls().size()<4) {
                makeImagesInvisible(event.getImageUrls().size());
            }
        }

        dateTextView.setText(LocalDateTimeConverter.convertDateToFormattedString(event.getDate()));
        headingTextView.setText(event.getTitle());
        if(event.getPrice().equalsIgnoreCase("£0.00")){
            priceTextView.setText("FREE");
        }else{
            priceTextView.setText(event.getPrice());
        }
        linksTextView.setText(event.getLink());
        locationTextView.setText(event.getLocation());
        descriptionTextView.setText(event.getDescription());


        attendeesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        attendeesRecyclerView.setHasFixedSize(true);
        getFollowing(user.getUid(), new UserRepository.UsersCallback() {
            @Override
            public void usersRetrieved(ArrayList<User> users) {
                setAttendeeAdapter(users);
            }

            @Override
            public void usersNotRetrieved() {
                setNoFollowing();
            }
        });


    }

    private void setNoFollowing() {
    }

    private void setAttendeeAdapter(ArrayList<User> users) {
        UserRepository.AttendeePreviewCallback attendeePreviewCallback = attendeePreviews -> {
            int extraAttendees = 0;
            ArrayList<AttendeePreview> threePreviews = new ArrayList<>();
            for(int i=0; i<Math.min(3, attendeePreviews.size()); i++){
                threePreviews.add(attendeePreviews.get(i));
            }
            if(attendeePreviews.size()>3){
                extraAttendees = attendeePreviews.size() - 3;
            }
            AttendeeAdapter attendeeAdapter = new AttendeeAdapter(getContext(), threePreviews);
            attendeeAdapter.notifyDataSetChanged();
            Log.d("attendeePreview", attendeePreviews.toString());
            if(attendeePreviews.size()>3){
                ellipsisIndicator.setVisibility(View.VISIBLE);
                if(extraAttendees == 1){
                    ellipsisIndicator.setText("+" + String.valueOf(extraAttendees) + " person you follow");
                }else{
                    ellipsisIndicator.setText("+" + String.valueOf(extraAttendees) + " people you follow");
                }
            }
            attendeeAdapter.setOnItemClickListener(new AttendeeAdapter.OnItemClickListener() {
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

            attendeesRecyclerView.setAdapter(attendeeAdapter);

        };

        if(users!=null) {
            ArrayList<String> followingIds = new ArrayList<>();
            for (int i = 0; i < users.size(); i++) {
                followingIds.add(users.get(i).getUid());
            }
            getAttendeePreviews(event, attendeePreviewCallback, user.getUid(), followingIds);
        }
    }

    private void isOwnUsersEvent(String eventUid, String userUid) {
        if(Objects.equals(eventUid, userUid)){   //if event is the users
            attendButton.setText("Edit");
            calendarCardView.setVisibility(View.GONE);

            //sets the event information and banner to take up the screen properly
            LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    2.2f
            );
            LinearLayout.LayoutParams bannerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1.3f
            );

            infoScrollView.setLayoutParams(infoParams);
            bannerHeading.setLayoutParams(bannerParams);
            profilePic.setVisibility(GONE);
            attendButton.setIcon(Objects.requireNonNull(ContextCompat.getDrawable(ContextCompat.createDeviceProtectedStorageContext(getContext()), R.drawable.ic_edit)));
            Log.d("setAttendButton", "Edit");
            yourCalendarTV.setVisibility(GONE);
            yourCalendarPreviewRV.setVisibility(GONE);
            setAttendEditOnClickListener(0);
        }else{      //if not the users event
            setProfilePic(eventUid); //set event owners profile pic
            setAttendance(userUid);
            setSavedState(userUid);
            profilePic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle args = new Bundle();
                    args.putString("userId", event.getUid());
                    ProfileFragment profileFragment = new ProfileFragment();
                    profileFragment.setArguments(args);
                    fragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainerView, profileFragment)
                            .addToBackStack(null)
                            .commit();
                }
            });
        }
    }

    private void setSavedState(String userUid) {
        notSaved(eventId, userUid, isNotSaved -> {
        if (isNotSaved) {
                isSaved = false;
            } else {
                isSaved =true;
                attendButton.setIcon(Objects.requireNonNull(ContextCompat.getDrawable(ContextCompat.createDeviceProtectedStorageContext(getContext()), R.drawable.tick_symbol)));
                attendButton.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
                Log.d("setAttendButton", "attending");
                setAttendEditOnClickListener(2);
            }
        });
    }

    private void setAttendance(String userUid) {
        notAttended(eventId, userUid, isNotAttending -> {
            if (isNotAttending) {
                attendance = false;
                Log.d("setAttendButton", "not attending");
                setAttendEditOnClickListener(1);
            } else {
                attendance=true;
                attendButton.setIcon(Objects.requireNonNull(ContextCompat.getDrawable(ContextCompat.createDeviceProtectedStorageContext(getContext()), R.drawable.tick_symbol)));
                attendButton.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
                attendButton.setText("Attending");
                Log.d("setAttendButton", "attending");
                setAttendEditOnClickListener(2);
            }
        });
    }

    private void setAttendEditOnClickListener(int code) {
        if(code==0){
            attendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //swap to addedit
                    Bundle args = new Bundle();
                    args.putString("eventId", event.getEventId());
                    AddEditPublicFragment addEditFragment = new AddEditPublicFragment();
                    addEditFragment.setArguments(args);
                    fragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainerView, addEditFragment)
                            .addToBackStack("publicEventView")
                            .commit();


                }
            });
        }/*else if(code == 1){
            attendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAttendDialog(0);
                }
            });
        }
        */
        else{
            attendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDialogWithCheckboxes();
                    //showAttendDialog(1);
                }
            });
        }
    }

    private void showDialogWithCheckboxes() {
        boolean[] initialCheckedStates = {attendance, isSaved};

        // Clone to avoid modifying the initial state
        final boolean[] checkedItems = initialCheckedStates.clone();


        //Create dialog box
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Set Attendance or Save Event:");


        //checkedItems set to new values dependent on the choices of user
        builder.setMultiChoiceItems(new CharSequence[]{"Add to Private Calendar", "Save Event Publically"}, checkedItems,
                (dialog, which, isChecked) -> checkedItems[which] = isChecked);


        //must confirm choices
        builder.setPositiveButton("Confirm", (dialog, id) -> handleAttendanceSavedEventChanges(initialCheckedStates, checkedItems));
        builder.setNegativeButton("Cancel", (dialog, id) -> {});

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void handleAttendanceSavedEventChanges(boolean[] initialStates, boolean[] finalStates) {
        //Method for handling checkbox inputs, whether attendance is changed or if the event being saved is changed
        for (int i = 0; i < initialStates.length; i++) {
            if (initialStates[i] != finalStates[i]) { // Checks if either is changed
                if (initialStates[i]) { // Changed
                    switch (i) {
                        case 0:
                            //Attendance unset
                            attendButton.setIcon(Objects.requireNonNull(ContextCompat.getDrawable(ContextCompat.createDeviceProtectedStorageContext(getContext()), R.drawable.ic_add)));
                            attendButton.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                            attendButton.setText("Attend");
                            removeEventFromPrivateCalendar();
                            removeAttendeeFromEvent(eventId, FirebaseAuth.getInstance().getUid());
                            attendance = false;
                            Toast.makeText(getContext(), "Attendance unset", Toast.LENGTH_SHORT).show();
                            break;
                        case 1:
                            // Saved Event unset
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            removeSavedEvent(eventId, user.getUid());
                            isSaved = false;
                            Toast.makeText(getContext(), "Event unsaved", Toast.LENGTH_SHORT).show();
                            break;
                    }
                } else { // False to True
                    switch (i) {
                        case 0:
                            //Attendance set
                            attendButton.setIcon(Objects.requireNonNull(ContextCompat.getDrawable(ContextCompat.createDeviceProtectedStorageContext(getContext()), R.drawable.tick_symbol)));
                            attendButton.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
                            attendButton.setText("Attending");
                            Log.d("setAttendButton", "attending");
                            addEventToPrivateCalendar();
                            addAttendeeToEvent(eventId);
                            attendance = true;
                            Toast.makeText(getContext(), "Attendance set", Toast.LENGTH_SHORT).show();
                            break;
                        case 1:
                            //Saved Event set
                            saveEvent(eventId);
                            isSaved = true;
                            Toast.makeText(getContext(), "Event saved", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }
        }
    }

    private void showAttendDialog(int code) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        if(code == 0){
            builder.setTitle("ATTEND")
                    .setMessage("Are you sure you would like to add this event to your private calendar?")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            setAttendEditOnClickListener(2);
                            attendButton.setIcon(Objects.requireNonNull(ContextCompat.getDrawable(ContextCompat.createDeviceProtectedStorageContext(getContext()), R.drawable.ic_cross)));
                            attendButton.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
                            attendButton.setText("Attend");
                            Log.d("setAttendButton", "attending");
                            addEventToPrivateCalendar();
                            addAttendeeToEvent(eventId);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    })
                    .show();
        }else{
            builder.setTitle("Withdraw")
                    .setMessage("Are you sure you would like to remove this event from your private calendar?")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            setAttendEditOnClickListener(1);
                            attendButton.setIcon(Objects.requireNonNull(ContextCompat.getDrawable(ContextCompat.createDeviceProtectedStorageContext(getContext()), R.drawable.ic_add)));
                            attendButton.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                            attendButton.setText("Attend");
                            setAttendEditOnClickListener(1);
                            removeEventFromPrivateCalendar();
                            calendarPreviewAdapter.notifyDataSetChanged();
                            removeAttendeeFromEvent(eventId, FirebaseAuth.getInstance().getUid());
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    })
                    .show();
        }


    }

    private void removeEventFromPrivateCalendar() {
        privateEvent = publicToPrivate(event);
        privateEvent.setEventId(eventId);
        eventViewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);
        Log.d("Event view model", event.toString(), null);
        eventViewModel.deleteEventById(privateEvent.getEventId());
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        eventViewModel.getAllEvents(uid).observe(requireActivity(), events -> {
            if (events != null) {
                for (int i = 0; i < events.size(); i++) {
                    Log.d("database", events.get(i).toString());
                }
            }else{
                Log.d("database", "no events in database", null);
            }
        });

    }

    private void addEventToPrivateCalendar() {
        try{
            privateEvent = publicToPrivate(event);
            privateEvent.setEventId(eventId);
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String uid = user.getUid();
            privateEvent.setUid(uid);
            eventViewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);
            Log.d("Event view model", event.toString(), null);
            eventViewModel.insert(privateEvent);
            calendarPreviewAdapter.notifyDataSetChanged();
            eventViewModel.getAllEvents(uid).observe(requireActivity(), events -> {
                if (events != null) {
                    for (int i = 0; i < events.size(); i++) {
                        Log.d("database", events.get(i).toString());
                    }
                }else{
                    Log.d("database", "no events in database", null);
                }
            });

        }catch (Exception e){
            Log.e("Adding event to private calendar", e.getMessage(), null);
        }
    }

    private void setProfilePic(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String profilePicUrl = document.getString("profileImage");
                        loadImageIntoView(profilePicUrl, 5);
                    } else {
                        Log.d("Firestore", "No user profile found");
                    }
                } else {
                    // Handle the failure to get the document
                    Log.d("Firestore", "get failed with ", task.getException());
                }
            }
        });

    }

    private void setCalendarView() {
        Date date = event.getDate();
        ArrayList<String> daysAroundDate = daysAroundDate(date);
        String monthYear = monthYearFromDate(date);
        CalendarAdapter.OnItemListener itemListener = (position, dayText) -> {
            if (!dayText.equals("")) {
                openDayViewFragment(getDateWithDay(event.getDate(), dayText));
            }


        };
        Application application = requireActivity().getApplication();
        calendarPreviewAdapter = new CalendarPreviewAdapter(daysAroundDate, itemListener, monthYear, getContext(), date, application, getViewLifecycleOwner());
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        yourCalendarPreviewRV.setLayoutManager(layoutManager);
        yourCalendarPreviewRV.setNestedScrollingEnabled(true);
        yourCalendarPreviewRV.setAdapter(calendarPreviewAdapter);
    }

    private void openDayViewFragment(Date date) {
        DayViewFragment dayViewFragment = new DayViewFragment();
        Bundle args = new Bundle();
        args.putSerializable("selectedDate", date);
        dayViewFragment.setArguments(args);
        fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, dayViewFragment)
                .addToBackStack(null)
                .commit();
    }

    private ArrayList<String> daysAroundDate(Date date) {
        ArrayList<String> daysAroundArray = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        // Move the calendar to 3 days before the provided date
        calendar.add(Calendar.DAY_OF_MONTH, -1);

        // Use "d" to format the date as just the day of the month
        SimpleDateFormat sdf = new SimpleDateFormat("d", Locale.getDefault());

        // Add the range of 7 days to the array (3 days before, the day of, and 3 days after)
        for (int i = 0; i < 3; i++) {
            daysAroundArray.add(sdf.format(calendar.getTime()));
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        Log.d("Days around date", String.valueOf(daysAroundArray));
        return daysAroundArray;
    }

    private String monthYearFromDate(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        return formatter.format(date);
    }

    private void loadImageIntoView(String imageUrl, int code){
        switch(code){
            case 0:
                Glide.with(this)
                        .load(imageUrl)
                        .centerCrop()
                        .into(bannerImageView);
                break;
            case 1:
                Glide.with(this)
                        .load(imageUrl)
                        .into(thumbnailImage1);
                break;
            case 2:
                Glide.with(this)
                        .load(imageUrl)
                        .into(thumbnailImage2);
                break;
            case 3:
                Glide.with(this)
                        .load(imageUrl)
                        .into(thumbnailImage3);
            case 4:
                Glide.with(this)
                        .load(imageUrl)
                        .into(thumbnailImage4);
            case 5:
                Glide.with(this)
                        .load(imageUrl)
                        .circleCrop()
                        .into(profilePic);
        }


    }
    private void makeImagesInvisible(int number) {
        for(int i=4;i>number; i--){
            switch (i) {
                case 4:
                    thumbnailImage4.setVisibility(View.GONE); // or View.GONE
                    break;
                case 3:
                    thumbnailImage3.setVisibility(View.GONE); // or View.GONE
                    break;
                case 2:
                    thumbnailImage2.setVisibility(View.GONE); // or View.GONE
                    break;
            }
        }
    }

}