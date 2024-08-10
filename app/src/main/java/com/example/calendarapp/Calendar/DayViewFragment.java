package com.example.calendarapp.Calendar;

import static com.example.calendarapp.MainActivity.MainActivity.displayFragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calendarapp.AddEditPublicFragment;
import com.example.calendarapp.Utils.DayOfWeekComparator;
import com.example.calendarapp.EventObjects.Event;
import com.example.calendarapp.Utils.EventTimeComparator;
import com.example.calendarapp.LocalDb.EventViewModel;
import com.example.calendarapp.Utils.LocalDateTimeConverter;
import com.example.calendarapp.EventObjects.PrivateEvent;
import com.example.calendarapp.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DayViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

// Fragment for displaying the events for a day
public class DayViewFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private FloatingActionButton addEventButton;
    private MaterialCardView monday, tuesday, wednesday, thursday, friday, saturday, sunday;
    private EventViewModel eventViewModel;
    private ArrayList<PrivateEvent> eventsList;
    private TextView dateTextView;

    public DayViewFragment() {
        // Required empty public constructor
    }

    public static DayViewFragment newInstance(String param1, String param2) {
        DayViewFragment fragment = new DayViewFragment();
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



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_day_view, container, false);

        init(view);
        Bundle args = getArguments();
        setDate(args, view);

        return view;

    }

    private void setDate(Bundle args, View view) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // if a date is passed, then get all events for said date

        if (args != null && args.containsKey("selectedDate")) {
            Date selectedDate = (Date) args.getSerializable("selectedDate");
            Log.d("selectedDate dayView", String.valueOf(selectedDate), null);


            //instantiate adapter
            DayViewAdapter adapter = getEventsForDay(view);

            //get events for day
            eventViewModel.getEventsByDate(selectedDate, user.getUid()).observe(getViewLifecycleOwner(), new Observer<List<PrivateEvent>>() {
                @Override
                public void onChanged(List<PrivateEvent> events) {
                    Log.d("no. of events", String.valueOf(events.size()), null);
                    Log.d("DayView Events", events.toString(), null);
                    adapter.setEvents(events);

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(selectedDate);
                    int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                    dayOfWeekSwitch(dayOfWeek);

                }
            });

            // make events clickable
            setOnItemClickListener(adapter);

            //set date text
            String formattedDate = formatToCustomDate(selectedDate);
            dateTextView.setText(formattedDate);

            //set the click listeners for the days at the top
            setOnClickListeners(selectedDate);

        }
        else{
            Log.d("Args invalid", args.toString());
        }
    }

    private void init(View view){

        //initialise views
        monday = view.findViewById(R.id.mondayCardView);
        tuesday = view.findViewById(R.id.tuesdayCardView);
        wednesday = view.findViewById(R.id.wednesdayCardView);
        thursday = view.findViewById(R.id.thursdayCardView);
        friday = view.findViewById(R.id.fridayCardView);
        saturday = view.findViewById(R.id.saturdayCardView);
        sunday = view.findViewById(R.id.sundayCardView);
        dateTextView = view.findViewById(R.id.selectedDateTextView);
        addEventButton = view.findViewById(R.id.addEventButton);
    }
    private void dayOfWeekSwitch(int dayOfWeek) {

        //switch case for changing the highlighted day card at top
        switch (dayOfWeek) {
            case 1:
                sunday.setCardElevation(30);
                monday.setCardElevation(0);
                tuesday.setCardElevation(0);
                wednesday.setCardElevation(0);
                thursday.setCardElevation(0);
                friday.setCardElevation(0);
                saturday.setCardElevation(0);
                break;
            case 2:
                sunday.setCardElevation(0);
                monday.setCardElevation(30);
                tuesday.setCardElevation(0);
                wednesday.setCardElevation(0);
                thursday.setCardElevation(0);
                friday.setCardElevation(0);
                saturday.setCardElevation(0);
                break;

            case 3:
                sunday.setCardElevation(0);
                monday.setCardElevation(0);
                tuesday.setCardElevation(30);
                wednesday.setCardElevation(0);
                thursday.setCardElevation(0);
                friday.setCardElevation(0);
                saturday.setCardElevation(0);
                break;

            case 4:
                sunday.setCardElevation(0);
                monday.setCardElevation(0);
                tuesday.setCardElevation(0);
                wednesday.setCardElevation(30);
                thursday.setCardElevation(0);
                friday.setCardElevation(0);
                saturday.setCardElevation(0);
                break;

            case 5:
                sunday.setCardElevation(0);
                monday.setCardElevation(0);
                tuesday.setCardElevation(0);
                wednesday.setCardElevation(0);
                thursday.setCardElevation(30);
                friday.setCardElevation(0);
                saturday.setCardElevation(0);
                break;

            case 6:
                sunday.setCardElevation(0);
                monday.setCardElevation(0);
                tuesday.setCardElevation(0);
                wednesday.setCardElevation(0);
                thursday.setCardElevation(0);
                friday.setCardElevation(30);
                saturday.setCardElevation(0);
                break;

            case 7:
                sunday.setCardElevation(0);
                monday.setCardElevation(0);
                tuesday.setCardElevation(0);
                wednesday.setCardElevation(0);
                thursday.setCardElevation(0);
                friday.setCardElevation(0);
                saturday.setCardElevation(30);
                break;
        }
    }

    private void setOnClickListeners(Date selectedDate){
        monday.setOnClickListener(v -> {
            switchDayViewFragment(selectedDate, Calendar.MONDAY);
        });

        tuesday.setOnClickListener(v -> {
            switchDayViewFragment(selectedDate, Calendar.TUESDAY);
        });

        wednesday.setOnClickListener(v -> {
            switchDayViewFragment(selectedDate, Calendar.WEDNESDAY);
        });

        thursday.setOnClickListener(v -> {
            switchDayViewFragment(selectedDate, Calendar.THURSDAY);
        });

        friday.setOnClickListener(v -> {
            switchDayViewFragment(selectedDate, Calendar.FRIDAY);

        });

        saturday.setOnClickListener(v -> {
            switchDayViewFragment(selectedDate, Calendar.SATURDAY);
        });

        sunday.setOnClickListener(v -> {
            switchDayViewFragment(selectedDate, Calendar.SUNDAY);
        });

        //adding an event click listener
        addEventButton.setOnClickListener(v -> {

            Bundle bundle = new Bundle();
            bundle.putSerializable("selectedDate",selectedDate);
            AddEditFragment addEditFragment = new AddEditFragment();
            addEditFragment.setArguments(bundle);
            displayFragment(addEditFragment, getParentFragmentManager());

        });
    }


    //opens event in add edit upon being clicked
    private void setOnItemClickListener(DayViewAdapter adapter){
        adapter.setOnItemClickListener(event -> {
            Log.d("in event onclicklistener", "reached adapter listener");
            AddEditFragment addEditFragment = new AddEditFragment();

            Bundle bundle = new Bundle();

            bundle.putString("title", event.getTitle());
            bundle.putString("location", event.getLocation());
            bundle.putBoolean("hasAlert", event.isAlert());
            bundle.putSerializable("date", event.getDate());
            bundle.putBoolean("allDay", event.getAllDay());
            bundle.putInt("id", event.getId());
            String startTimeStr = LocalDateTimeConverter.fromLocalDateTime(event.getStartTime());
            bundle.putString("startTime", startTimeStr);
            String endTimeStr = LocalDateTimeConverter.fromLocalDateTime(event.getEndTime());
            bundle.putString("endTime", endTimeStr);
            bundle.putString("description", event.getDescription());
            if (event.getAlertTime() != null) {
                String alertTimeStr = LocalDateTimeConverter.fromLocalDateTime(event.getAlertTime());
                Log.d("alertTimeStr", alertTimeStr);
                bundle.putString("alertTime", alertTimeStr);
            } else {
                bundle.putString("alertTime", "none");
            }
            bundle.putSerializable("eventType", event.getEventType());

            addEditFragment.setArguments(bundle);

            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();

            fragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainerView, addEditFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    //gets events for the day
    private DayViewAdapter getEventsForDay(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.eventsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        final DayViewAdapter adapter = new DayViewAdapter(getContext());
        recyclerView.setAdapter(adapter);


        eventViewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);
        adapter.setViewModelStoreOwner(this);
        return adapter;

    }








    //sets custom formatting for date to be aesthetic

    public static String formatToCustomDate(Date inputDate) {
        // Create a SimpleDateFormat for parsing and formatting
        SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy");
        // Format the Date to the desired format
        return sdf.format(inputDate);
    }





    //switches to the appropriate day view
    public void switchDayViewFragment(Date currentDate, int dayOfWeek) {
        // Create a new instance of DayViewFragment and set the selected date as an argument
        DayViewFragment dayViewFragment = new DayViewFragment();
        Bundle bundle = new Bundle();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date newDate = DayOfWeekComparator.compare(currentDate, dayOfWeek);

        bundle.putSerializable("selectedDate", newDate);
        bundle.putInt("dayOfWeek", newDate.getDay());
        dayViewFragment.setArguments(bundle);

        // Get the FragmentManager and start a transaction
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        if (isAdded()) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            Log.d("Reached openDayViewFragment", "openDayViewFragment", null);
            // Replace the current fragment with DayViewFragment
            Log.d("FragmentState", "Before replace: " + fragmentManager.getBackStackEntryCount());

            try {
                fragmentTransaction.replace(R.id.fragmentContainerView, dayViewFragment);

                // Add the transaction to the back stack
                fragmentTransaction.addToBackStack(null);

                // Commit the transaction
                fragmentTransaction.commit();
                Log.d("Reached openDayViewFragment", "switch occurred", null);
                Log.d("FragmentState", "After replace: " + fragmentManager.getBackStackEntryCount());

            } catch (Exception e) {
                Log.d("Reached openDayViewFragment", "switch not occurred", null);
            }
        } else {
            // Handle the case where the fragment is not added to the activity
            Log.e("CalendarFragment", "Fragment not added to the activity");
        }


    }

}