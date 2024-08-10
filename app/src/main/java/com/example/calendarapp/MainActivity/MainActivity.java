package com.example.calendarapp.MainActivity;



import static com.example.calendarapp.OnlineDb.UserRepository.getUserDetails;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;

import com.example.calendarapp.AddEditPublicFragment;
import com.example.calendarapp.Calendar.AddEditFragment;
import com.example.calendarapp.Calendar.CalendarFragment;
import com.example.calendarapp.HomeFragment;
import com.example.calendarapp.OnlineDb.UserRepository;
import com.example.calendarapp.ProfileFragment;
import com.example.calendarapp.R;
import com.example.calendarapp.SearchFragment;
import com.example.calendarapp.SignUpLogin.LoginRegisterActivity;
import com.example.calendarapp.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private Fragment CalendarFragment;
    private Fragment MonthSelectionFragment;
    private boolean isCalendarFragmentVisible = true;
    private SharedViewModel sharedViewModel;
    private Fragment HomeFragment;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private ToggleFragment toggleFragment;
    private SharedPreferenceHelper preferenceHelper;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private int code;

    private FragmentManager fragmentManager;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //sets initial fragment to HomeFragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainerView, new HomeFragment())
                    .commit();
        }

        user = FirebaseAuth.getInstance().getCurrentUser();

        //if user has not been created properly, having no details, then sign out
        getUserDetails(user.getUid(), new UserRepository.UserCallback() {
            @Override
            public void userRetrieved(User user) {
                if(user==null){
                    signOut();
                }
            }
        });




        //create channel for event alerts
        createNotificationChannel();


        //instantiate the SharedViewModel for the navigation bar
        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnItemSelectedListener(this::onBottomNavigationItemSelected);

        //stop the navigation bar going above the keyboard when typing
        final View activityRootView = findViewById(R.id.rootLayout);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
                if (heightDiff > dpToPx(getApplicationContext(), 200)) { // keyboard shown
                    bottomNavigationView.setVisibility(View.GONE);
                } else { // keyboard hidden
                    bottomNavigationView.setVisibility(View.VISIBLE);
                }
            }
        });

        CalendarFragment = new CalendarFragment();
        toggleFragment = new ToggleFragment();
        HomeFragment = new HomeFragment();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }





    }

    private void signOut() {
        Log.d("No user", "none2");
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();
        Intent intent = new Intent(getApplicationContext(), LoginRegisterActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear the activity stack
        startActivity(intent);

        if (getApplicationContext() != null) {

            this.finish();
        }
    }


    public static float dpToPx(Context context, float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }




    //change main fragment container's fragment
    public static void displayFragment(Fragment fragment, FragmentManager fragmentManager) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContainerView, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }



    //create the notification channel for the event alerts
    private void createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.BASE){
            CharSequence name = "notify";
            String description = "Channel for alarm";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel notificationChannel = new NotificationChannel("notify", name, importance);
            notificationChannel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannel);
                Log.d("NotificationChannel", "Notification channel created successfully");
            } else {
                Log.d("NotificationChannel", "Failed to get NotificationManager service");
            }
        }
    }


    Fragment fragment = null;

    //change fragment dependant on bottom navigation item clicked
    private boolean onBottomNavigationItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();
        sharedViewModel.setSelectedItemId(id);
        if (id == R.id.action_home) {
            fragment = new HomeFragment();
        } else if (id == R.id.action_calendar) {
            fragment = new CalendarFragment();
        } else if (id == R.id.action_add && code==1) {
            fragment = new AddEditFragment();
        } else if (id == R.id.action_add && code==0) {
            fragment = new AddEditPublicFragment();
        } else if (id == R.id.action_add && code==2) {
            fragment = new AddEditPublicFragment();
        } else if (id == R.id.action_profile) {
            fragment = new ProfileFragment();
        } else if(id == R.id.action_search){
            fragment = new SearchFragment();
        }

        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainerView, fragment)
                    .commit();
            return true;
        } else {
            return false;
        }

    }


}