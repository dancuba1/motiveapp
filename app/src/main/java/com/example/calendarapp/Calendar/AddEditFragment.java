package com.example.calendarapp.Calendar;

import static android.content.Context.ALARM_SERVICE;
import static com.example.calendarapp.Utils.LocalDateTimeConverter.convertLocalDateTimeToFormattedString;
import static com.example.calendarapp.Utils.LocalDateTimeConverter.localDateTimeToEpochMilli;
import static java.lang.String.format;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.NotificationManagerCompat;
import android.content.Context.*;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.calendarapp.EventObjects.EventType;
import com.example.calendarapp.LocalDb.EventViewModel;
import com.example.calendarapp.MainActivity.MainActivity;
import com.example.calendarapp.Utils.LocalDateTimeConverter;
import com.example.calendarapp.EventObjects.PrivateEvent;
import com.example.calendarapp.R;
import com.example.calendarapp.Utils.ReminderBroadcast;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddEditFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddEditFragment extends Fragment {


    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private String mParam1;
    private String mParam2;

    private MaterialTextView datePreviewTextView;
    private MaterialButton createDatePicker;

    private TextView startTimeTextView, endTimeTextView;
    private EditText titleEditText, locationEditText, descriptionEditText;
    private Spinner eventTypeSpinner, alertSpinner;
    private NumberPicker startHourPicker, startMinutePicker, endHourPicker, endMinutePicker;
    private SwitchCompat allDaySwitch;
    private TextInputLayout locationLayout, titleLayout, descriptionLayout;

    private Date date;
    private int id;
    private FloatingActionButton submitButton;


    private EventViewModel eventViewModel;
    private boolean update;

    public AddEditFragment() {
        // Required empty public constructor
    }


    public static AddEditFragment newInstance(String param1, String param2) {
        AddEditFragment fragment = new AddEditFragment();
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
        View view = inflater.inflate(R.layout.fragment_add_edit, container, false);

        //initialise views
        init(view);

        //get arguments, and if no arguments set the update indicator to false
        Bundle arguments = getArguments();
        if (arguments != null) {
            setDate(arguments);
        }else{
            update = false;
        }
        setOnClickListeners(view);
        return view;
    }

    private void setDate(Bundle arguments) {
        //if arguments are from long click on month view
        if(arguments.containsKey("day") && arguments.containsKey("monthYear")){
            //create the date string
            String selectedDateString = arguments.getString("day");
            String monthYearString = arguments.getString("monthYear");
            Log.d("monthYear", monthYearString, null);
            monthYearString = monthYearString.trim();

            //format string to a date
            SimpleDateFormat formatter = new SimpleDateFormat("d MMMM yyyy", Locale.ENGLISH);
            try {
                date = formatter.parse(selectedDateString + " " + monthYearString);
            } catch (ParseException e) {
                return;
            }

            //set views to the date
            datePreviewTextView.setText(LocalDateTimeConverter.convertDateToFormattedString(date));
            datePreviewTextView.setTextColor(Color.parseColor("black"));

        //if arguments are from clicking on add event button in day view fragment
        }else if(arguments.containsKey("selectedDate")){
            //get date object
            date = (Date) arguments.getSerializable("selectedDate");
            Log.d("DateFound", date.toString());

            //set views to the date
            datePreviewTextView.setText(LocalDateTimeConverter.convertDateToFormattedString(date));
            datePreviewTextView.setTextColor(Color.parseColor("black"));
        //if arguments are from clicking on event in day view
        }else if(arguments.containsKey("date")) {
            //set the views for editing a preexisting date
            setEditViews(arguments);
        }
    }

    private void setEditViews(Bundle arguments) {

        // set all views to the event's values
        String title = arguments.getString("title");
        String location = arguments.getString("location");
        date = (Date) arguments.getSerializable("date");
        id = arguments.getInt("id");
        datePreviewTextView.setText(LocalDateTimeConverter.convertDateToFormattedString(date));
        datePreviewTextView.setTextColor(Color.parseColor("black"));
        String startTimeStr = arguments.getString("startTime");
        LocalDateTime startTime = LocalDateTimeConverter.toLocalDateTime(startTimeStr);
        String endTimeStr = arguments.getString("endTime");
        LocalDateTime endTime = LocalDateTimeConverter.toLocalDateTime(endTimeStr);
        String alertTimeStr = arguments.getString("alertTime");
        Log.d("alertTimeStr", alertTimeStr);
        if(!alertTimeStr.equals("none")){
            long alertTime = localDateTimeToEpochMilli(LocalDateTimeConverter.toLocalDateTime(alertTimeStr), ZoneId.systemDefault());
            alertSpinner.setSelection(getAlertTime(alertTime, localDateTimeToEpochMilli(startTime, ZoneId.systemDefault())));
        }
        String description = arguments.getString("description");
        Boolean hasAlert = arguments.getBoolean("hasAlert");
        Boolean allDay = arguments.getBoolean("allDay");
        titleEditText.setText(title);
        int typeIndex = Arrays.asList(EventType.values()).indexOf(arguments.getSerializable("eventType"));
        eventTypeSpinner.setSelection(typeIndex);
        locationEditText.setText(location);
        startHourPicker.setValue(startTime.getHour());
        startMinutePicker.setValue(startTime.getMinute());
        endHourPicker.setValue(endTime.getHour());
        endMinutePicker.setValue(endTime.getMinute());

        descriptionEditText.setText(description);
        submitButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.tick_symbol));

        //set that it is to be an update of a preexisting event
        update = true;
    }

    private int getAlertTime(long alertTime, long startTime) {

        // switch case to determine how many milliseconds
        // before the start time to set the alarm

        long difference = startTime-alertTime;
        int alertTimePosition;
        switch((int) difference){
            case 300000:
                alertTimePosition = 1;
                break;
            case 900000:
                alertTimePosition = 2;
                break;
            case 1800000:
                alertTimePosition =  3;
                break;
            case 3600000:
                alertTimePosition = 4;
                break;
            case 7200000:
                alertTimePosition =  5;
                break;
            default:
                alertTimePosition = 0;
                break;
        }
        return alertTimePosition;

    }


    private void setOnClickListeners(View view) {

        //setting date picker
        createDatePicker.setOnClickListener(v -> {
            MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Date")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();
            materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
                @Override
                public void onPositiveButtonClick(Long selection) {
                    // date selected
                    date = new Date(selection);

                    //set views to reflect selected date
                    datePreviewTextView.setTextColor(Color.parseColor("black"));
                    datePreviewTextView.setText(LocalDateTimeConverter.convertDateToFormattedString(date));
                }
            });
            materialDatePicker.show(requireActivity().getSupportFragmentManager(), "tag");
        });




        allDaySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                // Toggle the visibility of start and end time pickers
                int visibility = isChecked ? View.GONE : View.VISIBLE;
                startTimeTextView.setVisibility(visibility);
                endTimeTextView.setVisibility(visibility);
                startHourPicker.setVisibility(visibility);
                startMinutePicker.setVisibility(visibility);
                endHourPicker.setVisibility(visibility);
                endMinutePicker.setVisibility(visibility);
            }
        });



        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // if inputs are found to be valid
                if(areInputsValid()) {

                    //create the event as inputs are valid
                    PrivateEvent event = createEvent();

                    //then event must be alerted
                    assert event != null;
                    uploadEvent(event);

                    //if alert has been set, create appropriate notification
                    String alertSpinnerString = alertSpinner.getSelectedItem().toString();
                    if(!(alertSpinnerString.equals("None"))){
                        createNotification(alertSpinnerString, event);
                    }

                    //switch fragments to day view of the selected date, to show user that event has been
                    //uploaded successfully
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

                    Bundle bundle = new Bundle();
                    bundle.putSerializable("selectedDate", event.getDate());
                    DayViewFragment dayViewFragment = new DayViewFragment();
                    dayViewFragment.setArguments(bundle);

                    fragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainerView, dayViewFragment)
                            .addToBackStack(null)
                            .commit();


                }
            }
        });
    }

    private void createNotification(String alertSpinnerString, PrivateEvent event) {
        Log.d("setting alert", "");

        //create intent for broadcasting
        Intent intent = new Intent(getActivity(), ReminderBroadcast.class);
        intent.setAction("com.example.calendarapp.ACTION_REMIND");
        intent.putExtra("title", event.getTitle());
        intent.putExtra("eventTime", convertLocalDateTimeToFormattedString(event.getStartTime()));

        //must have unique request code as to not overwrite previously instantiated reminders
        int requestCode = (int) (System.currentTimeMillis() & 0xfffffff);

        //create pendingIntent which is an intent and an action, broadcasting
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), requestCode, intent, PendingIntent.FLAG_IMMUTABLE);

        //get the alarm time and start time
        long alarmTime = getAlarmTime(alertSpinnerString);
        long startTimeLong = localDateTimeToEpochMilli(event.getStartTime(), ZoneId.systemDefault());
        Log.d("Alarm Time", String.valueOf(alarmTime));

        // then set alarm time to be the alarm time(e.g. 5 mins) taken away from the startTime
        Context context = requireActivity();
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        if(alarmManager!=null){
            Log.d("alarmManager", "created");
            alarmManager.set(AlarmManager.RTC_WAKEUP,
                                startTimeLong - alarmTime,
                                pendingIntent);
        }else{
            Log.d("alarmManager", "not created");
        }


    }


    private long getAlarmTime(String alertSpinnerString) {
        // gets the alarm time in milliseconds
        long alarmTime;
        switch(alertSpinnerString){
            case "5 minutes before":
                alarmTime = 300000;
                break;
            case "15 minutes before":
                alarmTime = 900000;
                break;
            case "30 minutes before":
                alarmTime =  1800000;
                break;
            case "1 hour before":
                alarmTime = 3600000;
                break;
            case "2 hours before":
                alarmTime =  7200000;
                break;
            default:
                alarmTime = 0;
                break;
        }
        return alarmTime;
    }

    private void uploadEvent(PrivateEvent event) {
        //get event view model
        eventViewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);
        Log.d("Event view model", event.toString(), null);
        Log.d("update value", String.valueOf(update), null);

        //if an update to a previously instantiated event, then update
        if (update) {
            Log.d("Updating private event", "update");
            eventViewModel.update(event);

        //if not, then insert
        } else {
            Log.d("Inserting private event", "inserts");
            eventViewModel.insert(event);
        }
    }

    //Makes sure events are valid
    private boolean areInputsValid() {
        String title = String.valueOf(titleEditText.getText());
        String location = String.valueOf(locationEditText.getText());
        String description = String.valueOf(descriptionEditText.getText());
        boolean isValid = true;
        if(title.length()> 60){
            titleLayout.setError("Title cannot be greater than 60 characters");
            isValid = false;
        }
        if(title.isEmpty()) {
            titleLayout.setError("Title cannot be empty");
            isValid = false;
        }
        if(location.isEmpty()){
            locationLayout.setError("Location cannot be empty");
            isValid = false;
        }
        if(description.isEmpty()){
            descriptionLayout.setError("Description cannot be empty");
            isValid = false;
        }
        if(description.length()>300){
            descriptionLayout.setError("Description cannot be greater than 300 characters");
            isValid = false;
        }

        if(date==null){
            isValid = false;
            Toast.makeText(getContext(), "A date must be selected", Toast.LENGTH_SHORT).show();
        }

        LocalDateTime startDateTime = null, endDateTime = null;

        //get numberpicker values
        int startHour = startHourPicker.getValue();
        int startMinute = startMinutePicker.getValue();
        int endHour = endHourPicker.getValue();
        int endMinute = endMinutePicker.getValue();

        //attempts to convert these values to LocalDateTime objects
        try{
            LocalDateTime initialDate = date.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            startDateTime=LocalDateTime.of(initialDate.toLocalDate(), LocalTime.of(startHour,
                    startMinute));
            endDateTime=LocalDateTime.of(initialDate.toLocalDate(), LocalTime.of(endHour,
                    endMinute));

            boolean allDay = allDaySwitch.isChecked();
            if(!allDay){
                if(startDateTime!=null  && endDateTime!=null){
                    //stops the end time being before the start time
                    if (!startDateTime.isBefore(endDateTime)) {
                        Toast.makeText(getContext(), "The start time must be before the end time", Toast.LENGTH_SHORT).show();
                        isValid = false;
                    }
                }
            }




        }catch (Exception e){
        //If doesnt work, then not a valid event
            isValid = false;
            Log.d("DatePicker", "No date selected");
            Toast.makeText(getContext(), "Must set start and end times and date", Toast.LENGTH_SHORT).show();
        }




        return isValid;
    }

    private void init(View view) {
        //initialise the views
        titleEditText = view.findViewById(R.id.titleEditText);
        locationEditText = view.findViewById(R.id.locationEditText);
        descriptionEditText = view.findViewById(R.id.descriptionEditText);

        titleLayout = view.findViewById(R.id.titleTextLayout);
        locationLayout = view.findViewById(R.id.locationTextLayout);
        descriptionLayout = view.findViewById(R.id.descriptionTextLayout);
        eventTypeSpinner = view.findViewById(R.id.eventTypeSpinner);
        alertSpinner = view.findViewById(R.id.alertSpinner);

        startTimeTextView = view.findViewById(R.id.startTimeTextView);
        endTimeTextView = view.findViewById(R.id.endTimeTextView);
        startHourPicker = view.findViewById(R.id.startHourPicker);
        startMinutePicker = view.findViewById(R.id.startMinutePicker);
        endHourPicker = view.findViewById(R.id.endHourPicker);
        endMinutePicker = view.findViewById(R.id.endMinutePicker);

        startHourPicker.setMinValue(0);
        startHourPicker.setMaxValue(23);
        startHourPicker.setValue(0);

        startMinutePicker.setMinValue(0);
        startMinutePicker.setMaxValue(59);
        startMinutePicker.setValue(0);

        endHourPicker.setMinValue(0);
        endHourPicker.setMaxValue(23);
        endHourPicker.setValue(0);

        endMinutePicker.setMinValue(0);
        endMinutePicker.setMaxValue(59);
        endMinutePicker.setValue(0);

        datePreviewTextView = view.findViewById(R.id.dateSelectedTextView);
        createDatePicker = view.findViewById(R.id.createDatePicker);
        submitButton = view.findViewById(R.id.submitEvent);
        allDaySwitch = view.findViewById(R.id.allDaySwitch);
    }


    //create the event, by gathering all information
    private PrivateEvent createEvent() {
        String title = titleEditText.getText().toString();
        String location = locationEditText.getText().toString();
        String description = descriptionEditText.getText().toString();

        String eventTypeString = eventTypeSpinner.getSelectedItem().toString();
        EventType eventType;
        if (eventTypeString.equals("Social")){
            eventType = EventType.SOCIAL;
        } else if (eventTypeString.equals("Work")){
            eventType = EventType.WORK;
        }else{
            eventType = EventType.PERSONAL;
        }





        int startHour = startHourPicker.getValue();
        int startMinute = startMinutePicker.getValue();
        int endHour = endHourPicker.getValue();
        int endMinute = endMinutePicker.getValue();

        //same method as checking inputs are valid
        LocalDateTime startDateTime = null, endDateTime = null;
        try{
            LocalDateTime initialDate = date.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            startDateTime=LocalDateTime.of(initialDate.toLocalDate(), LocalTime.of(startHour,
                    startMinute));
            endDateTime=LocalDateTime.of(initialDate.toLocalDate(), LocalTime.of(endHour,
                    endMinute));

        }catch (Exception e){
            Log.d("DatePicker", "No date selected");
            Toast.makeText(getContext(), "Must Select a Date", Toast.LENGTH_SHORT).show();
        }




        String alertTimeString = alertSpinner.getSelectedItem().toString();
        boolean alert;
        LocalDateTime alertTime = startDateTime;

        if (alertTimeString.equals("None")) {
            alert = false;
        } else {
            alert = true;
            if (alertTimeString.equals("5 minutes before")) {
                alertTime = startDateTime.minusMinutes(5);
            } else if (alertTimeString.equals("15 minutes before")) {
                alertTime = startDateTime.minusMinutes(15);
            } else if (alertTimeString.equals("30 minutes before")) {
                alertTime = startDateTime.minusMinutes(30);
            } else if (alertTimeString.equals("1 hour before")) {
                alertTime = startDateTime.minusHours(1);
            } else if (alertTimeString.equals("2 hours before")) {
                alertTime = startDateTime.minusHours(2);
            }
        }

        boolean allDay = allDaySwitch.isChecked();
        if(allDay){
            startDateTime = startDateTime.with(LocalTime.MIN);
            endDateTime = startDateTime.with(LocalTime.MAX);
        }


        //attemps to create the PublicEvent object
        try{
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String uid = user.getUid();
            PrivateEvent event = new PrivateEvent(title, null, uid, location, date, allDay, startDateTime, endDateTime, alert, alertTime, eventType, description);
            if(update){ //if updating a previously instantiated event.
                event.setId(id);
            }
            Toast.makeText(requireContext(), "Event created: " + event.toString(), Toast.LENGTH_SHORT).show();
            Log.d("Event created", event.toString());
            return event;
        }catch (Exception e) {
            Toast.makeText(requireContext(), "Event not created", Toast.LENGTH_SHORT).show();
        }
        return null;


    }
}