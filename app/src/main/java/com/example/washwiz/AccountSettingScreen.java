package com.example.washwiz;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AccountSettingScreen extends AppCompatActivity {

    private TextView accountName, accountEmail, accountAddress, accountContact;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account_setting_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        accountName = findViewById(R.id.account_name);
        accountEmail = findViewById(R.id.account_email);
        accountAddress = findViewById(R.id.account_address);
        accountContact = findViewById(R.id.account_contact);

        TextView changeAddress = findViewById(R.id.change_address);
        TextView changeContact = findViewById(R.id.change_contact);
        Button submitBtn = findViewById(R.id.submitButton);

        if (currentUser != null) {
            // Get userID of the currently authenticated user
            String userID = currentUser.getUid();

            // Reference to "Users" table in Firebase Realtime Database
            userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);

            // Fetch the user's data
            fetchUserData();

            submitBtn.setOnClickListener(view -> {
                String newAddress = changeAddress.getText().toString().trim();
                String newContact = changeContact.getText().toString().trim();

                if (!newAddress.isEmpty()) {
                    userRef.child("address").setValue(newAddress);
                }
                if (!newContact.isEmpty()) {
                    userRef.child("contact").setValue(newContact);
                }

                // Hide fields after submission
                changeAddress.setVisibility(View.GONE);
                changeContact.setVisibility(View.GONE);
                submitBtn.setVisibility(View.GONE);
            });
        }

        Button logoutBtn = findViewById(R.id.logout_button);
        logoutBtn.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();  // Sign out the user
            Intent intent = new Intent(AccountSettingScreen.this, LoginScreen.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        TextView orderHistoryLink = findViewById(R.id.order_history_link);
        orderHistoryLink.setOnClickListener(view -> {
            Intent intent = new Intent(AccountSettingScreen.this, OrderHistoryScreen.class);
            startActivity(intent);
            finish();
        });
    }

    private void fetchUserData() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Get user details from the snapshot
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String email = dataSnapshot.child("email").getValue(String.class);
                    String address = dataSnapshot.child("address").getValue(String.class);
                    String contact = dataSnapshot.child("contact").getValue(String.class);

                    // Display user details in TextViews
                    accountName.setText(String.format("Name:   %s", name));
                    accountEmail.setText(String.format("Email:   %s", email));
                    accountAddress.setText(String.format("Address:   %s", address));
                    accountContact.setText(String.format("Contact:   %s", contact));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database errors
            }
        });
    }
}