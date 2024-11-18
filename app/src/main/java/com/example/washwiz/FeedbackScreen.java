package com.example.washwiz;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

public class FeedbackScreen extends AppCompatActivity {

    private RatingBar ratingBar;
    private EditText commentEditText;
    private DatabaseReference feedbackRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_feedback_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ratingBar = findViewById(R.id.ratingBar);
        commentEditText = findViewById(R.id.commentEditText);
        Button submitButton = findViewById(R.id.submitButton);

        // Get user ID and create a reference for feedback
        String userID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        feedbackRef = FirebaseDatabase.getInstance().getReference("UserFeedback");

        submitButton.setOnClickListener(v -> submitFeedback(userID));
    }

    private void submitFeedback(String userID) {
        float rating = ratingBar.getRating();
        String comment = commentEditText.getText().toString().trim();
        long timestamp = System.currentTimeMillis();

        // Format the timestamp to ISO 8601 format
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        String formattedDate = sdf.format(new Date(timestamp));

        // Prepare feedback data
        Map<String, Object> feedbackData = new HashMap<>();
        feedbackData.put("userID", userID);
        feedbackData.put("rating", rating);
        feedbackData.put("comment", comment);
        feedbackData.put("feedbackTimestamp", formattedDate);

        // Save feedback to Firebase
        feedbackRef.push().setValue(feedbackData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(FeedbackScreen.this, "Feedback submitted!", Toast.LENGTH_SHORT).show();
                finish(); // Close feedback activity
            } else {
                Toast.makeText(FeedbackScreen.this, "Failed to submit feedback.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}