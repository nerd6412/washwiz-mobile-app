package com.example.washwiz;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

import android.util.Log;

public class OrderDetailScreen extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    DatabaseReference databaseRef;

    private ClothingAdapter clothingAdapter;
    private List<ClothingItem> clothingItemList;
    private int noOfLoads;
    private Map<String, Integer> subcategoryTotals;
    private final Map<String, Double> estimatedWeights = new HashMap<>();

    final String[] addOns = { "Add On/s (optional)", "Add Wash",
            "Add Rinse", "Add Dry"};

    final String[] clothes = { "Type of Clothes", "Cotton/Silk",
            "Comforter/Bulky", "Accessories (Coat/Top, Dress/Terno, Shoes, Bags, Gown)"};

    final String[] paymentModes = { "Choose mode of payment", "Cash On Delivery",
            "GCash Payment"};

    final String[] laundryServices = { "Choose laundry service", "Regular Wash",
            "Executive Wash", "Dry Clean", "Special Wash", "Comforter/Bulky"};

    final String[] detergents = { "Type of Detergent", "Liquid Detergent", "Powder Detergent"};

    final String[] regularWashOptions = { "Regular Wash Service Options", "Wash - Php 65.00",
            "Dry - Php 70.00", "Full Service - Php 165.00" };

    final String[] executiveWashOptions = { "Executive Wash Service Options",
            "Wash/Dry + Steam - Php 30/pc.", "Wash/Dry + Treatment - Php 25/pc." };

    final String[] specialWashOptions = { "Special Wash Service Options", "Warm Wash - Php 80.00",
            "Hot Wash - Php 90.00", "Disinfect Wash - Php 120.00" };

    final String[] comforterWashOptions = { "Comforter/Bulky Wash Service Options",
            "Wash+Dry - Php 160.00", "Wash+Dry+Fold - Php 185.00" };

    final String[] dryCleanWashOptions = { "Dry Clean Service Options", "Barong/Tops/Coat - Php 250.00",
            "Dress/Terno - Php 450.00", "Shoes/Bag - Php 500.00" };

    private DatabaseReference reference;
    private FirebaseAuth auth;

    private Spinner typeOfClothes, laundryService, addOn, modeOfPayment, typeOfDetergent;
    private Spinner regularWashOptionsSpinner, executiveWashSpinner, specialWashSpinner, comforterWashSpinner, dryCleanWashSpinner;
    private EditText noOfPcs, noteToStaff;
    private TextView addressField;

    int selectedLaundryServicePosition = 0;
    int selectedTypeOfClothesPosition = 0;
    int selectedTypeOfDetergentPosition = 0;
    int selectedAddOnPosition = 0;
    int selectedPaymentModePosition = 0;

    private int totalShirts = 0;
    private int totalPants = 0;
    private int totalShorts = 0;
    private int totalUnderwear = 0;
    private int totalSkirts = 0;
    private int totalPajamas = 0;
    private int totalSocks = 0;
    private int totalComfortersPillows = 0;
    private int totalTowelsRugs = 0;
    private int totalBedSheets = 0;
    private int totalCurtains = 0;
    private int totalTops = 0;
    private int totalDresses = 0;
    private int totalSwimwear = 0;
    private int totalShoes = 0;
    private int totalBags = 0;
    private int totalHats = 0;
    private int totalScarves = 0;
    private int totalGloves = 0;

    private void initializeWeights() {
        estimatedWeights.put("Shirts", 0.2);  // Weight per shirt
        estimatedWeights.put("Pants", 0.5);  // Weight per pant
        estimatedWeights.put("Shorts", 0.3); // Weight per short
        estimatedWeights.put("Underwear", 0.05); // Weight per underwear
        estimatedWeights.put("Skirts", 0.3); // Weight per skirt
        estimatedWeights.put("Pajamas", 0.3); // Weight per pajama
        estimatedWeights.put("Socks", 0.05); // Weight per sock
        estimatedWeights.put("Comforters/Pillows", 0.3); // Weight per comforter/pillow
        estimatedWeights.put("Towels/Rugs", 0.6); // Weight per towel/rug
        estimatedWeights.put("Bed Sheets", 1.5); // Weight per bed sheet
        estimatedWeights.put("Curtains", 1.2); // Weight per curtain
        estimatedWeights.put("Tops", 0.2); // Weight per top
        estimatedWeights.put("Dresses", 0.4); // Weight per dress
        estimatedWeights.put("Swimwear", 0.2); // Weight per swimwear
        estimatedWeights.put("Shoes", 0.5); // Weight per shoe
        estimatedWeights.put("Bags", 0.3); // Weight per bag
        estimatedWeights.put("Hats", 0.2); // Weight per hat
        estimatedWeights.put("Scarves", 0.1); // Weight per scarf
        estimatedWeights.put("Gloves", 0.1); // Weight per glove
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_detail_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeWeights();

        databaseRef = FirebaseDatabase.getInstance().getReference();

        Log.d("OrderDetailScreen", "Spinners and EditTexts initialized");

        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();

//        noOfLoads = findViewById(R.id.no_of_loads);
        noteToStaff = findViewById(R.id.note_to_staff);
        noOfPcs = findViewById(R.id.no_of_pcs);

        modeOfPayment = findViewById(R.id.mode_of_payment);
        modeOfPayment.setOnItemSelectedListener(this);
        ArrayAdapter<String> ad = getArrayAdapter();
        modeOfPayment.setAdapter(ad);

        laundryService = findViewById(R.id.laundry_service);
        laundryService.setOnItemSelectedListener(this);
        ArrayAdapter<String> adapter = getAdapter();
        laundryService.setAdapter(adapter);

        regularWashOptionsSpinner = findViewById(R.id.regular_options);
        regularWashOptionsSpinner.setOnItemSelectedListener(this);
        ArrayAdapter<String> regularWashAdapter = getRegularWashAdapter();
        regularWashOptionsSpinner.setAdapter(regularWashAdapter);
        regularWashOptionsSpinner.setVisibility(View.GONE);

        executiveWashSpinner = findViewById(R.id.executive_options);
        executiveWashSpinner.setOnItemSelectedListener(this);
        ArrayAdapter<String> executiveWashAdapter = getExecutiveWashAdapter();
        executiveWashSpinner.setAdapter(executiveWashAdapter);
        executiveWashSpinner.setVisibility(View.GONE);

        specialWashSpinner = findViewById(R.id.special_options);
        specialWashSpinner.setOnItemSelectedListener(this);
        ArrayAdapter<String> specialWashAdapter = getSpecialWashAdapter();
        specialWashSpinner.setAdapter(specialWashAdapter);
        specialWashSpinner.setVisibility(View.GONE);

        comforterWashSpinner = findViewById(R.id.comforter_options);
        comforterWashSpinner.setOnItemSelectedListener(this);
        ArrayAdapter<String> comforterWashAdapter = getComforterWashAdapter();
        comforterWashSpinner.setAdapter(comforterWashAdapter);
        comforterWashSpinner.setVisibility(View.GONE);

        dryCleanWashSpinner = findViewById(R.id.dryclean_options);
        dryCleanWashSpinner.setOnItemSelectedListener(this);
        ArrayAdapter<String> dryCleanWashAdapter = getDryCleanWashAdapter();
        dryCleanWashSpinner.setAdapter(dryCleanWashAdapter);
        dryCleanWashSpinner.setVisibility(View.GONE);

        typeOfClothes = findViewById(R.id.type_of_clothes);
        typeOfClothes.setOnItemSelectedListener(this);
        ArrayAdapter<String> adapt = getStringArrayAdapter();
        typeOfClothes.setAdapter(adapt);

        typeOfDetergent = findViewById(R.id.type_of_detergent);
        typeOfDetergent.setOnItemSelectedListener(this);
        ArrayAdapter<String> detergentAdapter = getDetergentAdapter();
        typeOfDetergent.setAdapter(detergentAdapter);

        // RecyclerView setup
        RecyclerView clothingRecyclerView = findViewById(R.id.clothingRecyclerView);
        clothingItemList = new ArrayList<>();
        clothingAdapter = new ClothingAdapter(clothingItemList, this);
        clothingRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        clothingRecyclerView.setAdapter(clothingAdapter);

        // Button to add new clothing items
        Button addClothingButton = findViewById(R.id.addClothingButton);
        addClothingButton.setOnClickListener(view -> {
            // Add a default clothing item
            clothingItemList.add(new ClothingItem("Shirts", "T-Shirts", 1)); // Adjust default values as needed
            clothingAdapter.notifyItemInserted(clothingItemList.size() - 1);
        });

        addOn = findViewById(R.id.add_ons);
        addOn.setOnItemSelectedListener(this);

        ArrayAdapter<String> adapter1 = getAdapter1();
        addOn.setAdapter(adapter1);

        addressField = findViewById(R.id.user_address);
        Intent intent = getIntent();
        String userAddress = intent.getStringExtra("location");
        if (userAddress != null) {
            addressField.setText(userAddress);
        }
    }

    private @NonNull ArrayAdapter<String> getAdapter1() {
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, addOns) {
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
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter1;
    }

    private @NonNull ArrayAdapter<String> getDryCleanWashAdapter() {
        ArrayAdapter<String> dryCleanWashAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, dryCleanWashOptions) {
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
        dryCleanWashAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return dryCleanWashAdapter;
    }

    private @NonNull ArrayAdapter<String> getComforterWashAdapter() {
        ArrayAdapter<String> comforterWashAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, comforterWashOptions) {
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
        comforterWashAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return comforterWashAdapter;
    }

    private @NonNull ArrayAdapter<String> getSpecialWashAdapter() {
        ArrayAdapter<String> specialWashAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, specialWashOptions) {
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
        specialWashAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return specialWashAdapter;
    }

    private @NonNull ArrayAdapter<String> getExecutiveWashAdapter() {
        ArrayAdapter<String> executiveWashAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, executiveWashOptions) {
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
        executiveWashAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return executiveWashAdapter;
    }

    private @NonNull ArrayAdapter<String> getRegularWashAdapter() {
        ArrayAdapter<String> regularWashAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, regularWashOptions) {
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
        regularWashAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return regularWashAdapter;
    }

    private @NonNull ArrayAdapter<String> getAdapter() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, laundryServices) {
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

    private @NonNull ArrayAdapter<String> getArrayAdapter() {
        ArrayAdapter<String> ad = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, paymentModes) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = view.findViewById(android.R.id.text1);
                text.setTextColor(Color.BLACK); // Set the text color to black
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView text = view.findViewById(android.R.id.text1);
                text.setTextColor(Color.WHITE); // Set the dropdown text color to black
                return view;
            }
        };
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return ad;
    }

    private @NonNull ArrayAdapter<String> getStringArrayAdapter() {
        ArrayAdapter<String> adapt = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, clothes) {
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
        adapt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapt;
    }

    private @NonNull ArrayAdapter<String> getDetergentAdapter() {
        ArrayAdapter<String> adapt = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, detergents) {
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
        adapt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapt;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        String selectedItem = "";

        if (adapterView.getId() == R.id.mode_of_payment) {
            selectedItem = paymentModes[position];
        } else if (adapterView.getId() == R.id.laundry_service) {
            selectedItem = laundryServices[position];

            if("Regular Wash".equals(selectedItem)) {
                regularWashOptionsSpinner.setVisibility(View.VISIBLE);
            } else {
                regularWashOptionsSpinner.setVisibility(View.GONE);
            }

            if("Executive Wash".equals(selectedItem)) {
                executiveWashSpinner.setVisibility(View.VISIBLE);
                noOfPcs.setVisibility(View.VISIBLE);
            } else {
                executiveWashSpinner.setVisibility(View.GONE);
                noOfPcs.setVisibility(View.GONE);
            }

            if("Special Wash".equals(selectedItem)) {
                specialWashSpinner.setVisibility(View.VISIBLE);
            } else {
                specialWashSpinner.setVisibility(View.GONE);
            }

            if("Comforter/Bulky".equals(selectedItem)) {
                comforterWashSpinner.setVisibility(View.VISIBLE);
            } else {
                comforterWashSpinner.setVisibility(View.GONE);
            }

            if("Dry Clean".equals(selectedItem)) {
                dryCleanWashSpinner.setVisibility(View.VISIBLE);
            } else {
                dryCleanWashSpinner.setVisibility(View.GONE);
            }

        } else if (adapterView.getId() == R.id.type_of_clothes) {
            selectedItem = clothes[position];

            // Call updateClothingItems when a clothing type is selected
            updateClothingItems(selectedItem);

        } else if (adapterView.getId() == R.id.add_ons) {
            selectedItem = addOns[position];
        }

        if (!selectedItem.equals("Choose mode of payment") && !selectedItem.equals("Choose laundry service") && !selectedItem.equals("Type of Clothes") && !selectedItem.equals("Add On/s (optional)")) {
            Toast.makeText(getApplicationContext(), selectedItem, Toast.LENGTH_SHORT).show();
        }

        // Redirect only for payment options
        if (adapterView.getId() == R.id.mode_of_payment) {
            if (position == 1) { // Cash On Delivery
                insertOrderData();
            } else if (position == 2) { // GCash Payment
                insertOrderData();
            }
        }

        if (adapterView.getId() == R.id.laundry_service) {
            selectedLaundryServicePosition = position;
        } else if (adapterView.getId() == R.id.type_of_clothes) {
            selectedTypeOfClothesPosition = position;
        } else if (adapterView.getId() == R.id.add_ons) {
            selectedAddOnPosition = position;
        } else if (adapterView.getId() == R.id.mode_of_payment) {
            selectedPaymentModePosition = position;
        } else if (adapterView.getId() == R.id.type_of_detergent) {
            selectedTypeOfDetergentPosition = position;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        // Default behavior when nothing is selected
        updateClothingItems("Type of Clothes");
    }

    public void insertOrderData() {
        if (auth.getCurrentUser() != null) {
            // Get the authenticated user's UID
            String userID = auth.getCurrentUser().getUid();
            DatabaseReference ordersRef = reference.child("Orders");

            // Check if there are any existing orders (based on orderID)
            ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String orderID = null;

                        // Loop through the existing orders to find the latest order by orderID
                        for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                            // Set the most recent orderID
                            orderID = orderSnapshot.getKey();
                        }

                        // Ensure we found an orderID before proceeding
                        if (orderID != null) {
                            // Reference to the existing order using the orderID
                            DatabaseReference orderRef = ordersRef.child(orderID);

                            // Prepare the data to update the order
                            Map<String, Object> updatedData = getStringObjectMap();

                            // Update the order with new data
                            String finalOrderID = orderID;
                            orderRef.updateChildren(updatedData)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getApplicationContext(), "Order updated successfully", Toast.LENGTH_SHORT).show();

                                            // Redirect after successful update
                                            redirectAfterUpdate(userID, finalOrderID);
                                        } else {
                                            Toast.makeText(getApplicationContext(), "Failed to update order: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        // No orders found in the database
                        Toast.makeText(getApplicationContext(), "No active orders found. Please create a new order.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getApplicationContext(), "Failed to retrieve order: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Handle the case where the user is not authenticated
            Toast.makeText(getApplicationContext(), "Please sign in to insert an order.", Toast.LENGTH_SHORT).show();
        }
    }

    private void redirectAfterUpdate(String userID, String orderID) {
        if (selectedPaymentModePosition == 1) {
            DatabaseReference cashOnDeliveryPaymentsRef = reference.child("CashOnDeliveryPayments").child(userID).child(orderID);

            // Ensure unique paymentID for each entry under the correct orderID
            String paymentID = cashOnDeliveryPaymentsRef.push().getKey();

            // Create the payment status map
            Map<String, Object> paymentData = new HashMap<>();
            paymentData.put("paymentStatus", "Pending");

            // Insert the payment status under the correct orderID and paymentID
            assert paymentID != null;
            cashOnDeliveryPaymentsRef.child(paymentID).setValue(paymentData).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Payment status inserted successfully for " + orderID, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to insert payment status: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            Intent intent = new Intent(OrderDetailScreen.this, ReceiptScreen.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else if (selectedPaymentModePosition == 2) {
            Intent intent = new Intent(OrderDetailScreen.this, GCashScreen.class);
            intent.putExtra("orderID", orderID); // Pass the orderID
            startActivity(intent);
            finish();
        }
    }

    // Method to calculate totals and loads
    private void calculateTotalsAndLoads() {
        subcategoryTotals = new HashMap<>(); // Initialize or reset
        double totalWeight = 0.0;

        // Sum up quantities for each subcategory and store in class-level variables
        for (ClothingItem item : clothingItemList) {
            String subcategory = item.getSubCategory();
            int quantity = item.getQuantity();

            // Update the subcategoryTotals map with the total quantity for each subcategory
            if (subcategoryTotals.containsKey(subcategory)) {
                subcategoryTotals.put(subcategory, subcategoryTotals.get(subcategory) + quantity);
            } else {
                subcategoryTotals.put(subcategory, quantity);
            }

            // Store the total quantity for each specific subcategory variable (like totalShirts, totalPants)
            switch (subcategory) {
                case "Shirts":
                    totalShirts += quantity;
                    break;
                case "Pants":
                    totalPants += quantity;
                    break;
                case "Shorts":
                    totalShorts += quantity;
                    break;
                case "Underwear":
                    totalUnderwear += quantity;
                    break;
                case "Skirts":
                    totalSkirts += quantity;
                    break;
                case "Pajamas":
                    totalPajamas += quantity;
                    break;
                case "Socks":
                    totalSocks += quantity;
                    break;
                case "Comforters/Pillows":
                    totalComfortersPillows += quantity;
                    break;
                case "Towels/Rugs":
                    totalTowelsRugs += quantity;
                    break;
                case "Bed Sheets":
                    totalBedSheets += quantity;
                    break;
                case "Curtains":
                    totalCurtains += quantity;
                    break;
                case "Tops":
                    totalTops += quantity;
                    break;
                case "Dresses":
                    totalDresses += quantity;
                    break;
                case "Swimwear":
                    totalSwimwear += quantity;
                    break;
                case "Shoes":
                    totalShoes += quantity;
                    break;
                case "Bags":
                    totalBags += quantity;
                    break;
                case "Hats":
                    totalHats += quantity;
                    break;
                case "Scarves":
                    totalScarves += quantity;
                    break;
                case "Gloves":
                    totalGloves += quantity;
                    break;
                default:
                    break;
            }
        }

        // Calculate total weight and loads
        for (Map.Entry<String, Integer> entry : subcategoryTotals.entrySet()) {
            String subcategory = entry.getKey();
            int quantity = entry.getValue();

            // Safe retrieval of weight for subcategory
            double weightPerItem = (estimatedWeights.containsKey(subcategory))
                    ? estimatedWeights.get(subcategory)
                    : 0.0; // Default to 0.0 if not found

            // Calculate the weight for the current subcategory
            double subcategoryWeight = quantity * weightPerItem;
            totalWeight += subcategoryWeight;

            Log.d("InsertOrderData", "Subcategory: " + subcategory + ", Quantity: " + quantity + ", Weight: " + subcategoryWeight);
        }

        // Divide total weight by 8 to calculate the number of loads
        noOfLoads = (int) Math.ceil(totalWeight / 8.0); // Update class variable
        Log.d("TotalWeight", "Total Weight: " + totalWeight + " kg");
        Log.d("NoOfLoads", "Number of Loads: " + noOfLoads);
    }

    private @NonNull Map<String, Object> getStringObjectMap() {
        String selectedLaundryService = laundryServices[laundryService.getSelectedItemPosition()];
        String selectedTypeOfClothes = clothes[typeOfClothes.getSelectedItemPosition()];
        String selectedTypeOfDetergent = detergents[typeOfDetergent.getSelectedItemPosition()];
        String selectedAddOn = addOns[addOn.getSelectedItemPosition()];
        if (selectedAddOn.equals("Add On/s (optional)")) {
            selectedAddOn = "";
        }
        String selectedPaymentMode = paymentModes[modeOfPayment.getSelectedItemPosition()];
        String selectedServiceOption = "";
        double serviceCost = 0.0;

        // Ensure calculations are up-to-date
        calculateTotalsAndLoads();

        // Use `noOfLoads` in your order data
        Log.d("InsertOrderData", "Number of Loads: " + noOfLoads);

        // Use `subcategoryTotals` for additional logic
        for (Map.Entry<String, Integer> entry : subcategoryTotals.entrySet()) {
            String subcategory = entry.getKey();
            int quantity = entry.getValue();
            Log.d("InsertOrderData", "Subcategory: " + subcategory + ", Quantity: " + quantity);
        }

//        String noOfLoadsText = noOfLoads.getText().toString();
//        int noOfLoadsInt = 0;
//        if (!noOfLoadsText.isEmpty()) {
//            try {
//                noOfLoadsInt = Integer.parseInt(noOfLoadsText);
//                noOfLoadsInt = (int) Math.round(noOfLoadsInt / 8.0);
//
//            } catch (NumberFormatException e) {
//                Toast.makeText(this, "Please input digits only", Toast.LENGTH_SHORT).show();
//            }
//        }

        String noOfPcsText = noOfPcs.getText().toString();
        int noOfPcsInt = 0;
        if (!noOfPcsText.isEmpty()) {
            try {
                noOfPcsInt = Integer.parseInt(noOfPcsText);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please input digits only", Toast.LENGTH_SHORT).show();
            }
        }

        String machineNeeded = "";

        switch (selectedLaundryService) {
            case "Regular Wash":
                selectedServiceOption = regularWashOptionsSpinner.getSelectedItem().toString();
                serviceCost = calculateRegularWashCost(selectedServiceOption, noOfLoads); // Pass noOfLoads

                switch (selectedServiceOption) {
                    case "Wash - Php 65.00":
                        machineNeeded = "Washing Machine";
                        break;
                    case "Dry - Php 70.00":
                        machineNeeded = "Dryer";
                        break;
                    case "Full Service - Php 165.00":
                        machineNeeded = "Washing Machine & Dryer";
                        break;
                }

                break;
            case "Executive Wash":
                selectedServiceOption = executiveWashSpinner.getSelectedItem().toString();
                serviceCost = calculateExecutiveWashCost(selectedServiceOption, noOfPcsInt, noOfLoads); // Pass noOfLoads

                switch (selectedServiceOption) {
                    case "Wash/Dry + Steam - Php 30/pc.":
                        machineNeeded = "Washing Machine & Dryer";
                        break;
                    case "Wash/Dry + Treatment - Php 25/pc.":
                        machineNeeded = "Washing Machine & Dryer";
                        break;
                }

                break;
            case "Special Wash":
                selectedServiceOption = specialWashSpinner.getSelectedItem().toString();
                serviceCost = calculateSpecialWashCost(selectedServiceOption, noOfLoads); // Pass noOfLoads

                switch (selectedServiceOption) {
                    case "Warm Wash - Php 80.00":
                        machineNeeded = "Washing Machine";
                        break;
                    case "Hot Wash - Php 90.00":
                        machineNeeded = "Washing Machine";
                        break;
                    case "Disinfect Wash - Php 120.00":
                        machineNeeded = "Washing Machine";
                        break;
                }

                break;
            case "Comforter/Bulky":
                selectedServiceOption = comforterWashSpinner.getSelectedItem().toString();
                serviceCost = calculateComforterWashCost(selectedServiceOption, noOfLoads); // Pass noOfLoads

                switch (selectedServiceOption) {
                    case "Wash+Dry - Php 160.00":
                    case "Wash+Dry+Fold - Php 185.00":
                        machineNeeded = "Washing Machine & Dryer";
                        break;
                }

                break;
            case "Dry Clean":
                selectedServiceOption = dryCleanWashSpinner.getSelectedItem().toString();
                serviceCost = calculateDryCleanCost(selectedServiceOption, noOfLoads);  // Dry Clean may not depend on loads

                switch (selectedServiceOption) {
                    case "Barong/Tops/Coat - Php 250.00":
                    case "Dress/Terno - Php 450.00":
                    case "Shoes/Bag - Php 500.00":
                        machineNeeded = "Dry Cleaning Machine";
                        break;
                }

                break;
            default:
                machineNeeded = "Unknown";
                break;
        }

        Log.d("ServiceDetails", "Selected Service Option: " + selectedServiceOption + ", Machine Needed: " + machineNeeded);

        // Add the addon cost if any
        double addOnCost = 0.0;
        if(selectedAddOn.equals("Add Wash") || selectedAddOn.equals("Add Rinse") || selectedAddOn.equals("Add Dry")) {
            addOnCost = 20.00;
        }

        final Wrapper<Double> totalCostWrapper = new Wrapper<>(serviceCost + addOnCost);
        applyDiscountIfAvailable(totalCostWrapper);

        String orderStatus = "Pending";
        long timestamp = System.currentTimeMillis();

        // Format the timestamp to ISO 8601 format
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        String formattedDate = sdf.format(new Date(timestamp));

        // Create the map to store order data
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("laundryService", selectedLaundryService);
        orderData.put("laundryServiceOption", selectedServiceOption);
        orderData.put("typeOfClothes", selectedTypeOfClothes);
        orderData.put("typeOfDetergent", selectedTypeOfDetergent);
        orderData.put("addOn", selectedAddOn);
        orderData.put("paymentMode", selectedPaymentMode);
        orderData.put("noOfLoads", noOfLoads);
        orderData.put("noOfShirts", totalShirts);
        orderData.put("noOfPants", totalPants);
        orderData.put("noOfShorts", totalShorts);
        orderData.put("noOfUnderwear", totalUnderwear);
        orderData.put("noOfSkirts", totalSkirts);
        orderData.put("noOfPajamas", totalPajamas);
        orderData.put("noOfSocks", totalSocks);
        orderData.put("noOfComfortersPillows", totalComfortersPillows);
        orderData.put("noOfTowelsRugs", totalTowelsRugs);
        orderData.put("noOfBedSheets", totalBedSheets);
        orderData.put("noOfCurtains", totalCurtains);
        orderData.put("noOfTops", totalTops);
        orderData.put("noOfDresses", totalDresses);
        orderData.put("noOfSwimwear", totalSwimwear);
        orderData.put("noOfShoes", totalShoes);
        orderData.put("noOfBags", totalBags);
        orderData.put("noOfHats", totalHats);
        orderData.put("noOfScarves", totalScarves);
        orderData.put("noOfGloves", totalGloves);
        orderData.put("executiveWashNoOfPcs", noOfPcsInt);
        orderData.put("noteToStaff", noteToStaff.getText().toString());
        orderData.put("orderAddress", addressField.getText().toString().trim());
        orderData.put("totalCost", totalCostWrapper.getValue());
        orderData.put("orderStatus", orderStatus);
        orderData.put("machineNeeded", machineNeeded);
        orderData.put("orderTimestamp", formattedDate);

        return orderData;
    }

    private double calculateRegularWashCost(String option, int noOfLoads) {
        double baseCost = 0.0;
        switch (option) {
            case "Wash - Php 65.00":
                baseCost = 65.00;
                break;
            case "Dry - Php 70.00":
                baseCost = 70.00;
                break;
            case "Full Service - Php 165.00":
                baseCost = 165.00;
                break;
        }
        return baseCost * noOfLoads;
    }

    private double calculateExecutiveWashCost(String option, int noOfPcs, int noOfLoads) {
        double baseCost = 0.0;
        switch (option) {
            case "Wash/Dry + Steam - Php 30/pc.":
                baseCost = 30.00 * noOfPcs;
                break;
            case "Wash/Dry + Treatment - Php 25/pc.":
                baseCost = 25.00 * noOfPcs;
                break;
        }
        return baseCost * noOfLoads;
    }

    private double calculateSpecialWashCost(String option, int noOfLoads) {
        double baseCost = 0.0;
        switch (option) {
            case "Warm Wash - Php 80.00":
                baseCost = 80.00;
                break;
            case "Hot Wash - Php 90.00":
                baseCost = 90.00;
                break;
            case "Disinfect Wash - Php 120.00":
                baseCost = 120.00;
                break;
        }
        return baseCost * noOfLoads;
    }

    private double calculateComforterWashCost(String option, int noOfLoads) {
        double baseCost = 0.0;
        switch (option) {
            case "Wash+Dry - Php 160.00":
                baseCost = 160.00;
                break;
            case "Wash+Dry+Fold - Php 185.00":
                baseCost = 185.00;
                break;
        }
        return baseCost * noOfLoads;
    }

    private double calculateDryCleanCost(String option, int noOfLoads) {
        double baseCost;
        switch (option) {
            case "Barong/Tops/Coat - Php 250.00":
                baseCost = 250.00;
                break;
            case "Dress/Terno - Php 450.00":
                baseCost = 450.00;
                break;
            case "Shoes/Bag - Php 500.00":
                baseCost = 500.00;
                break;
            default:
                return 0.00;
        }
        return baseCost * noOfLoads;
    }

    private void applyDiscountIfAvailable(Wrapper<Double> totalCostWrapper) {
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        DatabaseReference discountRef = databaseRef.child("UserDiscounts").child(userId);

        discountRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && Boolean.TRUE.equals(snapshot.child("isActive").getValue(Boolean.class))) {
                    Integer discountPercent = snapshot.child("discountPercent").getValue(Integer.class);
                    if (discountPercent != null) {
                        double discountAmount = totalCostWrapper.getValue() * discountPercent / 100;
                        totalCostWrapper.setValue(totalCostWrapper.getValue() - discountAmount);

                        // Set discount as inactive after use
                        discountRef.child("isActive").setValue(false);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("applyDiscountIfAvailable", "Error applying discount", error.toException());
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateClothingItems(String selectedType) {
        clothingItemList.clear(); // Clear the current list

        // Pass the selected type to the adapter to update the subcategories
        clothingAdapter.setClothingType(selectedType);

        switch (selectedType) {
            case "Cotton/Silk":
                clothingItemList.add(new ClothingItem("Cotton Shirt", "Shirts", 1));
                clothingItemList.add(new ClothingItem("Silk Dress", "Dresses", 2));
                break;

            case "Comforter/Bulky":
                clothingItemList.add(new ClothingItem("Comforter", "Comforters", 1));
                clothingItemList.add(new ClothingItem("Winter Jacket", "Jackets", 2));
                break;

            case "Accessories (Coat/Top, Dress/Terno, Shoes, Bags, Gown)":
                clothingItemList.add(new ClothingItem("Coat", "Accessories", 1));
                clothingItemList.add(new ClothingItem("Gown", "Accessories", 2));
                clothingItemList.add(new ClothingItem("Shoes", "Accessories", 3));
                break;

            default: // Default case for "Type of Clothes" or unmatched selection
                clothingItemList.add(new ClothingItem("Generic Shirt", "Others", 1));
                break;
        }

        clothingAdapter.notifyDataSetChanged(); // Notify adapter of data changes
    }

    private static class Wrapper<T> {
        private T value;

        public Wrapper(T value) {
            this.value = value;
        }

        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }
    }
}