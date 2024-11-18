package com.example.washwiz;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class SetDateScreen extends AppCompatActivity {

    private DatabaseReference reference;
    private FirebaseAuth auth;
    private boolean isTimeSlotTaken = false;
    private boolean isOrderBeingProcessed = false;
    private static final String TAG = "SetDateScreenListener";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_set_date_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Retrieve data passed from the notification
        Intent intent = getIntent();
        String waitlistChosenDate = intent.getStringExtra("freedUpDate");
        String waitlistChosenTime = intent.getStringExtra("freedUpTime");

        // Check if the activity was launched with the remove action
        if (getIntent().getAction() != null && getIntent().getAction().equals("REMOVE_FROM_WAITLIST")) {
            // Remove user from waitlist when navigating from notification
            removeUserFromWaitlist(waitlistChosenDate);
        }

        reference = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();

        TextView setDateTextView = findViewById(R.id.set_date);
        TextView setTimeTextView = findViewById(R.id.set_time);

        if (waitlistChosenDate != null) {
            setDateTextView.setText(waitlistChosenDate);
        }
        if (waitlistChosenTime != null) {
            setTimeTextView.setText(waitlistChosenTime);
        }

        setDateTextView.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(SetDateScreen.this, (view, year1, monthOfYear, dayOfMonth) -> {
                String formattedDate = getString(R.string.chosen_date_format, year1, monthOfYear + 1, dayOfMonth);
                setDateTextView.setText(formattedDate);

                // Check if the selected date is fully booked
                checkDateAvailability(formattedDate, isBooked -> {
                    if (isBooked) {
                        // Show a warning message
                        Toast.makeText(SetDateScreen.this, "Selected date is fully booked. Would you like to join the waitlist?.", Toast.LENGTH_SHORT).show();

                        // Reset the date selection
//                        setDateTextView.setText("");
                        // Prompt user to join the waitlist
                        showWaitlistDialog(formattedDate);
                    }
                });
            }, year, month, day);

            // Disable fully booked dates
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis()); // Ensure past dates are not selectable
            datePickerDialog.show();
        });

        setTimeTextView.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(SetDateScreen.this, (view, hourOfDay, minuteOfHour) -> {
                String formattedTime = getString(R.string.chosen_time_format, hourOfDay, minuteOfHour);
                String chosenDate = setDateTextView.getText().toString();

                if (hourOfDay < 8 || hourOfDay > 17) {
                    Toast.makeText(SetDateScreen.this, "Please select a time between 8:00 AM and 5:00 PM", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if the time slot is available
                checkTimeSlotAvailability(chosenDate, formattedTime, isAvailable -> {
                    if (isAvailable) {
                        setTimeTextView.setText(formattedTime);
                        isTimeSlotTaken = true;
                    } else {
                        Toast.makeText(SetDateScreen.this, "Time slot is already taken. Please choose another time.", Toast.LENGTH_SHORT).show();
                    }
                });
            }, hour, minute, false);
            timePickerDialog.show();
        });

        Button continueBtn = findViewById(R.id.proceed_btn);
        continueBtn.setOnClickListener(view -> {
            if (isTimeSlotTaken && !isOrderBeingProcessed) {
                isOrderBeingProcessed = true;

                // Proceed with the order creation logic
                String chosenDate = setDateTextView.getText().toString();
                String chosenTime = setTimeTextView.getText().toString();
                String userID = Objects.requireNonNull(auth.getCurrentUser()).getUid();

                insertOrder(reference.child("Orders").child(userID), chosenDate, chosenTime);
            } else if (!isTimeSlotTaken) {
                Toast.makeText(SetDateScreen.this, "Please select an available time slot.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to show a dialog for the waitlist
    private void showWaitlistDialog(String chosenDate) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Join Waitlist")
                .setMessage("Would you like to be added to the waitlist for " + chosenDate + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Call method to add user to waitlist
                    addToWaitlist(chosenDate);
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    // Method to add user to waitlist
    private void addToWaitlist(String chosenDate) {
        String userID = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        DatabaseReference waitlistRef = reference.child("Waitlist").child(chosenDate).child(userID);

        waitlistRef.setValue(true).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "You've been added to the waitlist for " + chosenDate, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to add you to the waitlist.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to remove user from the waitlist
    private void removeUserFromWaitlist(String waitlistChosenDate) {
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(); // Get the current user ID
        DatabaseReference waitlistRef = FirebaseDatabase.getInstance().getReference("Waitlist").child(waitlistChosenDate).child(userId);

        waitlistRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "User successfully removed from the waitlist.");
                Toast.makeText(this, "You've been removed from the waitlist for " + waitlistChosenDate, Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, "Failed to remove user from the waitlist: " + task.getException());
                Toast.makeText(this, "Failed to remove you from the waitlist.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // New method to check if a date is fully booked
    private void checkDateAvailability(String date, OnDateChecked callback) {
        DatabaseReference reservedSlotsRef = reference.child("ReservedTimeSlots").child(date);

        reservedSlotsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean isBooked = false;

                if (dataSnapshot.exists()) {
                    int bookedSlots = 0;
                    int totalSlots = 20; // Total of 20 time slots (08:00 to 17:30)

                    for (int hour = 8; hour <= 17; hour++) {
                        for (int minute = 0; minute < 60; minute += 30) {
                            String timeSlot = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
                            if (dataSnapshot.hasChild(timeSlot)) {
                                bookedSlots++;
                            }
                        }
                    }

                    isBooked = bookedSlots >= totalSlots; // Check if the date is fully booked
                }

                callback.onChecked(isBooked);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SetDateScreen.this, "Error checking date availability: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkTimeSlotAvailability(String chosenDate, String chosenTime, final OnTimeSlotChecked callback) {
        DatabaseReference reservedSlotsRef = reference.child("ReservedTimeSlots").child(chosenDate).child(chosenTime);

        reservedSlotsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Check if the chosen time slot already has any reservations
                boolean isAvailable = !dataSnapshot.exists();
                callback.onChecked(isAvailable);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SetDateScreen.this, "Error checking time slot: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String markTimeSlotAsTaken(String chosenDate, String chosenTime, String orderID) {
        DatabaseReference reservedSlotsRef = FirebaseDatabase.getInstance().getReference().child("ReservedTimeSlots").child(chosenDate).child(chosenTime);

        String userID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        String uniqueKey = reservedSlotsRef.push().getKey();

        // Save the reservation details under the unique key
        assert uniqueKey != null;
        Map<String, Object> reservationData = new HashMap<>();
        reservationData.put("userID", userID);
        reservationData.put("orderID", orderID);

        reservedSlotsRef.child(uniqueKey).setValue(reservationData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("Reservation", "Time slot reserved successfully for " + chosenDate + " " + chosenTime);
            } else {
                Log.e("Reservation", "Failed to reserve time slot for " + chosenDate + " " + chosenTime);
            }
        });

        return uniqueKey; // Return the unique key for use in the order details
    }

    private void insertOrder(DatabaseReference ordersRef, String chosenDate, String chosenTime) {
        DatabaseReference counterRef = ordersRef.child("counter");
        counterRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Integer currentValue = currentData.getValue(Integer.class);
                if (currentValue == null) {
                    currentValue = 0;
                }
                int nextOrderId = currentValue + 1;
                currentData.setValue(nextOrderId);

                String orderID = "WO" + nextOrderId;

                DatabaseReference orderRef = ordersRef.child(orderID);
                orderRef.child("date").setValue(chosenDate);
                orderRef.child("time").setValue(chosenTime);

                String uniqueKey = markTimeSlotAsTaken(chosenDate, chosenTime, orderID);

                // Save the unique key in the order details
                orderRef.child("reservedTimeSlotID").setValue(uniqueKey);

                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (committed) {
                    Toast.makeText(getApplicationContext(), "Order inserted successfully", Toast.LENGTH_SHORT).show();
                } else {
                    assert error != null;
                    Toast.makeText(getApplicationContext(), "Failed to insert order: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        Intent intent = new Intent(SetDateScreen.this, SetLocationScreen.class);
        startActivity(intent);
    }

    private interface OnTimeSlotChecked {
        void onChecked(boolean isAvailable);
    }

    private interface OnDateChecked {
        void onChecked(boolean isBooked);
    }
}