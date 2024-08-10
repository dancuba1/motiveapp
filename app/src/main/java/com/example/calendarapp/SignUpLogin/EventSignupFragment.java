package com.example.calendarapp.SignUpLogin;

import static android.content.ContentValues.TAG;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import static com.example.calendarapp.OnlineDb.UserRepository.deleteAccountAssets;
import static com.example.calendarapp.OnlineDb.UserRepository.isUsernameTaken;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.calendarapp.AddEditPublicFragment;
import com.example.calendarapp.MainActivity.MainActivity;
import com.example.calendarapp.R;
import com.example.calendarapp.OnlineDb.UserRepository;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EventSignupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EventSignupFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private ImageView profilePicImageView;
    private String username, email, password, confirmPassword, profilePicUrl, bannerImageUrl, description;
    private ImageView bannerImageView;
    private TextInputEditText editTextFirstName, editTextLastName, editTextEmail, editTextUsername, editTextPassword, editTextConfirmPassword;
    private TextInputLayout usernameLayout, emailLayout, passwordLayout, confirmPasswordLayout, descriptionLayout;
    private EditText editTextDescription;
    private MaterialButton signUpButton, logoutButton, deleteAccountButton;
    private ActivityResultLauncher<Intent> profileImagePickLauncher, bannerImagePickLauncher;
    private Uri selectedImageUri;

    private FirebaseAuth auth;
    private FirebaseDatabase db;
    private DatabaseReference ref;
    private Uri profilePic, bannerImage;
    private TextView emailVerifiedTextView;
    private MaterialTextView descriptionTV;
    private String currentUsername;
    private FirebaseUser user;
    private boolean newProfile, updateProfPic, updateBannerPic;


    public EventSignupFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EventSignupFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EventSignupFragment newInstance(String param1, String param2) {
        EventSignupFragment fragment = new EventSignupFragment();
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
        auth = FirebaseAuth.getInstance();
        View view = inflater.inflate(R.layout.fragment_event_signup, container, false);


        Bundle args = getArguments();
        if(args!=null && args.getString("edit")!=null){
            newProfile = false;
            user = FirebaseAuth.getInstance().getCurrentUser();
        }else{
            newProfile = true;
        }

        init(view);
        setOnClickListeners();


        return view;
    }

    private void setOnClickListeners(){
        profilePicImageView.setOnClickListener((v)->{
            ImagePicker.with(this).cropSquare().compress(512).maxResultSize(512,512)
                    .createIntent(new Function1<Intent, Unit>() {
                        @Override
                        public Unit invoke(Intent intent) {
                            profileImagePickLauncher.launch(intent);
                            return null;
                        }
                    });
        });

        profileImagePickLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if(result.getResultCode() == Activity.RESULT_OK){
                        Intent data = result.getData();
                        if(data!=null && data.getData()!=null){
                            if(!newProfile){
                                updateProfPic = true;
                            }
                            profilePic = data.getData();
                            Log.d("profilePic",profilePic.toString(), null);
                            setImageView(getContext(),profilePic,profilePicImageView, 1);
                        }
                    }
                }
        );

        bannerImagePickLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if(result.getResultCode() == Activity.RESULT_OK){
                        Intent data = result.getData();
                        if(data!=null && data.getData()!=null){
                            if(!newProfile){
                                updateBannerPic = true;
                            }
                            bannerImage = data.getData();
                            setImageView(getContext(),bannerImage,bannerImageView, 0);
                        }
                    }
                }
        );

        bannerImageView.setOnClickListener((v) -> {
            ImagePicker.with(this).cropSquare().compress(512).maxResultSize(512,512)
                    .createIntent(new Function1<Intent, Unit>() {
                        @Override
                        public Unit invoke(Intent intent) {
                            bannerImagePickLauncher.launch(intent);
                            return null;
                        }
                    });
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Sign out from Firebase
                showPromptDialog(0);
            }
        });

        deleteAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPromptDialog(1);
            }
        });

    }
    private void init(View view){
        deleteAccountButton = view.findViewById(R.id.deleteAccountButton);
        logoutButton = view.findViewById(R.id.signOutButton);
        editTextEmail= view.findViewById(R.id.editTextEmail);
        editTextUsername = view.findViewById(R.id.editTextUsername);
        editTextPassword = view.findViewById(R.id.editTextPassword);
        editTextConfirmPassword = view.findViewById(R.id.editTextConfirmPassword);
        editTextDescription = view.findViewById(R.id.editTextDescription);
        signUpButton = view.findViewById(R.id.signUpButton);
        profilePicImageView = view.findViewById(R.id.profile_pic);
        bannerImageView = view.findViewById(R.id.bannerImage);
        emailVerifiedTextView = view.findViewById(R.id.emailVerificationTextView);
        usernameLayout = view.findViewById(R.id.textInputLayoutUsername);
        emailLayout = view.findViewById(R.id.textInputLayoutEmail);
        passwordLayout = view.findViewById(R.id.textInputLayoutPassword);
        confirmPasswordLayout = view.findViewById(R.id.textInputLayoutConfirmPassword);
        descriptionLayout = view.findViewById(R.id.editTextDescriptionLayout);

        if(!newProfile){
            editTextEmail.setVisibility(GONE);
            passwordLayout.setVisibility(GONE);
            confirmPasswordLayout.setVisibility(GONE);
            logoutButton.setVisibility(VISIBLE);
            deleteAccountButton.setVisibility(VISIBLE);
            signUpButton.setText("Update");
            loadProfileData(view);
        }
    }

    private void loadProfileData(View view){
        DocumentReference userRef = FirebaseFirestore.getInstance().collection("Users")
                .document(user.getUid());
        userRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error!=null){
                    return;
                }if(value.exists()){
                    currentUsername = value.getString("username");
                    String description = value.getString("description");
                    editTextDescription.setText(description);
                    editTextUsername.setText(currentUsername);
                    String profileImageUrl = value.getString("profileImage");
                    if (profileImageUrl != null) {
                        loadImageIntoView(profileImageUrl, profilePicImageView);
                    }
                }
            };

        });

    }

    private void loadImageIntoView(String imageUrl, ImageView imageView){
        // Check if the fragment is added to an activity and the context is available
        if (isAdded() && getContext() != null) {
            Glide.with(getContext())
                    .load(imageUrl)
                    .circleCrop()
                    .into(imageView);
        }
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> inputs = getInputs();
                username = inputs.get(0);
                email = inputs.get(1);
                password = inputs.get(2);
                confirmPassword = inputs.get(3);
                description = inputs.get(4);

                if(newProfile){
                    if(areInputsValid(view)){
                        createAccount(username, password, email);
                    }else{
                        Toast.makeText(getContext(), "Inputs are invalid", Toast.LENGTH_SHORT);
                        Log.d("EventSignupFragment", "Inputs are invalid");
                    }
                }else{
                    areEditInputsValid(view, isValid -> {
                        if (isValid) {
                            Log.d("Updating User", "Inputs are valid", null);

                            AddEditPublicFragment.EventUploadCallback eventUploadCallback = new AddEditPublicFragment.EventUploadCallback() {
                                @Override
                                public void onComplete(String eventId) {
                                    AddEditPublicFragment.EventUploadCallback eventUploadCallback1 = new AddEditPublicFragment.EventUploadCallback() {
                                        @Override
                                        public void onComplete(String eventId) {
                                            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                            if (fragmentManager.getBackStackEntryCount() >= 1) {
                                                fragmentManager.popBackStackImmediate();
                                            } else {
                                                Log.d("Returning 1 fragments", "not 2 or more fragments in backstack", null);
                                            }
                                        }

                                        @Override
                                        public void onError(Exception e) {
                                            Log.d("UpdateUserDetails Error:", e.getMessage());
                                        }
                                    };
                                    updateUserDetails(user, username, profilePicUrl, bannerImageUrl, description, eventUploadCallback1);
                                }

                                @Override
                                public void onError(Exception e) {

                                }
                            };
                            uploadImagesToFirebase(user, username, email, eventUploadCallback);


                        } else {
                            Log.d("Input validation", "Inputs invalid");
                        }
                    });
                }
            }
        });
    }

    private void areEditInputsValid(View view, UserRepository.ValidationCallback callback) {
        ArrayList<String> inputs = getInputs();

        String username = inputs.get(0);
        String description = inputs.get(4);

        boolean isValid = true;

        if (username.isEmpty()) {
            usernameLayout.setError("Username cannot be empty");
            isValid = false;
        } else if (username.length() < 8) {
            usernameLayout.setError("Username must be 8 characters at least");
            isValid = false; // This line was missing to ensure isValid reflects this state.
        }else if (username.length() > 25) {
            usernameLayout.setError("Username must be less than 25 characters");
            isValid = false; // This line was missing to ensure isValid reflects this state.
        }else if(username.contains(" ")){
            usernameLayout.setError("Username cannot contain any spaces");
            isValid = false;
        }else if(!username.matches("^[a-zA-Z0-9_\\-]+$")) {
            usernameLayout.setError("Username can only contain letters, numbers, underscores, and dashes");
            isValid = false;
        } else if (Arrays.asList("admin", "root", "support").contains(username.toLowerCase())) {
            usernameLayout.setError("This username is reserved. Please choose another one.");
            isValid = false;
        } else {
            usernameLayout.setError(null); // Clear error
        }

        if (description.isEmpty()) {
            descriptionLayout.setError("Description cannot be empty");
            isValid = false;
        } else if (description.length() > 100) {
            descriptionLayout.setError("Description must be less than 100 characters");
            isValid = false;
        }

        if (!isValid) {
            // Immediate feedback, no need to check username uniqueness
            callback.onValidationResult(false);
            return;
        }

        // Proceed with asynchronous username check only if other validations pass
        isUsernameTaken(username, new UserRepository.UsernameCheckCallback() {
            @Override
            public void onUsernameChecked(boolean isTaken) {
                if (isTaken) {
                    if(!Objects.equals(currentUsername, username)){
                        usernameLayout.setError("Username is taken");
                        Log.d("isUsernameTaken", "Yes - Username is taken.");
                        callback.onValidationResult(false);
                    }else{
                        Log.d("isUsernameTaken", "Yes - however is the current users username");
                        callback.onValidationResult(true);
                    }
                } else {
                    Log.d("isUsernameTaken", "No - Username is not taken.");
                    callback.onValidationResult(true);
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("isUsernameTaken", "Error checking username: ", e);
                // Consider how to handle errors. Here, we assume validation failed.
                callback.onValidationResult(false);
            }
        });
    }



    private ArrayList<String> getInputs(){
        String username = usernameLayout.getEditText().getText().toString();
        String email = emailLayout.getEditText().getText().toString();
        String password = passwordLayout.getEditText().getText().toString();
        String confirmPassword = confirmPasswordLayout.getEditText().getText().toString();
        String description = String.valueOf(editTextDescription.getText());
        ArrayList<String> inputs = new ArrayList<String>();
        inputs.add(username);
        inputs.add(email);
        inputs.add(password);
        inputs.add(confirmPassword);
        inputs.add(description);
        return inputs;
    }

    private boolean areInputsValid(View view) {

        ArrayList<String> inputs = getInputs();

        username = inputs.get(0);
        email = inputs.get(1);
        password = inputs.get(2);
        confirmPassword = inputs.get(3);
        description = inputs.get(4);

        boolean isValid = true;

        if (username.isEmpty()) {
            usernameLayout.setError("Username cannot be empty");
            isValid = false;
        } else if (username.length()<8) {
            usernameLayout.setError("Username must be 8 characters at least");
            isValid = false;
        }else {
            usernameLayout.setError(null); // Clear error
        }
        final boolean[] takenUsername = {true};
        isUsernameTaken(username, new UserRepository.UsernameCheckCallback() {
            @Override
            public void onUsernameChecked(boolean isTaken) {
                if (isTaken) {
                    takenUsername[0] =false;
                    usernameLayout.setError("Username is taken");
                    Log.d("isUsernameTaken", "Yes - Username is taken.");
                } else {
                    Log.d("isUsernameTaken", "No - Username is not taken.");
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("isUsernameTaken", "Error checking username: ", e);
            }
        });

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
        if(passwordChecker(password))
        if (confirmPassword.isEmpty()) {
            confirmPasswordLayout.setError("Confirm password cannot be empty");
            isValid = false;
        } else {
            confirmPasswordLayout.setError(null);
        }
        if(!password.equals(confirmPassword)){
            passwordLayout.setError("Passwords must match");
            confirmPasswordLayout.setError("Passwords must match");
            isValid = false;
        }

        if(description.isEmpty()){
            Toast.makeText(getContext(), "Must enter a description", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        Boolean formatValid = true;
        if(username.length()>7){
            if(Objects.equals(confirmPassword, password)){
                if (passwordChecker(password)){
                    formatValid = true;
                }else{
                    Toast.makeText(getContext(), "Passwords must contain 1 capital, 1 symbol and be of 8 character minimum", Toast.LENGTH_LONG).show();
                    formatValid = false;
                }
            }else{
                Toast.makeText(getContext(), "Passwords must match", Toast.LENGTH_LONG).show();
                formatValid=false;
            }
        }else{
            Toast.makeText(getContext(), "Username must be at least 8 characters", Toast.LENGTH_LONG).show();
            formatValid = false;
        }
        if(profilePic==null){
            Toast.makeText(getContext(), "Must have a profile image", Toast.LENGTH_SHORT).show();
            formatValid = false;
        }


        boolean b = isValid && formatValid && takenUsername[0];
        return b;
    }




    public void setImageView(Context context, Uri imageUri, ImageView imageView, int code){
        if(code==1){
            Glide.with(context).load(imageUri).apply(RequestOptions.circleCropTransform()).into(imageView);
        }else{
            Glide.with(context).load(imageUri).apply(RequestOptions.centerInsideTransform()).into(imageView);
        }

    }

    private void createAccount(String username, String password,  String email) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser user = auth.getCurrentUser();
                            user.sendEmailVerification()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Toast.makeText(getContext(), "Email verification link sent", Toast.LENGTH_SHORT).show();
                                                waitForEmailVerification(user, username, email);
                                            }else{
                                                Log.d("Email not sent", task.getException().toString(), null);
                                            }
                                        }
                                    });
                        }else{
                            String exception = String.valueOf(task.getException());
                            Toast.makeText(getContext(), exception, Toast.LENGTH_SHORT).show();
                            try {
                                throw task.getException();
                            } catch(FirebaseAuthUserCollisionException e) {
                                // User with email already exists
                                Toast.makeText(getContext(), "Email already in use.", Toast.LENGTH_SHORT).show();
                            } catch(Exception e) {
                                // General error handling
                                Log.e(TAG, e.getMessage());
                            }
                        }
                    }
                });
    }


    private void updateUserDetails(FirebaseUser user, String username, String profileImageUrl, String bannerImageUrl, String description, AddEditPublicFragment.EventUploadCallback callback) {
        Log.d("In updateUserDetails", "start of Update User Details", null);
        Map<String, Object> map = new HashMap<>();
        map.put("username", username);
        if(updateProfPic){
            Log.d("Update Profile Pic", " " +profileImageUrl);
            map.put("profileImage", profileImageUrl);
        }
        if(updateBannerPic){
            map.put("bannerImage", bannerImageUrl);
        }
        map.put("description", description);
        FirebaseFirestore.getInstance().collection("Users").document(user.getUid())
                .update(map) // Use update for updating fields without overwriting the entire document.
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(getActivity(), "User details updated", Toast.LENGTH_SHORT).show();
                            callback.onComplete("complete");
                            // Consider calling a method here that signifies success, e.g., navigating the user to a new screen or refreshing data
                        }else{
                            Log.d("User Update Error", task.getException().getMessage(), null);
                            Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            callback.onError(task.getException());
                        }
                    }
                });
    }


    private void toLoginFragment() {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        SignupLoginFragment signupLoginFragment = new SignupLoginFragment();
        Bundle args = new Bundle();
        args.putString("deleteAccount", "yes");
        signupLoginFragment.setArguments(args);
        fragmentTransaction.replace(R.id.fragmentContainerView, signupLoginFragment)
                .commit();


    }


    private void uploadImagesToFirebase(final FirebaseUser user, String username, String email, AddEditPublicFragment.EventUploadCallback eventUploadCallback) {
        Log.d("Profile Image Upload: ", "in UploadImagesToFirebase", null);
        if (profilePic != null) {//check if profilePic is uploaded by user
            StorageReference profilePicRef = FirebaseStorage.getInstance().getReference("profileImages/" + user.getUid() + ".jpg");

            // Upload profile picture
            profilePicRef.putFile(profilePic)
                    .addOnSuccessListener(taskSnapshot -> profilePicRef.getDownloadUrl().addOnSuccessListener(profileImageUri -> {
                        // Profile picture uploaded successfully, now upload banner image
                        /*
                        if (bannerImage != null) {  //check if banner image exists
                            StorageReference bannerImageRef = FirebaseStorage.getInstance().getReference("bannerImages/" + user.getUid() + ".jpg");

                            // Upload banner image
                            bannerImageRef.putFile(bannerImage)
                                    .addOnSuccessListener(taskSnapshot2 -> bannerImageRef.getDownloadUrl().addOnSuccessListener(bannerImageUri -> {
                                        // Both images uploaded successfully, now call uploadUsers
                                        {
                                            if(eventUploadCallback==null){ //if uploading a new user
                                                profilePicUrl = profileImageUri.toString();
                                                Log.d("Profile Pic Url", profilePicUrl);
                                                SharedPreferences sharedPreferences = getContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
                                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                                editor.putBoolean("UserSetupComplete", true);
                                                editor.apply();
                                                bannerImageUrl = bannerImageUri.toString();
                                                uploadUser(user, username, email, profileImageUri.toString(), bannerImageUri.toString());
                                            }else{ // if updating a previous user
                                                profilePicUrl = profileImageUri.toString();
                                                Log.d("Profile Pic Url", profilePicUrl);
                                                eventUploadCallback.onComplete("Complete");
                                            }
                                        }
                                    }))
                                    .addOnFailureListener(e -> {
                                        // Unsuccessful banner image upload
                                        eventUploadCallback.onComplete("Complete");
                                        Log.d("Banner Image Upload Error: ", e.getMessage(), null);
                                        Toast.makeText(getContext(), "Banner Image Not Present", Toast.LENGTH_SHORT).show();
                                    });
                        }else{

                         */
                        if(eventUploadCallback==null){
                            uploadUser(user, username, email, profileImageUri.toString(), null);
                        }else{
                            profilePicUrl = profileImageUri.toString();
                            Log.d("Profile Pic Url", profilePicUrl);
                            eventUploadCallback.onComplete("Complete");
                        }

                    }))
                    .addOnFailureListener(e -> {
                        // Handle unsuccessful profile picture upload
                        if(eventUploadCallback!= null){
                            eventUploadCallback.onComplete("Complete");
                        }
                        uploadUser(user, username, email, null, null);

                        Log.d("Profile Image Upload Error: ", e.getMessage(), null);
                        Toast.makeText(getContext(), "Profile Image Upload Error", Toast.LENGTH_SHORT).show();
                    });
        }else{
            if(eventUploadCallback!=null){
                eventUploadCallback.onComplete("Complete");
            }
        }
    }
    private void uploadUser(FirebaseUser user, String username, String email, String profileImageUrl, String bannerImageUrl) {
        Log.d("In uploadUser", "start of Upload User", null);
        Map<String, Object> map = new HashMap<>();
        map.put("username", username);
        map.put("email", email);
        map.put("profileImage", profileImageUrl);
        map.put("bannerImage", bannerImageUrl);
        map.put("description", description);
        map.put("uid", user.getUid());

        FirebaseFirestore.getInstance().collection("Users").document(user.getUid())
                .set(map)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d("User Sign Up", "Created successfully", null);
                            //Toast.makeText(getActivity(), "Event User created", Toast.LENGTH_SHORT).show();
                            login();
                        }else{
                            Log.d("User Sign Up", task.getException().getMessage(), null);
                            //Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    }
                });


    }

    private void waitForEmailVerification(FirebaseUser user, String username, String email) {
        emailVerifiedTextView.setVisibility(VISIBLE);
        emailLayout.setVisibility(GONE);
        usernameLayout.setVisibility(GONE);
        passwordLayout.setVisibility(GONE);
        confirmPasswordLayout.setVisibility(GONE);
        editTextDescription.setVisibility(GONE);
        descriptionLayout.setVisibility(GONE);
        signUpButton.setVisibility(GONE);
        emailVerifiedTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Force a refresh of the user's profile to update the emailVerified flag
                user.reload().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Now check if the email is verified
                        if (user.isEmailVerified()) {
                            // Update SharedPreferences to reflect that the email is verified
                            SharedPreferences sharedPreferences = getContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean("EmailVerified", true);

                            // Upload the user
                            uploadImagesToFirebase(user, username, email, null);
                            editor.putBoolean("UserSetupComplete", true);
                            editor.apply();

                        } else {
                            Toast.makeText(getContext(), "Please verify your email.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Handle failure to reload the user's profile
                        Toast.makeText(getContext(), "Failed to reload user profile.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });



    }

    private void showPromptDialog(int code) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        if(code==0){
            builder.setTitle("Logging out")
                    .setMessage("Are you sure you would like to sign your account out?")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            FirebaseAuth mAuth = FirebaseAuth.getInstance();
                            mAuth.signOut();
                            Intent intent = new Intent(getContext(), LoginRegisterActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear the activity stack
                            startActivity(intent);

                            if (getActivity() != null) {
                                getActivity().finish();
                            }
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    })
                    .show();
        }else{
            builder.setTitle("Account deletion")
                    .setMessage("Are you sure you would like to delete your account?")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            toLoginFragment();
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    })
                    .show();
        }


    }
    private void login() {
        if(getContext()!=null){
            Intent intent = new Intent(getContext(), MainActivity.class);
            startActivity(intent);
            if(getActivity()!=null){
                getActivity().finish();
            }
        }

    }

    private boolean passwordChecker(String password) {
        String pattern = "^(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?])(?=\\S+$).{8,}$";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(password);
        return matcher.matches();
    }


}