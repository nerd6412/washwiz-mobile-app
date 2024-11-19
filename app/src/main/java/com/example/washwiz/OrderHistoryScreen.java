package com.example.washwiz;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class OrderHistoryScreen extends AppCompatActivity {

    private HistoryAdapter historyAdapter;
    private List<History> historyList;
    private TextView rewardsPointsText;
    private int rewardsPoints = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_history_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userID = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference().child("Orders");

        rewardsPointsText = findViewById(R.id.rewards_points_text);
        ImageView rewardsIcon = findViewById(R.id.rewards_icon);

        RecyclerView recyclerView = findViewById(R.id.recycler_view_history);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        historyList = new ArrayList<>();
        historyAdapter = new HistoryAdapter(historyList);
        recyclerView.setAdapter(historyAdapter);

        loadOrders(ordersRef, userID);
        calculateRewardsPoints(ordersRef, userID);

        // Set rewards icon click listener to open rewards page
        rewardsIcon.setOnClickListener(view -> {
            Intent intent = new Intent(OrderHistoryScreen.this, RewardsScreen.class);
            intent.putExtra("rewardsPoints", rewardsPoints);
            startActivity(intent);
        });

        TextView homeLinkBtn = findViewById(R.id.home_link);

        homeLinkBtn.setOnClickListener(view -> {
            Intent intent = new Intent(OrderHistoryScreen.this, HomeScreen.class);
            startActivity(intent);
            finish();
        });
    }

    private void loadOrders(DatabaseReference ordersRef, String userID) {
        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                historyList.clear(); // Clear the list before adding new data
                if (snapshot.exists()) {
                    for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                        // Check if the order belongs to the current user by matching the userID
                        String orderID = orderSnapshot.getKey(); // Get the orderID
                        String orderUserID = orderSnapshot.child("userID").getValue(String.class); // Get the userID for the order

                        // Only display orders where the userID matches the current user
                        if (userID.equals(orderUserID)) {
                            String orderLaundryService = orderSnapshot.child("laundryService").getValue(String.class);
                            Long totalCostValue = orderSnapshot.child("totalCost").getValue(Long.class);  // Retrieve as Long (or Double if it's a decimal value)
                            String orderTotalCost = totalCostValue != null ? String.valueOf(totalCostValue) : "0";  // Convert to String
                            String orderStatus = orderSnapshot.child("orderStatus").getValue(String.class);

                            // Create History object with the required fields
                            History history = new History(orderID, orderLaundryService, orderTotalCost, orderStatus);
                            historyList.add(history); // Add to historyList
                        }
                    }
                    historyAdapter.notifyDataSetChanged(); // Notify adapter of data changes
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Failed to retrieve orders: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

//    private void loadOrders(DatabaseReference ordersRef) {
//        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @SuppressLint("NotifyDataSetChanged")
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                historyList.clear(); // Clear the list before adding new data
//                if (snapshot.exists()) {
//                    for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
//                        if (!Objects.equals(orderSnapshot.getKey(), "counter")) {
//                            String orderID = orderSnapshot.getKey();
//                            String orderLaundryService = orderSnapshot.child("laundryService").getValue(String.class);
//                            Long totalCostValue = orderSnapshot.child("totalCost").getValue(Long.class);  // Retrieve as Long (or Double if it's a decimal value)
//                            String orderTotalCost = totalCostValue != null ? String.valueOf(totalCostValue) : "0";  // Convert to String
//                            String orderStatus = orderSnapshot.child("orderStatus").getValue(String.class);
//
//                            History history = new History(orderID, orderLaundryService, orderTotalCost, orderStatus);
//                            historyList.add(history);
//
//                        }
//                    }
//                    historyAdapter.notifyDataSetChanged(); // Notify adapter of data changes
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(getApplicationContext(), "Failed to retrieve orders: " + error.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

    private void calculateRewardsPoints(DatabaseReference ordersRef, String userID) {
        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int completedOrders = 0;

                // Count non-canceled orders for the current user
                for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                    String orderUserID = orderSnapshot.child("userID").getValue(String.class); // Retrieve the userID
                    if (userID.equals(orderUserID)) { // Check if the order belongs to the current user
                        String orderStatus = orderSnapshot.child("orderStatus").getValue(String.class);
                        if (!"Cancelled".equalsIgnoreCase(orderStatus)) {
                            completedOrders++;
                        }
                    }
                }

                // Calculate rewards points (1 point for every 5 completed orders)
                rewardsPoints = completedOrders / 5;
                rewardsPointsText.setText(String.format(Locale.getDefault(), "Rewards Points: %d", rewardsPoints));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Failed to retrieve rewards points: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}