package com.example.washwiz;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class HomeScreen extends AppCompatActivity {

    private static final int SLIDE_DELAY = 2000;
    private ViewPager slideViewPager;
    private ViewPagerAdapter adapter;
    private final Handler slideHandler = new Handler(Looper.getMainLooper());
    private int currentPage = 0;
    private FirebaseAnalytics mFirebaseAnalytics;
    private long sessionStartTime;
    private DatabaseReference reference;
    private String userID;
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1;
    private static final String TAG = "HomeScreenListener";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        // Check and request notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // TIRAMISU is API 33
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATION_PERMISSION);
            }
        }

        // Create the notification channel for Oreo and above
        createNotificationChannel();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        logAppOpenEvent();
        sessionStartTime = System.currentTimeMillis();

        logScreenView();

        Button schedBtn1 = findViewById(R.id.schedule_button);
        schedBtn1.setOnClickListener(view -> {
            Intent intent = new Intent(HomeScreen.this, SetDateScreen.class);
            startActivity(intent);
            logButtonClickEvent("schedule_button_1");
        });

        Button schedBtn2 = findViewById(R.id.sched);
        schedBtn2.setOnClickListener(view -> {
            Intent intent = new Intent(HomeScreen.this, SetDateScreen.class);
            startActivity(intent);
            logButtonClickEvent("schedule_button_2");
        });

        slideViewPager = findViewById(R.id.slideViewPager);
        adapter = new ViewPagerAdapter(this);
        slideViewPager.setAdapter(adapter);

        slideHandler.postDelayed(slideRunnable, SLIDE_DELAY);

        slideViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                currentPage = position;
                slideHandler.removeCallbacks(slideRunnable);
                slideHandler.postDelayed(slideRunnable, SLIDE_DELAY);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        ImageView profileIcon = findViewById(R.id.profile);
        profileIcon.setOnClickListener(view -> {
            Intent intent = new Intent(HomeScreen.this, AccountSettingScreen.class);
            startActivity(intent);
            logButtonClickEvent("profile_icon");
        });

        reference = FirebaseDatabase.getInstance().getReference();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        userID = Objects.requireNonNull(auth.getCurrentUser()).getUid();

        // Check for available slots and update the user
//        checkForAvailableSlots();
//        monitorSlotAvailability();
    }

    @Override
    protected void onStart() {
        super.onStart();
        monitorSlotAvailability(); // This will check the waitlist every time the app comes to the foreground
    }

    private final Runnable slideRunnable = new Runnable() {
        @Override
        public void run() {
            if (currentPage == adapter.getCount()) {
                currentPage = 0;
            }
            slideViewPager.setCurrentItem(currentPage++, true);
            slideHandler.postDelayed(this, SLIDE_DELAY);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        slideHandler.removeCallbacks(slideRunnable);

        // Log app close event and end session
        logAppCloseEvent();
        long sessionDuration = System.currentTimeMillis() - sessionStartTime;
        logSessionDuration(sessionDuration);
    }

    private void monitorSlotAvailability() {
        DatabaseReference waitlistRef = reference.child("Waitlist").child(getCurrentDate());

        // Remove listener when user is not on waitlist
        ValueEventListener waitlistListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "Checking if user is on waitlist for date: " + getCurrentDate());
                if (dataSnapshot.exists() && dataSnapshot.hasChild(userID)) {
                    Log.d(TAG, "User is on the waitlist. Starting to monitor slot cancellations...");
                    watchForSlotCancellations();
                } else {
                    Log.d(TAG, "User is not on the waitlist for the specified date.");
                    waitlistRef.removeEventListener(this); // Remove listener when user is not on waitlist
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error checking waitlist: " + databaseError.getMessage());
                Toast.makeText(HomeScreen.this, "Error checking waitlist: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        waitlistRef.addValueEventListener(waitlistListener);
    }

    private void watchForSlotCancellations() {
        DatabaseReference reservedSlotsRef = reference.child("ReservedTimeSlots").child(getCurrentDate());

        reservedSlotsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String canceledTimeSlot = snapshot.getKey();
                Log.d(TAG, "Detected cancelled slot at: " + canceledTimeSlot);
                notifyUserOfAvailableSlot(canceledTimeSlot);
            }

            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {}
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {}
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error monitoring slot cancellations: " + error.getMessage());
            }
        });
    }

    private void notifyUserOfAvailableSlot(String timeSlot) {
        Log.d(TAG, "Notifying user of available slot at: " + timeSlot);
        String message = "A slot has opened up for your waitlist on " + getCurrentDate() + " at " + timeSlot + ". Click to book!";
        sendNotification(message, timeSlot);
    }

    private void sendNotification(String message, String timeSlot) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATION_PERMISSION);
            return;
        }

        Intent intent = new Intent(this, SetDateScreen.class);
        intent.putExtra("freedUpDate", getCurrentDate());
        intent.putExtra("freedUpTime", timeSlot);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        intent.setAction("REMOVE_FROM_WAITLIST");

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "washwiz_schedule_updates")
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("Slot Available!")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1, builder.build());

        Log.d(TAG, "Notification sent for available slot at " + timeSlot);
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.set(2025, Calendar.JANUARY, 4);  // Setting the date to January 4, 2025
        return sdf.format(calendar.getTime());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "washwiz_schedule_updates";
            CharSequence channelName = "Schedule Updates";
            String channelDescription = "Notifications for available slot updates";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            channel.setDescription(channelDescription);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    // Logs a screen view event for HomeScreen
    private void logScreenView() {
        Bundle bundle = new Bundle();
        String screenName = "HomeScreen";
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName);
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenName);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
    }

    // Logs button click events
    private void logButtonClickEvent(String buttonName) {
        Bundle bundle = new Bundle();
        bundle.putString("button_name", buttonName);
        mFirebaseAnalytics.logEvent("button_click", bundle);
    }

    // Logs an app open event
    private void logAppOpenEvent() {
        Bundle bundle = new Bundle();
        mFirebaseAnalytics.logEvent("app_open", bundle);
    }

    // Logs an app close event
    private void logAppCloseEvent() {
        Bundle bundle = new Bundle();
        mFirebaseAnalytics.logEvent("app_close", bundle);
    }

    // Logs session duration event
    private void logSessionDuration(long duration) {
        Bundle bundle = new Bundle();
        bundle.putLong("session_duration", duration);
        mFirebaseAnalytics.logEvent("session_duration", bundle);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            //noinspection StatementWithEmptyBody
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can now proceed with notifications
            } else {
                // Permission denied, you can show a message to the user explaining why the permission is needed
                Toast.makeText(this, "Notification permission is required to receive alerts.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
