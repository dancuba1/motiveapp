package com.example.calendarapp.MainActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.calendarapp.R;

public class ToggleFragment extends Fragment {

    private ToggleListener toggleListener;

    interface ToggleListener {
        void onToggleButtonClick();
    }

    void setToggleListener(ToggleListener listener) {
        this.toggleListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_toggle, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button toggleButton = view.findViewById(R.id.arrowButton);
        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (toggleListener != null) {
                    toggleListener.onToggleButtonClick();
                }
            }
        });
    }
}
