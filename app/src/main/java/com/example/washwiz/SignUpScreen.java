package com.example.washwiz;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class SignUpScreen extends AppCompatActivity {

    EditText signupName, signupEmail, signupPassword, signupContact, signupAddress;
    TextView loginLink;
    Button signupButton;
    FirebaseDatabase db;
    DatabaseReference reference;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        signupName = findViewById(R.id.signup_name);
        signupEmail = findViewById(R.id.signup_email);
        signupPassword = findViewById(R.id.signup_password);
        signupContact = findViewById(R.id.signup_contact);
        signupAddress = findViewById(R.id.signup_address);
        signupButton = findViewById(R.id.register_button);
        loginLink = findViewById(R.id.login_link);

        int maxLength = 11;
        signupContact.setFilters(new InputFilter[] { new InputFilter.LengthFilter(maxLength) });

        final boolean[] isPasswordVisible = {false};

        signupPassword.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                int drawableRight = 2; // Index for the right drawable
                if (signupPassword.getCompoundDrawables()[drawableRight] != null) {
                    float touchPointX = event.getRawX();
                    float drawableWidth = signupPassword.getRight() - signupPassword.getCompoundDrawables()[drawableRight].getBounds().width();

                    if (touchPointX >= drawableWidth) {
                        // Toggle password visibility
                        isPasswordVisible[0] = !isPasswordVisible[0];
                        if (isPasswordVisible[0]) {
                            signupPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        } else {
                            signupPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        }
                        // Move cursor to the end
                        signupPassword.setSelection(signupPassword.getText().length());

                        // Call performClick to resolve warning
                        signupPassword.performClick();
                        return true; // Indicate that the touch was handled
                    }
                }
            }
            return false; // Pass the touch event to other listeners
        });

        // Override performClick for the EditText to resolve warning
        signupPassword.setOnClickListener(v -> {
            // Optional: handle click event if necessary
        });

        signupButton.setOnClickListener(view -> {

            String name = signupName.getText().toString();
            String email = signupEmail.getText().toString();
            String password = signupPassword.getText().toString();
            String contact = signupContact.getText().toString();
            String address = signupAddress.getText().toString();

            if(name.isEmpty() || email.isEmpty() || password.isEmpty() || contact.isEmpty() || address.isEmpty()) {
                Toast.makeText(SignUpScreen.this, "Please fill all the required fields", Toast.LENGTH_SHORT).show();
            } else if(password.length() < 8) {
                Toast.makeText(SignUpScreen.this, "Password must be at least 8 characters long", Toast.LENGTH_SHORT).show();
            } else {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        Users user = new Users(name, email, contact, address);

                        db = FirebaseDatabase.getInstance();
                        reference = db.getReference("Users");
                        String userID = Objects.requireNonNull(auth.getCurrentUser()).getUid();

                        reference.child(userID).setValue(user).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                signupName.setText("");
                                signupEmail.setText("");
                                signupPassword.setText("");
                                signupContact.setText("");
                                signupAddress.setText("");

                                FirebaseUser currentUser = auth.getCurrentUser();
                                currentUser.sendEmailVerification().addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful()) {
                                        Toast.makeText(this, "Verification Email Sent!", Toast.LENGTH_SHORT).show();
                                    }
                                });

                                Toast.makeText(SignUpScreen.this, "Successfully signed up!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(SignUpScreen.this, TermsNConditionsScreen.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(SignUpScreen.this, "Error saving user data: " + Objects.requireNonNull(task1.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        });

        loginLink.setOnClickListener(view -> {
            Intent intent = new Intent(SignUpScreen.this, LoginScreen.class);
            startActivity(intent);
        });
    }
}