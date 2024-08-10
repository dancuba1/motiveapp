package com.example.calendarapp;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.example.calendarapp.OnlineDb.PublicEventRepository.getEventById;
import static com.example.calendarapp.Utils.DateTypeConverter.fromDate;
import static com.example.calendarapp.Utils.LocalDateTimeConverter.convertDateToFormattedString;
import static java.lang.String.format;

import com.example.calendarapp.OnlineDb.PublicEventRepository;
import com.example.calendarapp.Utils.LocalDateTimeConverter;
import com.example.calendarapp.Utils.LocationUtils;
import com.google.android.material.datepicker.MaterialDatePicker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.calendarapp.EventObjects.EventGenre;
import com.example.calendarapp.EventObjects.EventType;
import com.example.calendarapp.EventObjects.PublicEvent;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import mabbas007.tagsedittext.TagsEditText;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddEditPublicFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddEditPublicFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Uri bannerImage, uploadImage1, uploadImage2, uploadImage3, uploadImage4;
    private String bannerUrl, image1Url, image2Url, image3Url, image4Url, eventId;
    private ArrayList<String> imageUrls;
    private DatePicker datePicker;
    private Spinner eventGenreSpinner, eventTypeSpinner;
    private EditText titleEditText, locationEditText, descriptionEditText, linkEditText, priceInput;
    private NumberPicker startHourPicker, startMinutePicker, endHourPicker, endMinutePicker;
    private ImageView bannerImageView, uploadImageView1, uploadImageView2, uploadImageView3, uploadImageView4;
    private ActivityResultLauncher<Intent> imagePickLauncher;
    private MaterialButton bannerButton, createDatePicker;
    private MaterialTextView datePreviewTextView;
    private PublicEvent event;
    private TextInputLayout titleLayout, locationLayout, descriptionLayout, linkLayout, priceLayout;
    private FirebaseUser user;
    private Date datePickerDate;
    private boolean newEvent, justBanner, bannerChanged;
    private FloatingActionButton submitButton, deleteButton;
    private int currentImagePickAction = -1;
    private TagsEditText tagsEditText;
    private double priceDouble;


    public AddEditPublicFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddEditPublicFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AddEditPublicFragment newInstance(String param1, String param2) {
        AddEditPublicFragment fragment = new AddEditPublicFragment();
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
        View view =  inflater.inflate(R.layout.fragment_add_edit_public, container, false);
        init(view);

        user = FirebaseAuth.getInstance().getCurrentUser();
        setOnClickListeners(view);


        Bundle args = getArguments();
        if(args!=null && args.getString("eventId")!=null){
            eventId = args.getString("eventId");
            newEvent = false;
            loadEventData(eventId, user);
        }else{
            newEvent = true;
        }

        bannerChanged = false;

        return view;

    }

    private void init(View view) {
        user = FirebaseAuth.getInstance().getCurrentUser();
        datePreviewTextView = view.findViewById(R.id.dateSelectedTextView);
        titleEditText = view.findViewById(R.id.titleEditText);
        locationEditText = view.findViewById(R.id.locationEditText);
        descriptionEditText = view.findViewById(R.id.descriptionEditText);
        linkEditText = view.findViewById(R.id.linksEditText);
        datePicker = view.findViewById(R.id.datePicker);
        createDatePicker = view.findViewById(R.id.createDatePicker);
        titleLayout = view.findViewById(R.id.titleTextLayout);
        locationLayout = view.findViewById(R.id.locationTextLayout);
        descriptionLayout = view.findViewById(R.id.descriptionTextLayout);
        linkLayout = view.findViewById(R.id.linksTextLayout);
        eventTypeSpinner = view.findViewById(R.id.eventTypeSpinner);



        bannerButton = view.findViewById(R.id.bannerButton);
        bannerImageView =  view.findViewById(R.id.bannerImage);
        uploadImageView1 = view.findViewById(R.id.uploadImage1);
        uploadImageView2 = view.findViewById(R.id.uploadImage2);
        uploadImageView3 = view.findViewById(R.id.uploadImage3);
        uploadImageView4 = view.findViewById(R.id.uploadImage4);

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

        priceInput = view.findViewById(R.id.priceInput);
        eventGenreSpinner = view.findViewById(R.id.eventGenreSpinner);
        ArrayList<String> spinnerArray = new ArrayList<>();
        for (EventGenre genre : EventGenre.values()) {
            spinnerArray.add(genre.getDisplayName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, spinnerArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        eventGenreSpinner.setAdapter(adapter);
        imageUrls = new ArrayList<>();
        bannerUrl = null;
        deleteButton = view.findViewById(R.id.deleteEventButton);
        tagsEditText = view.findViewById(R.id.tagsEditText);


    }


    private void loadEventData(String eventId, FirebaseUser user) {
        getEventById(eventId,new PublicEventRepository.EventFetchListener(){
            @Override
            public void onEventFetched(PublicEvent fetchedEvent){
                event = fetchedEvent;
                Log.d("Event Details", event.toString());
                setEditViews(event);
            }
        });
    }

    private void setEditViews(PublicEvent currentEvent){
        titleEditText.setText(currentEvent.getTitle());
        locationEditText.setText(currentEvent.getLocation());
        descriptionEditText.setText(currentEvent.getDescription());
        linkEditText.setText(currentEvent.getLink());

        int genreIndex = Arrays.asList(EventGenre.values()).indexOf(event.getEventGenre());
        eventGenreSpinner.setSelection(genreIndex);
        int typeIndex = Arrays.asList(EventType.values()).indexOf(event.getEventType());
        eventTypeSpinner.setSelection(typeIndex);


        if(event.getTags()!=null){
            String [] tagsStringList = event.getTags().toArray(new String[0]);
            tagsEditText.setTags(tagsStringList);
        }


        datePickerDate = currentEvent.getDate();
        datePreviewTextView.setText(convertDateToFormattedString(datePickerDate));
        datePreviewTextView.setTextColor(Color.parseColor("black"));

        startHourPicker.setValue(currentEvent.getStartTime().getHour());
        startMinutePicker.setValue(currentEvent.getStartTime().getMinute());
        endHourPicker.setValue(currentEvent.getEndTime().getHour());
        endMinutePicker.setValue(currentEvent.getEndTime().getMinute());
        if(!(currentEvent.getBannerUrl() == null)){
            bannerUrl = currentEvent.getBannerUrl();
            Glide.with(getContext()).load(currentEvent.getBannerUrl()).into(bannerImageView);
            bannerButton.setVisibility(GONE);
            setBannerOnClickListener();
        }
        submitButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.tick_symbol));
        deleteButton.setVisibility(VISIBLE);

        String priceString = currentEvent.getPrice();
        Log.d("priceString", priceString);
        String numericPart = priceString.replace("£", "");
        NumberFormat format = NumberFormat.getInstance(Locale.UK); // Adjust the Locale as needed
        try {
            Number number = format.parse(numericPart);
            priceDouble = number.doubleValue();
            priceInput.setText(numericPart);
        }catch(Exception e){
            Log.d("Price", e.getMessage());
        }


    }

    private void setOnClickListeners(View view) {
        bannerButton.setOnClickListener((v) -> {
            ImagePicker.with(this).crop().compress(1080).maxResultSize(1080, 1920)
                    .createIntent(intent -> {
                        intent.putExtra("code", 0);
                        imagePickLauncher.launch(intent);
                        return null;
                    });
        });

        priceInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String userInput = priceInput.getText().toString();
                    try {
                        // Parse user input to a number
                        double price = Double.parseDouble(userInput);
                        // Format the number back to a user-friendly string
                        NumberFormat currencyFormat = NumberFormat.getNumberInstance();
                        priceInput.setText(currencyFormat.format(price));
                    } catch (NumberFormatException e) {
                        priceInput.setError("Invalid price");
                    }
                }
            }
        });

        uploadImageView1.setOnClickListener((v) -> {
            ImagePicker.with(this).cropSquare().compress(512).maxResultSize(512,512)
                    .createIntent(new Function1<Intent, Unit>() {
                        @Override
                        public Unit invoke(Intent intent) {
                            intent.putExtra("code", 1);
                            imagePickLauncher.launch(intent);
                            return null;
                        }
                    });
        });
        uploadImageView2.setOnClickListener((v) -> {
            ImagePicker.with(this).cropSquare().compress(512).maxResultSize(512,512)
                    .createIntent(new Function1<Intent, Unit>() {
                        @Override
                        public Unit invoke(Intent intent) {
                            intent.putExtra("code", 2);
                            imagePickLauncher.launch(intent);
                            return null;
                        }
                    });
        });
        uploadImageView3.setOnClickListener((v) -> {
            ImagePicker.with(this).cropSquare().compress(512).maxResultSize(512,512)
                    .createIntent(intent -> {
                        intent.putExtra("code", 3);
                        imagePickLauncher.launch(intent);
                        return null;
                    });
        });
        uploadImageView4.setOnClickListener((v) -> {
            ImagePicker.with(this).cropSquare().compress(512).maxResultSize(512,512)
                    .createIntent(intent -> {
                        intent.putExtra("code", 4);
                        imagePickLauncher.launch(intent);
                        return null;
                    });
        });

        createDatePicker.setOnClickListener(v -> {
            MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Date")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();
            materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
                @Override
                public void onPositiveButtonClick(Long selection) {
                    datePickerDate = new Date(selection);
                    datePreviewTextView.setTextColor(Color.parseColor("black"));
                    datePreviewTextView.setText(convertDateToFormattedString(datePickerDate));
                }
            });
            materialDatePicker.show(getActivity().getSupportFragmentManager(), "tag");
        });

        imagePickLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if(result.getResultCode() == Activity.RESULT_OK){
                        Intent data = result.getData();
                        if(data!=null && data.getData()!=null){
                            if(data.getIntExtra("code", 0)==0){
                                Log.d("imagePickLauncher", "bannerImage picked", null);
                                bannerChanged = true;
                                bannerImage = data.getData();
                                setImageView(getContext(),bannerImage,bannerImageView, 1);
                                bannerButton.setVisibility(GONE);
                                setBannerOnClickListener();
                            } else if (data.getIntExtra("code", 0)==1){
                                Log.d("imagePickLauncher", "image1 picked", null);
                                uploadImage1 = data.getData();
                                setImageView(getContext(),uploadImage1,uploadImageView1, 0);
                                uploadImageView2.setVisibility(VISIBLE);
                            }else if(data.getIntExtra("code", 0)==2){
                                Log.d("imagePickLauncher", "image2 picked", null);
                                uploadImage2 = data.getData();
                                setImageView(getContext(),uploadImage2,uploadImageView2, 0);
                                uploadImageView3.setVisibility(VISIBLE);
                            }else if(data.getIntExtra("code", 0)==3){
                                Log.d("imagePickLauncher", "image3 picked", null);
                                uploadImage3 = data.getData();
                                setImageView(getContext(),uploadImage3,uploadImageView3, 0);
                                uploadImageView4.setVisibility(VISIBLE);
                            }else if(data.getIntExtra("code", 0)==4){
                                Log.d("imagePickLauncher", "image2 picked", null);
                                uploadImage4 = data.getData();
                                setImageView(getContext(),uploadImage4,uploadImageView4, 0);
                            }
                        }
                    }
                }
        );

        submitButton = view.findViewById(R.id.submitEvent);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = titleEditText.getText().toString();
                if(areInputsValid(view)){
                    if(createEvent()){
                        if(newEvent){
                            Log.d("Create/Edit Event", "Create Event", null);
                            EventUploadCallback imageUploadCallback = new EventUploadCallback() {
                                @Override
                                public void onComplete(String eventId) {
                                    EventUploadCallback eventUploadCallback = new EventUploadCallback() {
                                        @Override
                                        public void onComplete(String eventId) {
                                            // Handle successful upload here
                                            Log.d("EventUpload", "Event uploaded successfully with ID: " + eventId);
                                            Toast.makeText(getContext(), "Event uploaded successfully!", Toast.LENGTH_SHORT).show();
                                            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                            Bundle bundle = new Bundle();
                                            bundle.putString("eventId", eventId);
                                            PublicEventViewFragment publicEventViewFragment = new PublicEventViewFragment();
                                            publicEventViewFragment.setArguments(bundle);

                                            fragmentManager.beginTransaction()
                                                    .replace(R.id.fragmentContainerView, publicEventViewFragment)
                                                    .addToBackStack(null)
                                                    .commit();

                                        }

                                        @Override
                                        public void onError(Exception e) {
                                            // Handle error here
                                            Log.d("EventUploadError", "Error uploading event: " + e.getMessage());
                                            Toast.makeText(getContext(), "Error uploading event", Toast.LENGTH_SHORT).show();
                                        }

                                    };
                                    uploadEvent(user, eventUploadCallback);

                                }

                                @Override
                                public void onError(Exception e) {
                                    Log.d("EventUploadError", "Error uploading image: " + e.getMessage());

                                }
                            };
                            uploadImages(user, title, 0, imageUploadCallback);



                        }else{
                            Log.d("Create/Edit Event", "Edit Event", null);
                            updateEvent(user, view);

                        }
                    }else{
                        Log.d("Event Creation", "Event failed to be created", null);
                    }
                }else{
                    Log.d("Event Creation(Input Validation)", "Inputs invalid");
                }
            }

        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!newEvent){
                    deleteDialog();
                }else{
                    Log.d("Delete Event", "A new event(not deletable)", null);
                }
            }
        });
    }

    private void setBannerOnClickListener() {
        bannerImageView.setVisibility(VISIBLE);
        bannerImageView.setOnClickListener((v) -> {
            ImagePicker.with(this).crop().compress(1080).maxResultSize(1080,1920)
                    .createIntent(intent -> {
                        intent.putExtra("code", 0);
                        imagePickLauncher.launch(intent);
                        return null;
                    });
        });
    }

    private void deleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Delete event")
                .setMessage("This will permanently delete this event, are you sure you would like to proceed?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("PublicEvents").document(eventId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("DeletePublicEvent", "DocumentSnapshot successfully deleted!");
                                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                    if (fragmentManager.getBackStackEntryCount() >= 2) {
                                        fragmentManager.popBackStackImmediate();
                                        fragmentManager.popBackStackImmediate();
                                    } else if(fragmentManager.getBackStackEntryCount() == 1) {
                                        fragmentManager.popBackStackImmediate();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.w("DeletePublicEvent", "Error deleting document", e);
                                    // Handle the failure here, e.g., show an error message
                                });


                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })
                .show();
    }


    private void uploadImages(final FirebaseUser user, String title, int imageNumber, EventUploadCallback callback) {
        Uri imageUri = bannerImage;
        String imageRefPath = "eventImages/" + title + "_bannerImage.jpg";
        switch (imageNumber) {
            case 1:
                imageUri = uploadImage1;
                imageRefPath = "eventImages/" + title + "_image1.jpg";
                break;
            case 2:
                imageUri = uploadImage2;
                imageRefPath = "eventImages/" + title + "_image2.jpg";
                break;
            case 3:
                imageUri = uploadImage3;
                imageRefPath = "eventImages/" + title + "_image3.jpg";
                break;
            case 4:
                imageUri = uploadImage4;
                imageRefPath = "eventImages/" + title + "_image4.jpg";
                break;
            default:
                Log.d("UploadImage Switch Case", "default", null);
        }

        if (imageUri != null && bannerChanged) {
            Log.d("Image Uri in Upload Image", "imageUri found", null);
            StorageReference imageRef = FirebaseStorage.getInstance().getReference(imageRefPath);
            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setCustomMetadata("creatorId", user.getUid())
                    .build();
            imageRef.putFile(imageUri, metadata)
                    .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(imageUrl -> {
                        if (imageNumber == 0) {
                            bannerUrl = imageUrl.toString();
                            Log.d("Url Assignment", "bannerUrl "+ bannerUrl);
                        }else{
                            imageUrls.add(imageUrl.toString());
                            Log.d("Url Assignment", "imageUrl " + imageUrl.toString());

                        }
                        if (imageNumber < 4 && !justBanner) {
                            Log.d("Url Assignment", "incrementing image number");
                            uploadImages(user, title, imageNumber + 1, callback); // Proceed to the next image
                        }
                    }))
                    .addOnFailureListener(e -> {
                        Log.d("Upload Error: ", e.getMessage(), null);
                        Toast.makeText(getContext(), "Upload failed for image " + imageNumber, Toast.LENGTH_SHORT).show();
                        if (imageNumber < 4) {
                            uploadImages(user, title, imageNumber + 1, callback); // Proceed to the next image even if current fails
                        }
                    });
        } else {
            Log.d("Image Uri in Upload Image", "imageUri not found", null);
            if (imageNumber < 4) {
                uploadImages(user, title, imageNumber + 1, callback); // Proceed to the next image if current is null
            }else{
                callback.onComplete(eventId);
            }


        }

    }


    public interface EventUploadCallback {
        void onComplete(String eventId);
        void onError(Exception e);
    }


    private void uploadEvent(FirebaseUser user, EventUploadCallback callback) {
        Log.d("In uploadEvent", "start of Upload Event");
        Map<String, Object> map = new HashMap<>();
        map.put("title", event.getTitle());
        map.put("title_lower", event.getTitle().toLowerCase());
        map.put("location", event.getLocation());
        Address address = LocationUtils.getAddressFromString(event.getLocation(), getContext());
        if (address != null) {
            double latitude = address.getLatitude();
            double longitude = address.getLongitude();
            map.put("locationGeo", new GeoPoint(latitude, longitude));
        }
        map.put("link", event.getLink());
        event.setBannerUrl(bannerUrl);
        map.put("bannerImage", event.getBannerUrl());
        map.put("description_lower", event.getDescription().toLowerCase());
        map.put("description", event.getDescription());
        map.put("price", event.getPrice());
        map.put("priceDouble", priceDouble);
        map.put("date_string", convertDateToFormattedString(event.getDate()));

        Date startTime = Date.from(event.getStartTime().atZone(ZoneId.systemDefault()).toInstant());
        Date endTime = Date.from(event.getEndTime().atZone(ZoneId.systemDefault()).toInstant());
        com.google.firebase.Timestamp startTimeTimestamp = new com.google.firebase.Timestamp(startTime);
        com.google.firebase.Timestamp endTimeTimestamp = new com.google.firebase.Timestamp(endTime);
        Timestamp dateTimeStamp = new Timestamp(event.getDate());
        map.put("startTime", startTimeTimestamp);
        map.put("endTime", endTimeTimestamp);
        map.put("date", dateTimeStamp);
        map.put("eventGenre", event.getEventGenre());
        map.put("eventType", event.getEventType());

        map.put("creatorId", user.getUid());
        event.setImageUrls(imageUrls);
        map.put("images", event.getImageUrls());
        if(event.getTags()!=null){
            Log.d("Event Tags", "found");
            map.put("tags", event.getTags());
        }else{
            Log.d("Event Tags", "not found");
        }
        map.put("attendees", event.getAttendees());
        map.put("createdAt", FieldValue.serverTimestamp());

        DocumentReference newEventRef = FirebaseFirestore.getInstance().collection("PublicEvents").document();
        String eventId = newEventRef.getId();
        map.put("eventId", eventId);


        newEventRef.set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.d("UploadEvent", "Public Event created with ID: " + eventId);
                    Toast.makeText(getContext(), "Public Event created", Toast.LENGTH_SHORT).show();
                    callback.onComplete(eventId); // Invoke the callback with the new event ID
                }else{
                    Log.d("Event Creation Error", task.getException().getMessage());
                    Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    callback.onError(task.getException());
                }
            }
        });
    }



    private void updateEvent(FirebaseUser user, View view) {
        if (areInputsValid(view)) {
            if (createEvent()) {
                String title = event.getTitle();
                Log.d("Create/Edit Event", "Edit Event", null);
                EventUploadCallback imageUploadCallback = new EventUploadCallback() {
                    @Override
                    public void onComplete(String eventId) {
                        EventUploadCallback eventUploadCallback = new EventUploadCallback() {
                            @Override
                            public void onComplete(String eventId) {
                                // Handle successful upload here
                                Log.d("EventUpload", "Event uploaded successfully with ID: " + event.toString());
                                Toast.makeText(getContext(), "Event uploaded successfully!", Toast.LENGTH_SHORT).show();
                                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                Bundle bundle = new Bundle();
                                bundle.putString("eventId", eventId);
                                PublicEventViewFragment publicEventViewFragment = new PublicEventViewFragment();
                                publicEventViewFragment.setArguments(bundle);

                                fragmentManager.beginTransaction()
                                        .replace(R.id.fragmentContainerView, publicEventViewFragment)
                                        .addToBackStack(null)
                                        .commit();

                            }

                            @Override
                            public void onError(Exception e) {
                                // Handle error here
                                Log.d("EventUploadError", "Error uploading event: " + e.getMessage());
                                Toast.makeText(getContext(), "Error uploading event", Toast.LENGTH_SHORT).show();
                            }

                        };
                        updateEventFirebase(user, eventUploadCallback);

                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d("EventUploadError", "Error uploading image: " + e.getMessage());

                    }
                };
                uploadImages(user, title, 0, imageUploadCallback);

            }
        }



    }



    private void updateEventFirebase(FirebaseUser user, EventUploadCallback callback) {
        Log.d("In uploadEvent", "start of Upload Event");
        Map<String, Object> map = new HashMap<>();
        map.put("title", event.getTitle());
        map.put("title_lower", event.getTitle().toLowerCase());

        map.put("location", event.getLocation());
        Address address = LocationUtils.getAddressFromString(event.getLocation(), getContext());
        if (address != null) {
            double latitude = address.getLatitude();
            double longitude = address.getLongitude();
            map.put("locationGeo", new GeoPoint(latitude, longitude));
        }
        map.put("link", event.getLink());
        event.setBannerUrl(bannerUrl);
        if(bannerChanged){
            map.put("bannerImage", event.getBannerUrl());
        }
        map.put("description_lower", event.getDescription().toLowerCase());
        map.put("description", event.getDescription());
        map.put("price", event.getPrice());
        map.put("priceDouble", priceDouble);
        map.put("eventType", event.getEventType());
        map.put("date_string", convertDateToFormattedString(event.getDate()));

        Date startTime = Date.from(event.getStartTime().atZone(ZoneId.systemDefault()).toInstant());
        Date endTime = Date.from(event.getEndTime().atZone(ZoneId.systemDefault()).toInstant());
        com.google.firebase.Timestamp startTimeTimestamp = new com.google.firebase.Timestamp(startTime);
        com.google.firebase.Timestamp endTimeTimestamp = new com.google.firebase.Timestamp(endTime);
        Timestamp dateTimeStamp = new Timestamp(event.getDate());
        map.put("startTime", startTimeTimestamp);
        map.put("endTime", endTimeTimestamp);
        map.put("date", dateTimeStamp);
        map.put("eventGenre", event.getEventGenre());

        map.put("creatorId", user.getUid());
        event.setImageUrls(imageUrls);
        map.put("images", event.getImageUrls());

        map.put("tags", event.getTags());

        DocumentReference eventRef = FirebaseFirestore.getInstance().collection("PublicEvents").document(eventId);
        map.put("eventId", eventId);

        eventRef.update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.d("UpdateEvent", "Public Event created with ID: " + eventId);
                    Toast.makeText(getContext(), "Public Event created", Toast.LENGTH_SHORT).show();
                    callback.onComplete(eventId); // Invoke the callback with the new event ID
                }else{
                    Log.d("Event Update Error", task.getException().getMessage());
                    Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    callback.onError(task.getException());
                }
            }
        });
    }



    private boolean areInputsValid(View view) {
        boolean valid = true;

        if(bannerImageView.getVisibility() == GONE){
            valid = false;
            Toast.makeText(getContext(), "Must have a banner image", Toast.LENGTH_SHORT).show();
        }
        String title = titleEditText.getText().toString().trim();
        String location = locationEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String link = linkEditText.getText().toString().trim();
        int startHour = startHourPicker.getValue();
        int startMinute = startMinutePicker.getValue();
        int endHour = endHourPicker.getValue();
        int endMinute = endMinutePicker.getValue();

        if(datePickerDate==null){
            Toast.makeText(getContext(), "A date must be selected.", Toast.LENGTH_SHORT).show();
            valid = false;
        }else{
            LocalDate localDatePickerDate = datePickerDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            // Combine LocalDate with LocalTime to create LocalDateTime
            LocalDateTime startDateTime = LocalDateTime.of(localDatePickerDate, LocalTime.of(startHour, startMinute));
            LocalDateTime endDateTime = LocalDateTime.of(localDatePickerDate, LocalTime.of(endHour, endMinute));
            if (!startDateTime.isBefore(endDateTime)) {
                Toast.makeText(getContext(), "The start time must be before the end time", Toast.LENGTH_SHORT).show();
                valid = false;
            }

        }






        if (title.isEmpty()) {
            titleLayout.setError("Title cannot be empty");
            valid = false;
        } else if (title.length() > 100) {
            titleLayout.setError("Title cannot exceed 100 characters");
            valid = false;
        } else {
            titleLayout.setError(null);
        }

        if (location.isEmpty()) {
            locationLayout.setError("Location cannot be empty");
            valid = false;
        } else {
            locationLayout.setError(null);
        }

        if (description.isEmpty()) {
            descriptionLayout.setError("Description cannot be empty");
            valid = false;
        } else {
            descriptionLayout.setError(null);
        }

        if (link.isEmpty()) {
            linkLayout.setError("Link cannot be empty");
            valid = false;
        } else {
            linkLayout.setError(null); // Clear error
        }


        if (tagsEditText.getTags() == null || tagsEditText.getTags().size() == 0) {
            Toast.makeText(getContext(), "Must have at least 1 tag", Toast.LENGTH_SHORT).show();
            valid = false;
        }
        if(tagsEditText.getTags().size() > 10){
            Toast.makeText(getContext(), "Must have no more than 10 tags", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        try {
            priceDouble = Double.parseDouble(priceInput.getText().toString());
            if (priceDouble < 0) {
                valid=false;
                priceInput.setError("Price cannot be negative");
            }

        } catch (NumberFormatException e) {
            valid=false;
            priceInput.setError("Invalid price");
        }

        return valid;
    }


    private boolean createEvent(){
        String title = titleEditText.getText().toString();
        String location = locationEditText.getText().toString();
        String description = descriptionEditText.getText().toString();
        String link = linkEditText.getText().toString();

        String price = "";
        try {
            double priceDouble = Double.parseDouble(priceInput.getText().toString());
            if (priceDouble < 0) {
                priceInput.setError("Price cannot be negative");

            }
            price = String.format("£%.2f", priceDouble);
            // Proceed with a valid price
        } catch (NumberFormatException e) {
            priceInput.setError("Invalid price");
        }


        int startHour = startHourPicker.getValue();
        int startMinute = startMinutePicker.getValue();
        int endHour = endHourPicker.getValue();
        int endMinute = endMinutePicker.getValue();

        LocalDateTime startDateTime, endDateTime;
        try{
            LocalDateTime initialDate = datePickerDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            startDateTime=LocalDateTime.of(initialDate.toLocalDate(), LocalTime.of(startHour,
                    startMinute));
            endDateTime=LocalDateTime.of(initialDate.toLocalDate(), LocalTime.of(endHour,
                    endMinute));

        }catch (Exception e){
            Log.d("DatePicker", "No date selected");
            Toast.makeText(getContext(), "Must Select a Date", Toast.LENGTH_SHORT).show();
            return false;
        }




        String selectedDisplayName = eventGenreSpinner.getSelectedItem().toString();
        EventGenre eventGenre = null;
        for (EventGenre genre : EventGenre.values()) {
            if (genre.getDisplayName().equals(selectedDisplayName)) {
                eventGenre = genre;
                break;
            }
        }

        EventType eventType = EventType.SOCIAL;

        try{
            String eventTypeString = eventTypeSpinner.getSelectedItem().toString();
            for(EventType type: EventType.values()){
                if(type.getDisplayName().equals(eventTypeString)){
                    eventType = type;
                    break;
                }
            }
        }catch (Exception e){
            Log.d("EventType", "Failed", e);
        }

        List<String> tagList = tagsEditText.getTags();
        ArrayList<String> tags = new ArrayList<>(tagList);


        try{
            String uid = user.getUid();
            event = new PublicEvent(title, null, uid, location, datePickerDate, startDateTime, endDateTime, eventType, eventGenre, description, price, link, bannerUrl, imageUrls, tags, new ArrayList<>());
            Toast.makeText(requireContext(), "Event created: " + event.toString(), Toast.LENGTH_SHORT).show();
            Log.d("event creation", "Event created: " + event.toString());
            return true;
        }catch (Exception e) {
            Log.d("event creation", "Event created: " + e);
            Toast.makeText(requireContext(), "Event not created", Toast.LENGTH_SHORT).show();
            return false;
        }
    }




    public void setImageView(Context context, Uri imageUri, ImageView imageView, int code){
        if(code==1){
            Glide.with(context).load(imageUri).apply(RequestOptions.circleCropTransform()).into(imageView);
        }if(code==0){
            Glide.with(context).load(imageUri).apply(RequestOptions.centerInsideTransform()).into(imageView);
        }else{
            Glide.with(context).load(imageUri).into(imageView);
        }

    }



}