package com.example.washwiz;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RewardsScreen extends AppCompatActivity {

    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_rewards_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseRef = FirebaseDatabase.getInstance().getReference();

        Button tenPts = findViewById(R.id.tenPtsAvailBtn);
        Button twentyPts = findViewById(R.id.twentyPtsAvailBtn);
        Button thirtyPts = findViewById(R.id.thirtyPtsAvailBtn);
        Button fortyPts = findViewById(R.id.fortyPtsAvailBtn);
        Button fiftyPts = findViewById(R.id.fiftyPtsAvailBtn);
        Button sixtyPts = findViewById(R.id.sixtyPtsAvailBtn);
        Button seventyPts = findViewById(R.id.seventyPtsAvailBtn);
        Button eightyPts = findViewById(R.id.eightyPtsAvailBtn);
        Button ninetyPts = findViewById(R.id.ninetyPtsAvailBtn);
        Button hundredPts = findViewById(R.id.hundredPtsAvailBtn);

        TextView homelink = findViewById(R.id.home_link);
        homelink.setOnClickListener(v -> {
            Intent intent = new Intent(RewardsScreen.this, HomeScreen.class);
            startActivity(intent);
        });

        int rewardsPoints = 0; // Default value in case of an exception
        try {
            rewardsPoints = getIntent().getIntExtra("rewardsPoints", 0);

            TextView rewardsPtsText = findViewById(R.id.rewardsPts_display);
            if (rewardsPtsText != null) {
                if (rewardsPoints > 0) { // Check if rewardsPoints is greater than zero
                    rewardsPtsText.setText(String.valueOf(rewardsPoints)); // Display the points
                } else {
                    rewardsPtsText.setText(R.string.no_rewards_available);
                }
            } else {
                Log.e("RewardsScreen", "TextView rewardsPts_display is null");
            }
        } catch (NumberFormatException e) {
            Log.e("RewardsScreen", "Error parsing rewards points", e); // Use Log.e() for logging the exception
        }

        tenPts.setOnClickListener(v -> availDiscount(10, 4));
        twentyPts.setOnClickListener(v -> availDiscount(20, 6));
        thirtyPts.setOnClickListener(v -> availDiscount(30, 8));
        fortyPts.setOnClickListener(v -> availDiscount(40, 10));
        fiftyPts.setOnClickListener(v -> availDiscount(50, 12));
        sixtyPts.setOnClickListener(v -> availDiscount(60, 14));
        seventyPts.setOnClickListener(v -> availDiscount(70, 16));
        eightyPts.setOnClickListener(v -> availDiscount(80, 18));
        ninetyPts.setOnClickListener(v -> availDiscount(90, 20));
        hundredPts.setOnClickListener(v -> availDiscount(100, 22));

        enableButtonsBasedOnPoints(rewardsPoints, tenPts, twentyPts, thirtyPts, fortyPts, fiftyPts,
                sixtyPts, seventyPts, eightyPts, ninetyPts, hundredPts);
    }

    private void availDiscount(int points, int discountPercent) {
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        DatabaseReference discountRef = databaseRef.child("UserDiscounts").child(userId);

        Map<String, Object> discountData = new HashMap<>();
        discountData.put("points", points);
        discountData.put("discountPercent", discountPercent);
        discountData.put("isActive", true);

        discountRef.setValue(discountData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(RewardsScreen.this, "Discount availed successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(RewardsScreen.this, "Failed to avail discount.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enableButtonsBasedOnPoints(int rewardsPoints, Button... buttons) {
        Button[] pointsButtons = {buttons[0], buttons[1], buttons[2], buttons[3], buttons[4],
                buttons[5], buttons[6], buttons[7], buttons[8], buttons[9]};

        int[] pointsRequired = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100};

        for (int i = 0; i < pointsButtons.length; i++) {
            if (rewardsPoints >= pointsRequired[i]) {
                pointsButtons[i].setEnabled(true);
                pointsButtons[i].setAlpha(1f);  // Make button appear fully visible
            } else {
                pointsButtons[i].setEnabled(false);
                pointsButtons[i].setAlpha(0.5f); // Make disabled button appear dimmed
            }
        }
    }

}