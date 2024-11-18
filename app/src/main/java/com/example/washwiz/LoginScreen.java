package com.example.washwiz;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
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

public class LoginScreen extends AppCompatActivity {

      EditText loginEmail, loginPassword;
      Button loginButton;
      TextView signupLink, forgotPasswordLink;
      FirebaseAuth auth;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
        signupLink = findViewById(R.id.signup_link);

        auth = FirebaseAuth.getInstance();

        // Check if the user is already logged in
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            // User is logged in, redirect to home screen
            Intent intent = new Intent(LoginScreen.this, HomeScreen.class);
            startActivity(intent);
            finish();  // Prevent going back to the login screen
        }

        final boolean[] isPasswordVisible = {false};

        loginPassword.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                int drawableRight = 2; // Index for the right drawable
                if (loginPassword.getCompoundDrawables()[drawableRight] != null) {
                    float touchPointX = event.getRawX();
                    float drawableWidth = loginPassword.getRight() - loginPassword.getCompoundDrawables()[drawableRight].getBounds().width();

                    if (touchPointX >= drawableWidth) {
                        // Toggle password visibility
                        isPasswordVisible[0] = !isPasswordVisible[0];
                        if (isPasswordVisible[0]) {
                            loginPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        } else {
                            loginPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        }
                        // Move cursor to the end
                        loginPassword.setSelection(loginPassword.getText().length());

                        // Call performClick to resolve warning
                        loginPassword.performClick();
                        return true; // Indicate that the touch was handled
                    }
                }
            }
            return false; // Pass the touch event to other listeners
        });

        // Override performClick for the EditText to resolve warning
        loginPassword.setOnClickListener(v -> {
            // Optional: handle click event if necessary
        });


        loginButton.setOnClickListener(view -> {
            String email = loginEmail.getText().toString();
            String password = loginPassword.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginScreen.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                // Exit the method if fields are empty
            }

            FirebaseAuth auth = FirebaseAuth.getInstance();
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Login successful
                    loginEmail.setText("");
                    loginPassword.setText("");
                    Toast.makeText(LoginScreen.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginScreen.this, HomeScreen.class);
                    startActivity(intent);
                    finish();
                } else {
                    // Login failed
                    Toast.makeText(LoginScreen.this, "Invalid email or password.", Toast.LENGTH_SHORT).show();
                }
            });
        });

        signupLink.setOnClickListener(view -> {
            Intent intent = new Intent(LoginScreen.this, SignUpScreen.class);
            startActivity(intent);
            finish();
        });

        forgotPasswordLink = findViewById(R.id.forgot_password_link);
        forgotPasswordLink.setOnClickListener(view -> {
            Intent intent = new Intent(LoginScreen.this, ForgotPasswordScreen.class);
            startActivity(intent);
        });
    }
}