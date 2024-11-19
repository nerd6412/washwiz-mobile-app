package com.example.washwiz;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
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

public class SetLocationScreen extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    final String[] choices = {"Choose pickup location", "Current Address"};
    private DatabaseReference reference;
    private FirebaseAuth auth;
    private String currentUserAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_set_location_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();

        Spinner locationSpinner = findViewById(R.id.location);
        locationSpinner.setOnItemSelectedListener(this);
        ArrayAdapter<String> adapter = getAdapter();
        locationSpinner.setAdapter(adapter);

        TextView newLocationLink = findViewById(R.id.add_newAddress);
        newLocationLink.setOnClickListener(view -> {
            Intent intent = new Intent(SetLocationScreen.this, AddNewLocation.class);
            startActivity(intent);
        });

        Button proceedBtn = findViewById(R.id.proceed_button);
        proceedBtn.setOnClickListener(view -> {
            if (currentUserAddress != null) {
                Intent intent = new Intent(SetLocationScreen.this, OrderDetailScreen.class);
                intent.putExtra("location", currentUserAddress); // Pass the address
                startActivity(intent);
            } else {
                Toast.makeText(this, "Please select a valid pickup location.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private @NonNull ArrayAdapter<String> getAdapter() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, choices) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = view.findViewById(android.R.id.text1);
                text.setTextColor(Color.BLACK);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView text = view.findViewById(android.R.id.text1);
                text.setTextColor(Color.WHITE);
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        if (position == 1) { // Current Address selected
            fetchCurrentUserAddress();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private void fetchCurrentUserAddress() {
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            DatabaseReference userAddressRef = reference.child("Users").child(userId).child("address"); // Adjust path as needed

            userAddressRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        currentUserAddress = snapshot.getValue(String.class);
                        Toast.makeText(SetLocationScreen.this, "Address fetched: " + currentUserAddress, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SetLocationScreen.this, "No address found for the current user.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(SetLocationScreen.this, "Failed to retrieve address: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "User not authenticated. Please log in.", Toast.LENGTH_SHORT).show();
        }
    }
}