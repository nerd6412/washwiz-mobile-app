package com.example.washwiz;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

public class FeedbackScreen extends AppCompatActivity {

    private RatingBar ratingBar;
    private EditText commentEditText;
    private DatabaseReference feedbackRef;
    private List<ToggleButton> categoryToggleButtons;

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

        categoryToggleButtons = new ArrayList<>();
        categoryToggleButtons.add(findViewById(R.id.tagCleanFreshClothes));
        categoryToggleButtons.add(findViewById(R.id.tagQuickService));
        categoryToggleButtons.add(findViewById(R.id.tagNoDamage));
        categoryToggleButtons.add(findViewById(R.id.tagOnTimePickup));
        categoryToggleButtons.add(findViewById(R.id.tagOnTimeDelivery));
        categoryToggleButtons.add(findViewById(R.id.tagEasyToUse));
        categoryToggleButtons.add(findViewById(R.id.tagHighlyRecommend));
        categoryToggleButtons.add(findViewById(R.id.tagEasyScheduling));

        ToggleButton category1 = findViewById(R.id.tagCleanFreshClothes);
        ToggleButton category2 = findViewById(R.id.tagQuickService);
        ToggleButton category3 = findViewById(R.id.tagNoDamage);
        ToggleButton category4 = findViewById(R.id.tagOnTimePickup);
        ToggleButton category5 = findViewById(R.id.tagOnTimeDelivery);
        ToggleButton category6 = findViewById(R.id.tagEasyToUse);
        ToggleButton category7 = findViewById(R.id.tagHighlyRecommend);
        ToggleButton category8 = findViewById(R.id.tagEasyScheduling);

        category1.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Change text color when toggled on
                category1.setTextColor(ContextCompat.getColor(this, R.color.white));
                category1.setBackgroundColor(ContextCompat.getColor(this, R.color.blue));
            } else {
                // Change text color when toggled off
                category1.setTextColor(ContextCompat.getColor(this, R.color.blue));
                category1.setBackgroundResource(R.drawable.custom_edittext);
            }
        });

        category2.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Change text color when toggled on
                category2.setTextColor(ContextCompat.getColor(this, R.color.white));
                category2.setBackgroundColor(ContextCompat.getColor(this, R.color.blue));
            } else {
                // Change text color when toggled off
                category2.setTextColor(ContextCompat.getColor(this, R.color.blue));
                category2.setBackgroundResource(R.drawable.custom_edittext);
            }
        });

        category3.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Change text color when toggled on
                category3.setTextColor(ContextCompat.getColor(this, R.color.white));
                category3.setBackgroundColor(ContextCompat.getColor(this, R.color.blue));
            } else {
                // Change text color when toggled off
                category3.setTextColor(ContextCompat.getColor(this, R.color.blue));
                category3.setBackgroundResource(R.drawable.custom_edittext);
            }
        });

        category4.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Change text color when toggled on
                category4.setTextColor(ContextCompat.getColor(this, R.color.white));
                category4.setBackgroundColor(ContextCompat.getColor(this, R.color.blue));
            } else {
                // Change text color when toggled off
                category4.setTextColor(ContextCompat.getColor(this, R.color.blue));
                category4.setBackgroundResource(R.drawable.custom_edittext);
            }
        });

        category5.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Change text color when toggled on
                category5.setTextColor(ContextCompat.getColor(this, R.color.white));
                category5.setBackgroundColor(ContextCompat.getColor(this, R.color.blue));
            } else {
                // Change text color when toggled off
                category5.setTextColor(ContextCompat.getColor(this, R.color.blue));
                category5.setBackgroundResource(R.drawable.custom_edittext);
            }
        });

        category6.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Change text color when toggled on
                category6.setTextColor(ContextCompat.getColor(this, R.color.white));
                category6.setBackgroundColor(ContextCompat.getColor(this, R.color.blue));
            } else {
                // Change text color when toggled off
                category6.setTextColor(ContextCompat.getColor(this, R.color.blue));
                category6.setBackgroundResource(R.drawable.custom_edittext);
            }
        });

        category7.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Change text color when toggled on
                category7.setTextColor(ContextCompat.getColor(this, R.color.white));
                category7.setBackgroundColor(ContextCompat.getColor(this, R.color.blue));
            } else {
                // Change text color when toggled off
                category7.setTextColor(ContextCompat.getColor(this, R.color.blue));
                category7.setBackgroundResource(R.drawable.custom_edittext);
            }
        });

        category8.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Change text color when toggled on
                category8.setTextColor(ContextCompat.getColor(this, R.color.white));
                category8.setBackgroundColor(ContextCompat.getColor(this, R.color.blue));
            } else {
                // Change text color when toggled off
                category8.setTextColor(ContextCompat.getColor(this, R.color.blue));
                category8.setBackgroundResource(R.drawable.custom_edittext);
            }
        });

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

        // Get selected feedback categories
        List<String> selectedCategories = new ArrayList<>();
        for (ToggleButton toggleButton : categoryToggleButtons) {
            if (toggleButton.isChecked()) {
                selectedCategories.add(toggleButton.getTextOn().toString());
            }
        }

        // Prepare feedback data
        Map<String, Object> feedbackData = new HashMap<>();
        feedbackData.put("userID", userID);
        feedbackData.put("rating", rating);
        feedbackData.put("comment", comment);
        feedbackData.put("feedbackTimestamp", formattedDate);
        feedbackData.put("categories", selectedCategories); // Add categories to the database

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