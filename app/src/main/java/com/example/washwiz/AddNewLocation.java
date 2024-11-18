package com.example.washwiz;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AddNewLocation extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_new_location);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        EditText newLocation = findViewById(R.id.new_location);

        Button confirmBtn = findViewById(R.id.confirm_button);
        confirmBtn.setOnClickListener(view -> {
            String newLocationText = newLocation.getText().toString();

            Intent intent = new Intent(AddNewLocation.this, OrderDetailScreen.class);
            intent.putExtra("location", newLocationText);
            startActivity(intent);
        });
    }
}