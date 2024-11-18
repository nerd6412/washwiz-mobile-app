package com.example.washwiz;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordScreen extends AppCompatActivity {

    private EditText emailEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        emailEditText = findViewById(R.id.forgot_email);
        Button submitButton = findViewById(R.id.forgot_submit);

        submitButton.setOnClickListener(view -> {
            String email = emailEditText.getText().toString();

            if (email.isEmpty()) {
                Toast.makeText(ForgotPasswordScreen.this, "Please enter your email address", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseAuth auth = FirebaseAuth.getInstance();
            auth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Password reset email sent!", Toast.LENGTH_SHORT).show();
                    emailEditText.setText("");
                    Intent intent = new Intent(ForgotPasswordScreen.this, LoginScreen.class);
                    startActivity(intent);
                    finish();
                } else {
                    Log.w(TAG, "sendPasswordResetEmail:failure", task.getException());
                    Toast.makeText(ForgotPasswordScreen.this, "Error sending email", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}