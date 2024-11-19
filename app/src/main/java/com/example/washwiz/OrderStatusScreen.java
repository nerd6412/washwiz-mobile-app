package com.example.washwiz;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OrderStatusScreen extends AppCompatActivity {

    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1;
    private OrderAdapter orderAdapter;
    private List<Orders> orderList;
    private String pendingOrderID;
    private String pendingMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_status_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Request notification permission if needed (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userID = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference().child("Orders");

        RecyclerView recyclerView = findViewById(R.id.recycler_view_orders);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(orderList, ordersRef, userID);
        recyclerView.setAdapter(orderAdapter);

        loadOrders(ordersRef, userID);
        monitorOrderStatus(ordersRef, userID);

        TextView homeLinkBtn = findViewById(R.id.home_link);

        homeLinkBtn.setOnClickListener(view -> {
            Intent intent = new Intent(OrderStatusScreen.this, HomeScreen.class);
            startActivity(intent);
            finish();
        });

        // Create notification channel
        createNotificationChannel();
    }

    private void loadOrders(DatabaseReference ordersRef, String userID) {
        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderList.clear(); // Clear the list before adding new data
                if (snapshot.exists()) {
                    for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                        // Retrieve the userID associated with the order
                        String orderUserID = orderSnapshot.child("userID").getValue(String.class);
                        String orderID = orderSnapshot.getKey(); // Use the order ID directly as the key

                        // Check if the order's userID matches the current user's ID
                        if (userID.equals(orderUserID)) {
                            String orderStatus = orderSnapshot.child("orderStatus").getValue(String.class);

                            // Only add orders that are not "Completed" or "Cancelled"
                            if (!"Completed".equals(orderStatus) && !"Cancelled".equals(orderStatus)) {
                                String orderDate = orderSnapshot.child("date").getValue(String.class);
                                String time = orderSnapshot.child("time").getValue(String.class);
                                String reservedTimeSlotID = orderSnapshot.child("reservedTimeSlotID").getValue(String.class);
                                String orderETA = orderSnapshot.child("eta").getValue(String.class);
                                String pickupRider = orderSnapshot.child("pickupName").getValue(String.class);
                                String deliveryRider = orderSnapshot.child("deliveryName").getValue(String.class);

                                // Create Order object with the required fields
                                Orders order = new Orders(orderID, orderDate, orderStatus, time, reservedTimeSlotID, orderETA, pickupRider, deliveryRider);
                                orderList.add(order); // Add order to the list
                            }
                        }
                    }
                    orderAdapter.notifyDataSetChanged(); // Notify adapter of data changes
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Failed to retrieve orders: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Monitor changes in the order status and send a notification when it changes to "Out for Delivery"
    private void monitorOrderStatus(DatabaseReference ordersRef, String userID) {
        ordersRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String orderID = snapshot.getKey(); // Get the orderID directly from the snapshot key
                String orderUserID = snapshot.child("userID").getValue(String.class); // Retrieve the userID for this order

                // Check if the order's userID matches the current user's userID
                if (userID.equals(orderUserID)) {
                    String orderStatus = snapshot.child("orderStatus").getValue(String.class);

                    // Send a notification based on the order status
                    if ("Out for Delivery".equals(orderStatus)) {
                        sendOrderNotification(orderID, "Your order " + orderID + " is now out for delivery!");
                    } else if ("Picked Up".equals(orderStatus)) {
                        sendOrderNotification(orderID, "Your order " + orderID + " has arrived at the laundry shop!");
                    } else if ("Ongoing".equals(orderStatus)) {
                        sendOrderNotification(orderID, "Your order " + orderID + " is now being processed!");
                    }
                }
            }

            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // Handle child added if needed, typically not needed for status changes.
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                // Handle child removed if needed, typically not needed for status changes.
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // Handle child moved if needed.
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("OrderStatusScreen", "Failed to monitor order status: " + error.getMessage());
            }
        });
    }


//    private void loadOrders(DatabaseReference ordersRef) {
//        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @SuppressLint("NotifyDataSetChanged")
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                orderList.clear(); // Clear the list before adding new data
//                if (snapshot.exists()) {
//                    for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
//                        if (!Objects.equals(orderSnapshot.getKey(), "counter")) {
//                            String orderID = orderSnapshot.getKey();
//                            String orderStatus = orderSnapshot.child("orderStatus").getValue(String.class);
//
//                            // Only add orders that are not "Completed" or "Cancelled"
//                            if (!"Completed".equals(orderStatus) && !"Cancelled".equals(orderStatus)) {
//                                String orderDate = orderSnapshot.child("date").getValue(String.class);
//                                String time = orderSnapshot.child("time").getValue(String.class);
//                                String reservedTimeSlotID = orderSnapshot.child("reservedTimeSlotID").getValue(String.class);
//                                String orderETA = orderSnapshot.child("eta").getValue(String.class);
//                                String pickupRider = orderSnapshot.child("pickupName").getValue(String.class);
//                                String deliveryRider = orderSnapshot.child("deliveryName").getValue(String.class);
//
//                                Orders order = new Orders(orderID, orderDate, orderStatus, time, reservedTimeSlotID, orderETA, pickupRider, deliveryRider); // Create Order object
//                                orderList.add(order); // Add order to the list
//                            }
//                        }
//                    }
//                    orderAdapter.notifyDataSetChanged(); // Notify adapter of data changes
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(getApplicationContext(), "Failed to retrieve orders: " + error.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    // Monitor changes in the order status and send a notification when it changes to "Out for Delivery"
//    private void monitorOrderStatus() {
//        ordersRef.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                if (!Objects.equals(snapshot.getKey(), "counter")) {
//                    String orderID = snapshot.getKey();
//                    String orderStatus = snapshot.child("orderStatus").getValue(String.class);
//                    if ("Out for Delivery".equals(orderStatus)) {
//                        sendOrderNotification(orderID, "Your order " + orderID + " is now out for delivery!");
//                    } else if ("Picked Up".equals(orderStatus)) {
//                        sendOrderNotification(orderID, "Your order " + orderID + " has arrived at the laundry shop!");
//                    } else if("Ongoing".equals(orderStatus)) {
//                        sendOrderNotification(orderID, "Your order " + orderID + " is now being processed!");
//                    }
//                }
//            }
//
//            @Override
//            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                // Handle child added if needed
//            }
//
//            @Override
//            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
//                // Handle child removed if needed
//            }
//
//            @Override
//            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                // Handle child moved if needed
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.e("OrderStatusScreen", "Failed to monitor order status: " + error.getMessage());
//            }
//        });
//    }

    // Function to send a notification
    private void sendOrderNotification(String orderID, String message) {
        pendingOrderID = orderID;
        pendingMessage = message;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST_CODE);
        } else {
            showNotification(orderID, message);
        }
    }

    private void showNotification(String orderID, String message) {
        String notificationTitle = "Order " + orderID + " Update";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "orderStatusChannel")
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(notificationTitle)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        Intent intent = new Intent(this, OrderStatusScreen.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "orderStatusChannel",
                    "Order Status Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(1, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "orderStatusChannel",
                    "Order Status Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Channel for order status notifications");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (pendingOrderID != null && pendingMessage != null) {
                    showNotification(pendingOrderID, pendingMessage);
                    pendingOrderID = null;
                    pendingMessage = null;
                }
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}