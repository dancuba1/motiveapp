package com.example.calendarapp.SignUpLogin;

import static com.example.calendarapp.MainActivity.MainActivity.displayFragment;
import static com.example.calendarapp.OnlineDb.UserRepository.deleteAccountAssets;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.calendarapp.MainActivity.MainActivity;
import com.example.calendarapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SignupLoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SignupLoginFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private TextView loginPopup;
    private FirebaseAuth auth;
    private Button loginButton;
    private MaterialTextView loginTV;
    private TextInputLayout emailLayout, passwordLayout;


    public SignupLoginFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SignupLoginFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SignupLoginFragment newInstance(String param1, String param2) {
        SignupLoginFragment fragment = new SignupLoginFragment();
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
        View view = inflater.inflate(R.layout.fragment_signup_login, container, false);
        init(view);
        if(getArguments()!=null){
            loginTV.setText("Delete Account");
            loginButton.setText("Delete");
            loginPopup.setVisibility(View.GONE);
            Log.d("Arguments", "Found");
            setOnClickListeners(1);
        }else{
            Log.d("Arguments", "Not Found");
            setOnClickListeners(0);
        }

        return view;
    }

    private void setOnClickListeners(int code) {
        if(code== 0){
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(areInputsValid()){
                        String email = emailLayout.getEditText().getText().toString();
                        String password = passwordLayout.getEditText().getText().toString();
                        auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if(task.isSuccessful()){
                                            FirebaseUser user = auth.getCurrentUser();
                                            if(!user.isEmailVerified()){
                                                Toast.makeText(getContext(),"Please verify email", Toast.LENGTH_LONG).show();
                                            }else{
                                                login();
                                            }

                                        }else{
                                            String error = task.getException().getMessage();
                                            Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }else{
                        Toast.makeText(getContext(), "Inputs are invalid", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else{
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    String email = emailLayout.getEditText().getText().toString();
                    String password = passwordLayout.getEditText().getText().toString();
                    AuthCredential credential = EmailAuthProvider.getCredential(email, password);

                    user.reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                deleteAccountAssets(user);
                                                Toast.makeText(getContext(), user.getUid() + " " + user.getEmail() + " is deleted", Toast.LENGTH_SHORT);
                                                Log.d("Account Deletion", user.getUid() + " " + user.getEmail() + " is deleted");
                                                Intent intent = new Intent(getContext(), LoginRegisterActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                                if (getActivity() != null) {
                                                    getActivity().finish();
                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d("Delete User Account", "Failed to delete " + user.getUid(), e);
                                            }
                                        });

                                    } else {
                                        Toast.makeText(getContext(), "Incorrect email/password", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("Reauthenticate", "Failed to reauthenticate", e);
                                }
                            });

                }
            });
        }
        loginPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSignUpPopupFragment();
            }

        });


    }

    private void init(View view) {
        loginTV =view.findViewById(R.id.loginTV);
        loginButton = view.findViewById(R.id.loginButton);
        emailLayout = view.findViewById(R.id.textInputLayoutEmail);
        passwordLayout = view.findViewById(R.id.textInputLayoutPassword);
        loginPopup = view.findViewById(R.id.loginPopupTextView);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        auth = FirebaseAuth.getInstance();

    }

    private void login() {
        Intent intent = new Intent(getContext(), MainActivity.class);
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("UserSetupComplete", true);
        editor.apply();
        startActivity(intent);
        getActivity().finish();
    }

    private boolean areInputsValid() {
        String email = emailLayout.getEditText().getText().toString();
        String password = passwordLayout.getEditText().getText().toString();


        boolean isValid = true;


        if (email.isEmpty()) {
            emailLayout.setError("Email cannot be empty");
            isValid = false;
        } else {
            emailLayout.setError(null);
        }
        if (password.isEmpty()) {
            passwordLayout.setError("Password cannot be empty");
            isValid = false;
        } else {
            passwordLayout.setError(null);
        }



        boolean b = isValid;
        return b;
    }




    private void openSignUpPopupFragment() {
        Log.d("in openPersonalOLoginPopupFragment", "personal", null);
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        EventSignupFragment eventSignupFragment = new EventSignupFragment();
        displayFragment(eventSignupFragment, getParentFragmentManager());
        fragmentTransaction.addToBackStack("login");
        fragmentTransaction.commit();
    }



}