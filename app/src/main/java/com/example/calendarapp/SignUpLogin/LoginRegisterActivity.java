package com.example.calendarapp.SignUpLogin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.example.calendarapp.MainActivity.MainActivity;
import com.example.calendarapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginRegisterActivity extends AppCompatActivity {
    SignupLoginFragment signupLoginFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register_actviity);
        signupLoginFragment = new SignupLoginFragment();
        displayFragment(signupLoginFragment);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Get Firebase auth instance
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();

        // Check if user is logged in
        if (user != null) {
            // User is logged in, now check for email verification and user setup completion
            Log.d("AuthCheck", "User Setup Complete: " + isUserSetupComplete());
            if (user.isEmailVerified() && isUserSetupComplete()) {
                // Both email verified and user setup complete, navigate to Main Activity
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        }
    }

    public boolean isUserSetupComplete() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        // Assuming you store a boolean to track if the user's setup is complete.
        return sharedPreferences.getBoolean("UserSetupComplete", false);
    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    private void displayFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView, fragment)
                .addToBackStack(null)
                .commit();
    }
}