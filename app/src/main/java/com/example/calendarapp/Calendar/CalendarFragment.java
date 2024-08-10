package com.example.calendarapp.Calendar;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import androidx.core.util.Pair;

import com.example.calendarapp.Utils.DateUtils;
import com.example.calendarapp.LocalDb.EventViewModel;
import com.example.calendarapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


/**
 * A simple {@link Fragment} subclass.
 * Use the  factory method to
 * create an instance of this fragment.
 */
public class CalendarFragment extends Fragment implements CalendarAdapter.OnItemListener {
    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;
    private LocalDate selectedDate;
    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;
    private boolean isCalendarFragmentVisible;
    private EventViewModel eventViewModel;
    private CalendarAdapter calendarAdapter;
    private LinearLayout daysInWeekLayout;
    private FirebaseUser user;
    private FloatingActionButton prevMonthButton, nextMonthButton;
    private DividerItemDecoration horiDividerItemDecoration, vertDividerItemDecoration;
    private FragmentContainerView yearViewFragmentContainer;
    private FragmentManager fragmentManager;

    private ViewModelStoreOwner viewModelStoreOwner;
    public CalendarFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);


        init(view);
        setInitialRV();
        setMonthView();


        setOnClickListeners();




        return view;
    }



    private void init(View view) {
        //initialise views and fragment manager

        fragmentManager = requireActivity().getSupportFragmentManager();
        calendarRecyclerView = view.findViewById(R.id.calendarRecyclerView);
        user = FirebaseAuth.getInstance().getCurrentUser();
        horiDividerItemDecoration = new DividerItemDecoration(getContext(), R.drawable.divider_line, LinearLayoutManager.HORIZONTAL);
        vertDividerItemDecoration = new DividerItemDecoration(getContext(), R.drawable.divider_line, LinearLayoutManager.VERTICAL);

        monthYearText = view.findViewById(R.id.month);
        daysInWeekLayout = view.findViewById(R.id.daysOfWeek);


        prevMonthButton = view.findViewById(R.id.prevMonthButton);
        nextMonthButton = view.findViewById(R.id.nextMonthButton);

        // Set initial date
        selectedDate = LocalDate.now();



    }

    private void setOnClickListeners() {
        monthYearText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setYearView(selectedDate.getYear());
            }
        });

        prevMonthButton.setOnClickListener(this::prevMonth);
        nextMonthButton.setOnClickListener(this::nextMonth);
    }
    private ArrayList<String> generateMonthsForYear(int year) {
        // Customize this method to generate a list of month names for the given year
        ArrayList<String> monthsInYear = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            monthsInYear.add(getMonthName(i)); //+ " " + year);
        }
        return monthsInYear;
    }
    private String getMonthName(int month) {
        // Customize this method to get the name of the month based on the month number
        // For simplicity, using a basic array here; consider using a DateFormatSymbols instance
        String[] monthNames = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        return monthNames[month - 1];
    }

    private void setYearView(int year) {
        // set a view of a year at the top, and clickable months
        ArrayList<String> monthsInYear = generateMonthsForYear(year);
        Log.d("Setting year view" ,"set");

        //get rid of 2 different item decorations
        calendarRecyclerView.invalidateItemDecorations();
        calendarRecyclerView.invalidateItemDecorations();
        calendarRecyclerView.removeItemDecoration(horiDividerItemDecoration);
        calendarRecyclerView.removeItemDecoration(vertDividerItemDecoration);


        daysInWeekLayout.setVisibility(View.GONE);
        Log.d("months in year", String.valueOf(monthsInYear));
        // Set up RecyclerView
        MonthsInYearAdapter monthsInYearAdapter = new MonthsInYearAdapter();
        monthsInYearAdapter.setMonthTexts(monthsInYear);// Pass your item click listener
        monthsInYearAdapter.setYear(year);
        monthsInYearAdapter.setContext(requireContext());
        monthsInYearAdapter.setOnMonthClickListener((month, year1) -> {
            // Sets to the first day of the clicked month and year
            selectedDate = LocalDate.of(year1, month, 1);
            Log.d("Selected Date", selectedDate.toString());
            setMonthView();
        });
        calendarRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 3));

        calendarRecyclerView.setAdapter(monthsInYearAdapter);

        monthYearText.setText(String.valueOf(year));
        setYearClickListeners();

    }

    private void setYearClickListeners() {
        monthYearText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMonthView();
                setInitialRV();
            }
        });
        prevMonthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedDate = selectedDate.minusYears(1);
                Log.d("selectedDate", String.valueOf(selectedDate.getYear()));
                setYearView(selectedDate.getYear());
            }
        });
        nextMonthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedDate = selectedDate.plusYears(1);
                Log.d("selectedDate", String.valueOf(selectedDate.getYear()));
                setYearView(selectedDate.getYear());
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if(getArguments() != null && getArguments().containsKey("selectedDate")) {
            LocalDate restoredDate = (LocalDate) getArguments().getSerializable("selectedDate");
            selectedDate = restoredDate != null ? restoredDate : selectedDate;
        }
        //setMonthView();
    }



    private void setInitialRV() {
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(requireContext(), 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.addItemDecoration(horiDividerItemDecoration);
        calendarRecyclerView.addItemDecoration(vertDividerItemDecoration);
    }
    private void setMonthView() {
        String monthYear = monthYearFromDate(selectedDate);
        prevMonthButton.setOnClickListener(this::prevMonth);
        nextMonthButton.setOnClickListener(this::nextMonth);
        daysInWeekLayout.setVisibility(View.VISIBLE);
        Log.d("Set month view", monthYear, null);
        monthYearText.setText(monthYear);
        ArrayList<String> daysInMonth = daysInMonthArray(selectedDate);

        //Create main calendar month view, displaying all days in a month

        calendarAdapter = new CalendarAdapter(daysInMonth, this, monthYear, getContext(), calendarRecyclerView);

        Pair<Date, Date> startAndEndDates = DateUtils.getStartAndEndDateOfMonth(monthYear);
        if (startAndEndDates != null) {
            Date startOfMonth = startAndEndDates.first;
            Date endOfMonth = startAndEndDates.second;
            Log.d("Are start and end dates found", "yes " + startOfMonth.toString(), null);

            // Use ViewModel to get events for that year
            eventViewModel = new EventViewModel(getActivity().getApplication());
            eventViewModel.getEventsForMonth(startOfMonth, endOfMonth, user.getUid()).observe(getViewLifecycleOwner(), privateEvents -> {
                for(int i=0;i<privateEvents.size();i++){
                    Log.d("Event found in month", privateEvents.get(i).toString(), null);
                }
                calendarAdapter.setMonthlyEvents(privateEvents);});




        } else {
            Log.d("Are start and end dates found", "no", null);
        }



        calendarRecyclerView.setAdapter(calendarAdapter);

        //must reset onclick listener
        monthYearText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setYearView(selectedDate.getYear());
            }
        });
    }

    //gets the days in a month of a LocalDate object
    private ArrayList<String> daysInMonthArray(LocalDate date) {
        ArrayList<String> daysInMonthArray = new ArrayList<>();
        YearMonth yearMonth = YearMonth.from(date);

        int daysInMonth = yearMonth.lengthOfMonth();

        LocalDate firstOfMonth = selectedDate.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();

        for(int i = 1; i <= 42; i++)
        {
            if(i <= dayOfWeek || i > daysInMonth + dayOfWeek)
            {
                daysInMonthArray.add("");
            }
            else
            {
                daysInMonthArray.add(String.valueOf(i - dayOfWeek));
            }
        }
        return  daysInMonthArray;
    }


    public static String monthYearFromDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        return date.format(formatter);
    }



    public void prevMonth(View view) {
        selectedDate = selectedDate.minusMonths(1);
        setMonthView();
    }


    public void nextMonth(View view) {
        selectedDate = selectedDate.plusMonths(1);
        setMonthView();
    }

    //takes to day view of date clicked
    @Override
    public void onItemClick(int position, String dayText) {
        if (!dayText.equals("")) {
            String message = "Selected Date " + dayText + " " + monthYearFromDate(selectedDate);
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            LocalDate unformattedDate = selectedDate.withDayOfMonth(Integer.parseInt(dayText));
            String formattedDate = formatDateAsYYYYMMDD(unformattedDate);
            openDayViewFragment(formattedDate);
            Log.d("dateText", formattedDate, null);
            Log.d("toastDate", message, null);
        }
    }
    private String formatDateAsYYYYMMDD(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return date.format(formatter);
    }




    public void openDayViewFragment(String selectedDate) {
        // Create a new instance of DayViewFragment and set the selected date as an argument
        DayViewFragment dayViewFragment = new DayViewFragment();
        Bundle bundle = new Bundle();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            //Set LocalDate to a Date object for parsing as an argument in the bundle sent to day view.
            date = dateFormat.parse(selectedDate);

            Log.d("Date", date.toString());
            // Put the Date object directly into the bundle
            bundle.putSerializable("selectedDate", date);

            LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE"); // "EEEE" gives the full day name
            String dayOfWeek = localDate.format(formatter);
            bundle.putString("dayOfWeek", dayOfWeek);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        dayViewFragment.setArguments(bundle);

        // Get the FragmentManager and start a transaction
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        if (isAdded()) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Bundle args = new Bundle();
            args.putSerializable("selectedDate", date);
            dayViewFragment.setArguments(args);

            Log.d("Reached openDayViewFragment", "openDayViewFragment", null);
            Log.d("FragmentState", "Before replace: " + fragmentManager.getBackStackEntryCount());

            try {
                fragmentTransaction.replace(R.id.fragmentContainerView, dayViewFragment);


                fragmentTransaction.addToBackStack(null);

                fragmentTransaction.commit();
                Log.d("Reached openDayViewFragment", "switch occurred", null);
                Log.d("FragmentState", "After replace: " + fragmentManager.getBackStackEntryCount());

            } catch (Exception e) {
                Log.d("Reached openDayViewFragment", "switch not occurred", null);
            }
        } else {
            Log.e("CalendarFragment", "Fragment not added to the activity");
        }


    }



}
