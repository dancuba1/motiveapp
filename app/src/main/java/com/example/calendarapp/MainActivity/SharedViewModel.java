package com.example.calendarapp.MainActivity;

import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    private int selectedItemId;

    public int getSelectedItemId() {
        return selectedItemId;
    }

    public void setSelectedItemId(int selectedItemId) {
        this.selectedItemId = selectedItemId;
    }
}

