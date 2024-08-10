package com.example.calendarapp.SignUpLogin;

import static android.view.View.VISIBLE;

import static com.example.calendarapp.OnlineDb.UserRepository.isUsernameTaken;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
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
import com.example.calendarapp.OnlineDb.UserRepository;
import com.example.calendarapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
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


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PersonalSignupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PersonalSignupFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ImageView profilePic;
    private String username, firstName, lastName, email, password, confirmPassword, bio, profilePicUrl, bannerImageUrl;
    private TextInputEditText editTextFirstName, editTextLastName, editTextEmail, editTextUsername, editTextPassword, editTextConfirmPassword;
    private EditText editTextBio;
    private MaterialButton signUpButton;
    private ActivityResultLauncher<Intent> imagePickLauncher;
    private Uri selectedImageUri, profileImage, bannerImage;
    public static final String EMAIL_REGEX ="^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$";

    private FirebaseAuth auth;
    private FirebaseDatabase db;
    private DatabaseReference ref;
    private TextView emailVerificationTextView;
    private boolean newProfile;
    private FirebaseUser user;
    private TextInputLayout usernameLayout, emailLayout, passwordLayout, confirmPasswordLayout, textInputLayoutFirstName, textInputLayoutLastName;

    public PersonalSignupFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PersonalLoginFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PersonalSignupFragment newInstance(String param1, String param2) {
        PersonalSignupFragment fragment = new PersonalSignupFragment();
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
        View view = inflater.inflate(R.layout.fragment_personal_signup, container, false);

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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> inputs = getInputs();
                if(newProfile){
                    if(areInputsValid(view)){
                        createAccount(username, password, firstName, lastName);
                    }else{
                        Toast.makeText(getContext(), "Inputs are invalid", Toast.LENGTH_SHORT);
                        Log.d("PersonalSignupFragment", "Inputs are invalid");
                    }
                }else{
                    if(areEditInputsValid(view, inputs)){
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

                                    }
                                };
                                updateUserDetails(user, firstName, lastName, username, profilePicUrl, bannerImageUrl, bio, eventUploadCallback1);
                            }

                            @Override
                            public void onError(Exception e) {

                            }
                        };
                        uploadImagesToFirebase(user, username, email, eventUploadCallback);
                    }
                }
            }
        });
    }

    private void updateUserDetails(FirebaseUser user, String firstName, String lastName, String username, String profileImageUrl, String bannerImageUrl, String bio, AddEditPublicFragment.EventUploadCallback callback) {
        Log.d("In updateUserDetails", "start of Update User Details", null);
        Map<String, Object> map = new HashMap<>();
        map.put("firstName", firstName);
        map.put("lastName", lastName);
        map.put("username", username);
        map.put("profileImage", profileImageUrl);
        map.put("bannerImage", bannerImageUrl);
        map.put("bio", bio);
        // Assuming the password handling is done elsewhere through Firebase Auth.
        FirebaseFirestore.getInstance().collection("Users").document(user.getUid())
                .update(map) // Use update instead of set for updating fields without overwriting the entire document.
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


    private boolean areEditInputsValid(View view, ArrayList<String> inputs) {
        String firstName = inputs.get(0);
        String lastName = inputs.get(1);
        String username = inputs.get(2);
        String bio = inputs.get(6);

        boolean isValid = true;

        if(firstName.isEmpty()){
            textInputLayoutFirstName.setError("Provide a first name");
            isValid=false;
        }else{
            textInputLayoutFirstName.setError(null);
        }
        if(lastName.isEmpty()){
            textInputLayoutLastName.setError("Provide a first name");
            isValid=false;
        }else{
            textInputLayoutLastName.setError(null);
        }
        if (username.isEmpty()) {
            usernameLayout.setError("Username cannot be empty");
            isValid = false;
        } else if (username.length()<8) {
            usernameLayout.setError("Username must be 8 characters at least");
        } else {
        usernameLayout.setError(null); // Clear error
        }

        final boolean[] formatValid = {true};
        isUsernameTaken("desiredUsername", new UserRepository.UsernameCheckCallback() {
            @Override
            public void onUsernameChecked(boolean isTaken) {
                if (isTaken) {
                    formatValid[0] =false;
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


        if(bio.isEmpty()){
            Toast.makeText(getContext(), "Must enter a bio", Toast.LENGTH_SHORT).show();
            isValid = false;
        }




        return isValid && formatValid[0];
    }

    private boolean areInputsValid(View view) {
        ArrayList<String> inputs = getInputs();
        String firstName = inputs.get(0);
        String lastName = inputs.get(1);
        String username = inputs.get(2);
        String email = inputs.get(3);
        String password = inputs.get(4);
        String confirmPassword = inputs.get(5);
        String description = inputs.get(6);

        boolean isValid = true;

        if(firstName.isEmpty()){
            textInputLayoutFirstName.setError("Provide a first name");
            isValid=false;
        }else{
            textInputLayoutFirstName.setError(null);
        }
        if(lastName.isEmpty()){
            textInputLayoutLastName.setError("Provide a first name");
            isValid=false;
        }else{
            textInputLayoutLastName.setError(null);
        }
        if (username.isEmpty()) {
            usernameLayout.setError("Username cannot be empty");
            isValid = false;
        } else if (username.length()<8) {
            usernameLayout.setError("Username must be 8 characters at least");
        //}else if (isUsernameTaken(username)) {
            //usernameLayout.setError("Username is taken");
        } else {
            usernameLayout.setError(null); // Clear error
        }
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

        boolean b = isValid && formatValid;
        return b;
    }


    private ArrayList<String> getInputs(){
        String firstName = textInputLayoutFirstName.getEditText().getText().toString();
        String lastName = textInputLayoutLastName.getEditText().getText().toString();
        String username = usernameLayout.getEditText().getText().toString();
        String email = emailLayout.getEditText().getText().toString();
        String password = passwordLayout.getEditText().getText().toString();
        String confirmPassword = confirmPasswordLayout.getEditText().getText().toString();
        String bio = String.valueOf(editTextBio.getText());
        ArrayList<String> inputs = new ArrayList<String>();
        inputs.add(firstName);
        inputs.add(lastName);
        inputs.add(username);
        inputs.add(email);
        inputs.add(password);
        inputs.add(confirmPassword);
        inputs.add(bio);
        return inputs;
    }

    private void setOnClickListeners() {

        profilePic.setOnClickListener((v)->{
            ImagePicker.with(this).cropSquare().compress(512).maxResultSize(512,512)
                    .createIntent(new Function1<Intent, Unit>() {
                        @Override
                        public Unit invoke(Intent intent) {
                            imagePickLauncher.launch(intent);
                            return null;
                        }
                    });
        });

        imagePickLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if(result.getResultCode() == Activity.RESULT_OK){
                        Intent data = result.getData();
                        if(data!=null && data.getData()!=null){
                            selectedImageUri = data.getData();
                            setProfilePic(getContext(),selectedImageUri,profilePic);
                        }
                    }
                }
        );
    }


    public void init(View view){
        editTextFirstName = view.findViewById(R.id.editTextFirstName);
        editTextLastName = view.findViewById(R.id.editTextLastName);
        editTextEmail= view.findViewById(R.id.editTextEmail);
        editTextUsername = view.findViewById(R.id.editTextUsername);
        editTextPassword = view.findViewById(R.id.editTextPassword);
        editTextBio = view.findViewById(R.id.editTextBio);
        editTextConfirmPassword = view.findViewById(R.id.editTextConfirmPassword);
        editTextBio = view.findViewById(R.id.editTextBio);
        signUpButton = view.findViewById(R.id.signUpButton);
        profilePic = view.findViewById(R.id.profile_pic);
        emailVerificationTextView = view.findViewById(R.id.emailVerificationTextView);
        usernameLayout = view.findViewById(R.id.textInputLayoutUsername);
        emailLayout = view.findViewById(R.id.textInputLayoutEmail);
        passwordLayout = view.findViewById(R.id.textInputLayoutPassword);
        confirmPasswordLayout = view.findViewById(R.id.textInputLayoutConfirmPassword);
        textInputLayoutFirstName = view.findViewById(R.id.textInputLayoutFirstName);
        textInputLayoutLastName = view.findViewById(R.id.textInputLayoutLastName);


        if(!newProfile){
            editTextEmail.setVisibility(View.GONE);
            editTextPassword.setVisibility(View.GONE);
            editTextConfirmPassword.setVisibility(View.GONE);
            signUpButton.setText("Update");
            loadProfileData(view);
        }
    }

    private void loadProfileData(View view) {
        DocumentReference userRef = FirebaseFirestore.getInstance().collection("privUsers")
                .document(user.getUid());
        userRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error!=null){
                    Toast.makeText(getContext(),"Unable to load user details", Toast.LENGTH_LONG);
                    return;
                }if(value.exists()){
                    String fName = value.getString("firstName");
                    String lName = value.getString("lastName");
                    String username = value.getString("username");
                    String bio = value.getString("bio");
                    editTextFirstName.setText(fName);
                    editTextLastName.setText(lName);
                    editTextBio.setText(bio);
                    editTextUsername.setText(username);
                    String profileImageUrl = value.getString("profileImage");
                    if (profileImageUrl != null) {
                        loadImageIntoView(profileImageUrl, view, profilePic);
                    }
                }
            }

        });
    }

    private void createAccount(String username, String password, String firstName, String lastName) {
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
                                                    toLoginFragment();
                                                }
                                            }
                                        });
                            uploadUser(user, firstName, lastName, username, email);
                        }else{
                            String exception = String.valueOf(task.getException());
                            Toast.makeText(getContext(), exception, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void toLoginFragment() {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        SignupLoginFragment signupLoginFragment = new SignupLoginFragment();
        fragmentTransaction.replace(R.id.fragmentContainerView, signupLoginFragment);
    }

    private void waitForEmailVerification(FirebaseUser user, String username, String email) {
        emailVerificationTextView.setVisibility(VISIBLE);
        emailVerificationTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Force a refresh of the user's profile to update the emailVerified flag
                user.reload().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Now check if the email is verified
                        if (user.isEmailVerified()) {
                            // Email is verified, you can now upload the user
                            uploadImagesToFirebase(user, username, email, null);
                        } else {
                            // Email not verified yet, prompt the user to check their email again or resend the verification email
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

    private void uploadImagesToFirebase(FirebaseUser user, String username, String email, AddEditPublicFragment.EventUploadCallback eventUploadCallback) {
        Log.d("Profile Image Upload: ", "in UploadImagesToFirebase", null);
        if (profileImage != null) {
            StorageReference profilePicRef = FirebaseStorage.getInstance().getReference("profileImages/" + user.getUid() + ".jpg");

            // Upload profile picture
            profilePicRef.putFile(profileImage)
                    .addOnSuccessListener(taskSnapshot -> profilePicRef.getDownloadUrl().addOnSuccessListener(profileImageUri -> {
                        // Profile picture uploaded successfully, now upload banner image
                        if (bannerImage != null) {
                            StorageReference bannerImageRef = FirebaseStorage.getInstance().getReference("bannerImages/" + user.getUid() + ".jpg");

                            // Upload banner image
                            bannerImageRef.putFile(bannerImage)
                                    .addOnSuccessListener(taskSnapshot2 -> bannerImageRef.getDownloadUrl().addOnSuccessListener(bannerImageUri -> {
                                        // Both images uploaded successfully, now call uploadUsers
                                        {
                                            if(eventUploadCallback==null){
                                                profilePicUrl = profileImageUri.toString();
                                                bannerImageUrl = bannerImageUri.toString();
                                                uploadUser(user, username, email, profilePicUrl, bannerImageUrl);
                                            }else{
                                                eventUploadCallback.onComplete("Complete");
                                            }
                                        }
                                    }))
                                    .addOnFailureListener(e -> {
                                        // Handle unsuccessful banner image upload
                                        eventUploadCallback.onComplete("Complete");
                                        Log.d("Banner Image Upload Error: ", e.getMessage(), null);
                                        Toast.makeText(getContext(), "Banner Image Not Present", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }))
                    .addOnFailureListener(e -> {
                        // Handle unsuccessful profile picture upload
                        eventUploadCallback.onComplete("Complete");
                        Log.d("Profile Image Upload Error: ", e.getMessage(), null);
                        Toast.makeText(getContext(), "Profile Image Not Present", Toast.LENGTH_SHORT).show();
                    });
        }else{
            if(eventUploadCallback!=null){
                eventUploadCallback.onComplete("Complete");
            }
        }
    }

    private void uploadUser(FirebaseUser user, String username, String email, String profileUrl, String bannerUrl) {
        Map<String, Object> map = new HashMap<>();
        map.put("username", username);
        map.put("firstName", firstName);
        map.put("lastName", lastName);
        map.put("email", email);
        map.put("profileImage", profileUrl);
        map.put("bannerImage", bannerUrl);
        map.put("bio", bio);
        map.put("uid", user.getUid());

        FirebaseFirestore.getInstance().collection("privUsers").document(user.getUid())
                .set(map)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                   @Override
                   public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(getContext(), "Personal User created", Toast.LENGTH_SHORT).show();
                        login();
                    }else{
                        Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                    }
                   }
               });



    }

    private void login() {
        Intent intent = new Intent(getContext(), MainActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    private boolean passwordChecker(String password) {
        String pattern = "^(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?])(?=\\S+$).{8,}$";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(password);
        return matcher.matches();
    }
    private ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    // Handle the selected image URI
                    profilePic.setImageURI(uri);
                    selectedImageUri = uri;
                }
            });

    public void setProfilePic(Context context, Uri imageUri, ImageView imageView){
        Glide.with(context).load(imageUri).apply(RequestOptions.circleCropTransform()).into(imageView);
    }

    private void loadImageIntoView(String imageUrl, View view, ImageView imageView){
        Glide.with(this)
                .load(imageUrl)
                .into(imageView);
    }



}