package com.example.washwiz;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class ReceiptScreen extends AppCompatActivity {

    private DatabaseReference reference;
    private FirebaseAuth auth;

    private TextView itemTextView;
    private TextView totalCostTextView;
    private TextView paymentModeTextView, discountAmountTextView;
    Button trackButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_receipt_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();

        // TextView to display the item information
        TextView orderDateTextView = findViewById(R.id.order_date);

        // Get the current date
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Set the current date to the TextView
        orderDateTextView.setText(currentDate);

        // Initialize UI elements
        itemTextView = findViewById(R.id.item);
        totalCostTextView = findViewById(R.id.total_amount);
        paymentModeTextView = findViewById(R.id.paymentMode);
        discountAmountTextView = findViewById(R.id.discount_amount);
        trackButton = findViewById(R.id.track_button);

        // Retrieve the current user's order
        displayCurrentOrder();

        trackButton.setOnClickListener(view -> {
            Intent intent = new Intent(ReceiptScreen.this, OrderStatusScreen.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void displayCurrentOrder() {
        if (auth.getCurrentUser() != null) {
            String userID = auth.getCurrentUser().getUid();
            DatabaseReference ordersRef = reference.child("Orders");

            // Fetch the orders for the current user
            ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        DataSnapshot mostRecentOrderSnapshot = null;
                        long mostRecentOrderID = -1;

                        // Iterate through the orders to find the most recent one
                        for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                            // Get the orderID from the snapshot key
                            String orderIDString = orderSnapshot.getKey();

                            // Skip if the orderID is null or not a valid number
                            if (orderIDString == null) continue;

                            long currentOrderID = -1;
                            try {
                                // Parse the order ID as a long, assuming it's numeric
                                currentOrderID = Long.parseLong(orderIDString);
                            } catch (NumberFormatException e) {
                                continue; // Skip if the orderID is not a valid number
                            }

                            // Update the most recent order
                            if (currentOrderID > mostRecentOrderID) {
                                mostRecentOrderSnapshot = orderSnapshot;
                                mostRecentOrderID = currentOrderID;
                            }
                        }

                        if (mostRecentOrderSnapshot != null) {
                            // Fetch order details
                            String addOn = mostRecentOrderSnapshot.child("addOn").getValue(String.class);
                            String laundryService = mostRecentOrderSnapshot.child("laundryService").getValue(String.class);
                            String laundryServiceOption = mostRecentOrderSnapshot.child("laundryServiceOption").getValue(String.class);
                            Long noOfPcs = mostRecentOrderSnapshot.child("executiveWashNoOfPcs").getValue(Long.class);
                            Long noOfLoads = mostRecentOrderSnapshot.child("noOfLoads").getValue(Long.class);
                            String typeOfClothes = mostRecentOrderSnapshot.child("typeOfClothes").getValue(String.class);
                            String paymentMode = mostRecentOrderSnapshot.child("paymentMode").getValue(String.class);
                            String noteToStaff = mostRecentOrderSnapshot.child("noteToStaff").getValue(String.class);
                            String date = mostRecentOrderSnapshot.child("date").getValue(String.class);
                            String time = mostRecentOrderSnapshot.child("time").getValue(String.class);
                            String address = mostRecentOrderSnapshot.child("orderAddress").getValue(String.class);
                            Double totalCost = mostRecentOrderSnapshot.child("totalCost").getValue(Double.class);

                            // Build the order details string
                            StringBuilder orderDetails = new StringBuilder();
                            orderDetails.append("Number of Loads: ").append(noOfLoads != null ? noOfLoads.toString() : "N/A").append("\n")
                                    .append("Type of Clothes: ").append(typeOfClothes != null ? typeOfClothes : "N/A").append("\n")
                                    .append("Laundry Service: ").append(laundryService != null ? laundryService : "N/A").append("\n")
                                    .append("Laundry Service Option: ").append(laundryServiceOption != null ? laundryServiceOption : "N/A").append("\n");

                            // Only add the number of pieces if the laundry service option is "Executive Wash"
                            if ("Executive Wash".equals(laundryServiceOption) && noOfPcs != null) {
                                orderDetails.append("Number of Pieces: ").append(noOfPcs.toString()).append("\n");
                            }

                            orderDetails.append("Add-ons: ").append(addOn != null ? addOn : "N/A").append("\n")
                                    .append("Notes to Staff: ").append(noteToStaff != null ? noteToStaff : "N/A").append("\n")
                                    .append("Order Date: ").append(date != null ? date : "N/A").append("\n")
                                    .append("Pickup Time: ").append(time != null ? time : "N/A").append("\n")
                                    .append("Address: ").append(address != null ? address : "N/A").append("\n\n");

                            // Set the most recent order details to the TextView
                            itemTextView.setText(orderDetails.toString());

                            StringBuilder totalAmountText = new StringBuilder();
                            totalAmountText.append("Total Amount Due: Php ").append(totalCost != null ? String.format(Locale.getDefault(), "%.2f", totalCost) : "N/A");
                            totalCostTextView.setText(totalAmountText.toString());

                            StringBuilder paymentModeText = new StringBuilder();
                            paymentModeText.append("Payment Mode: ").append(paymentMode != null ? paymentMode : "N/A").append("\n");
                            paymentModeTextView.setText(paymentModeText.toString());

                            checkForDiscount(userID, totalCost);
                        } else {
                            itemTextView.setText(R.string.no_current_orders_found);
                            totalCostTextView.setText(R.string.no_current_orders_found);
                        }
                    } else {
                        itemTextView.setText(R.string.no_current_orders_found);
                        totalCostTextView.setText(R.string.no_current_orders_found);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getApplicationContext(), "Error fetching orders: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getApplicationContext(), "User not authenticated. Please log in.", Toast.LENGTH_SHORT).show();
        }
    }

//    private void displayCurrentOrder() {
//        if (auth.getCurrentUser() != null) {
//            String userID = auth.getCurrentUser().getUid();
//            DatabaseReference ordersRef = reference.child("Orders").child(userID);
//
//            // Fetch the user's current orders
//            ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    if (snapshot.exists()) {
//                        DataSnapshot mostRecentOrderSnapshot = null;
//                        long mostRecentOrderID = -1;
//
//                        // Iterate through the orders to find the most recent one
//                        for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
//                            if (Objects.equals(orderSnapshot.getKey(), "counter")) {
//                                continue; // Skip the counter node, not an order
//                            }
//
//                            // Parse the order ID (assuming order IDs are in the format "WO<number>")
//                            String orderIDString = Objects.requireNonNull(orderSnapshot.getKey()).replace("WO", "");
//                            long currentOrderID = Long.parseLong(orderIDString);
//
//                            if (currentOrderID > mostRecentOrderID) {
//                                mostRecentOrderSnapshot = orderSnapshot;
//                                mostRecentOrderID = currentOrderID;
//                            }
//                        }
//
//                        if (mostRecentOrderSnapshot != null) {
//                            // Fetch and display the details of the most recent order
//                            String addOn = mostRecentOrderSnapshot.child("addOn").getValue(String.class);
//                            String laundryService = mostRecentOrderSnapshot.child("laundryService").getValue(String.class);
//                            String laundryServiceOption = mostRecentOrderSnapshot.child("laundryServiceOption").getValue(String.class);
//                            Long noOfPcs = mostRecentOrderSnapshot.child("executiveWashNoOfPcs").getValue(Long.class);
//                            Long noOfLoads = mostRecentOrderSnapshot.child("noOfLoads").getValue(Long.class);
//                            String typeOfClothes = mostRecentOrderSnapshot.child("typeOfClothes").getValue(String.class);
//                            String paymentMode = mostRecentOrderSnapshot.child("paymentMode").getValue(String.class);
//                            String noteToStaff = mostRecentOrderSnapshot.child("noteToStaff").getValue(String.class);
//                            String date = mostRecentOrderSnapshot.child("date").getValue(String.class);
//                            String time = mostRecentOrderSnapshot.child("time").getValue(String.class);
//                            String address = mostRecentOrderSnapshot.child("orderAddress").getValue(String.class);
//                            Double totalCost = mostRecentOrderSnapshot.child("totalCost").getValue(Double.class);
//
//                            // Build the order details string
//                            StringBuilder orderDetails = new StringBuilder();
//                            orderDetails.append("Number of Loads: ").append(noOfLoads != null ? noOfLoads.toString() : "N/A").append("\n")
//                                    .append("Type of Clothes: ").append(typeOfClothes != null ? typeOfClothes : "N/A").append("\n")
//                                    .append("Laundry Service: ").append(laundryService != null ? laundryService : "N/A").append("\n")
//                                    .append("Laundry Service Option: ").append(laundryServiceOption != null ? laundryServiceOption : "N/A").append("\n")
//                                    .append("Number of Pieces: ").append(noOfPcs != null ? noOfPcs.toString() : "N/A").append("\n")
//                                    .append("Add-ons: ").append(addOn != null ? addOn : "N/A").append("\n")
//                                    .append("Notes to Staff: ").append(noteToStaff != null ? noteToStaff : "N/A").append("\n")
//                                    .append("Order Date: ").append(date != null ? date : "N/A").append("\n")
//                                    .append("Pickup Time: ").append(time != null ? time : "N/A").append("\n")
//                                    .append("Address: ").append(address != null ? address : "N/A").append("\n\n");
//
//                            // Set the most recent order details to the TextView
//                            itemTextView.setText(orderDetails.toString());
//
//                            StringBuilder totalAmountText = new StringBuilder();
//                            totalAmountText.append("Total Amount Due: Php ").append(totalCost != null ? String.format(Locale.getDefault(), "%.2f", totalCost) : "N/A");
//                            totalCostTextView.setText(totalAmountText.toString());
//
//                            StringBuilder paymentModeText = new StringBuilder();
//                            paymentModeText.append("Payment Mode: ").append(paymentMode != null ? paymentMode : "N/A").append("\n");
//                            paymentModeTextView.setText(paymentModeText.toString());
//
//                            checkForDiscount(userID, totalCost);
//                        } else {
//                            itemTextView.setText(R.string.no_current_orders_found);
//                            totalCostTextView.setText(R.string.no_current_orders_found);
//                        }
//                    } else {
//                        itemTextView.setText(R.string.no_current_orders_found);
//                        totalCostTextView.setText(R.string.no_current_orders_found);
//                    }
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//                    Toast.makeText(getApplicationContext(), "Error fetching orders: " + error.getMessage(), Toast.LENGTH_SHORT).show();
//                }
//            });
//        } else {
//            Toast.makeText(getApplicationContext(), "User not authenticated. Please log in.", Toast.LENGTH_SHORT).show();
//        }
//    }

    private void checkForDiscount(String userID, Double totalCost) {
        DatabaseReference discountRef = reference.child("UserDiscounts").child(userID);

        discountRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && Boolean.TRUE.equals(snapshot.child("isActive").getValue(Boolean.class))) {
                    Integer discountPercent = snapshot.child("discountPercent").getValue(Integer.class);
                    if (discountPercent != null && totalCost != null) {
                        double discountAmount = totalCost * discountPercent / 100;
                        discountAmountTextView.setText(String.format(Locale.getDefault(), "Discount: Php %.2f", discountAmount));
                        discountAmountTextView.setVisibility(View.VISIBLE);
                    }
                } else {
                    discountAmountTextView.setVisibility(View.GONE); // Hide if no active discount
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ReceiptScreen.this, "Error fetching discount: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}