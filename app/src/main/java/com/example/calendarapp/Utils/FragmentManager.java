package com.example.calendarapp.Utils;


import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.Fragment;

import com.example.calendarapp.R;

public class FragmentManager {
    public static void changeFragment(Fragment newFragment, androidx.fragment.app.FragmentManager fragmentManager){
        fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, newFragment)
                .addToBackStack("profile")
                .commit();
    }
}
