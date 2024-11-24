package com.example.washwiz;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GCashScreen extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText referenceNoEditText;
    private TextView proofOfPaymentTextView, totalCostTextView;
    private static final int PICK_IMAGE_REQUEST = 1;

    private Uri imageUri;  // URI for the selected image
    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    private ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gcash_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("GCashPayments");
        storageReference = FirebaseStorage.getInstance().getReference("ProofOfPayment");

        referenceNoEditText = findViewById(R.id.reference_no);
        proofOfPaymentTextView = findViewById(R.id.proofOfPayment);
        totalCostTextView = findViewById(R.id.amountDue);

        String orderID = getIntent().getStringExtra("orderID");
        String currentUserID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(); // Get the current user's ID

        // Retrieve and display the total cost
        if (orderID != null) {
            DatabaseReference orderReference = FirebaseDatabase.getInstance()
                    .getReference("Orders")
                    .child(orderID);

            orderReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Verify that the userID inside this order matches the current user
                        String userIDInOrder = snapshot.child("userID").getValue(String.class);
                        if (currentUserID.equals(userIDInOrder)) {
                            // Retrieve the totalCost
                            Object totalCostValue = snapshot.child("totalCost").getValue();
                            String totalCost;

                            if (totalCostValue instanceof Long) {
                                // Convert Long to String
                                totalCost = String.valueOf(totalCostValue);
                            } else if (totalCostValue instanceof String) {
                                // If it's already a String
                                totalCost = (String) totalCostValue;
                            } else {
                                totalCost = "N/A";  // Fallback in case of an unexpected type
                            }

                            totalCostTextView.setText(String.format("Total Cost: Php %s", totalCost));
                        } else {
                            // User ID mismatch
                            Toast.makeText(GCashScreen.this, "Unauthorized access to this order", Toast.LENGTH_SHORT).show();
                            totalCostTextView.setText(R.string.total_cost_not_available);
                        }
                    } else {
                        totalCostTextView.setText(R.string.total_cost_not_available);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(GCashScreen.this, "Failed to retrieve total cost", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            totalCostTextView.setText(R.string.total_cost_not_available);
        }

        Button confirmButton = findViewById(R.id.confirm_button);
        int maxLength = 15;
        referenceNoEditText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(maxLength) });

        proofOfPaymentTextView.setOnClickListener(view -> openFileChooser());

        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        proofOfPaymentTextView.setText(R.string.image_selected);
                    }
                }
        );

        confirmButton.setOnClickListener(view -> processImageAndVerifyReferenceNumber());
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            // User is not signed in; redirect to login
            Intent intent = new Intent(GCashScreen.this, LoginScreen.class);
            startActivity(intent);
            finish();
        }
    }

    // Method to open file chooser to pick an image
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activityResultLauncher.launch(Intent.createChooser(intent, "Select Image"));

    }

    // Method to format the reference number with spaces
    private String formatReferenceNumber(String referenceNo) {
        // Remove any spaces from the input
        referenceNo = referenceNo.replaceAll("\\s+", "");

        // Check if the length is correct (13 characters, based on your pattern: 4 + 3 + 6)
        if (referenceNo.length() == 13) {
            // Format the reference number with spaces
            return referenceNo.substring(0, 4) + " " + referenceNo.substring(4, 7) + " " + referenceNo.substring(7);
        } else {
            // If it's not the correct length, return the original input (or handle as needed)
            return referenceNo;
        }
    }

    // Method to process the image and perform OCR
    private void processImageAndVerifyReferenceNumber() {
        final String referenceNo = referenceNoEditText.getText().toString().trim(); // Make referenceNo effectively final
        final String orderID = getIntent().getStringExtra("orderID"); // Make orderID effectively final

        if (referenceNo.isEmpty()) {
            Toast.makeText(this, "Please enter the reference number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri == null) {
            Toast.makeText(this, "Please upload proof of payment", Toast.LENGTH_SHORT).show();
            return;
        }

        if (orderID == null) {
            Toast.makeText(this, "Order ID is missing", Toast.LENGTH_SHORT).show();
            return; // Prevent further execution if orderID is null
        }

        // Format the user's reference number to match the OCR pattern
        final String formattedReferenceNo = formatReferenceNumber(referenceNo); // Use effectively final variable

        try {
            InputImage image = InputImage.fromFilePath(this, imageUri);
            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

            recognizer.process(image).addOnSuccessListener(visionText -> {
                String extractedText = visionText.getText();
                Log.d("GCashScreen", "Extracted Text: " + extractedText);

                // Use regex to find the reference number after "Ref No."
                String refNoPattern = "Ref No\\.\\s*([\\d\\s]+)";
                Pattern pattern = Pattern.compile(refNoPattern);
                Matcher matcher = pattern.matcher(extractedText);

                String extractedRefNo = null;
                if (matcher.find()) {
                    extractedRefNo = Objects.requireNonNull(matcher.group(1)).trim(); // Get the first capturing group and trim spaces
                    Log.d("GCashScreen", "Extracted Reference No: " + extractedRefNo);
                }

                // Format the extracted reference number to match the pattern
                String formattedExtractedRefNo = formatReferenceNumber(extractedRefNo); // Format extracted reference number
                Log.d("GCashScreen", "Formatted Extracted Reference No: " + formattedExtractedRefNo);

                // Check if the extracted reference number matches the input reference number
                if (formattedExtractedRefNo.equals(formattedReferenceNo)) {
                    uploadPaymentDetails(formattedReferenceNo, orderID);
                } else {
                    Toast.makeText(GCashScreen.this, "Reference number does not match the image", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> Toast.makeText(GCashScreen.this, "Failed to extract text from image: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        } catch (IOException e) {
            Log.e("GCashScreen", "Error processing image: " + e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            proofOfPaymentTextView.setText(R.string.image_selected);
        }
    }

    // Method to upload payment details
    private void uploadPaymentDetails(String referenceNo, String orderID) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userID = user.getUid();
            String imageFileName = userID + "_" + System.currentTimeMillis() + ".jpg";

            // Upload the proof of payment image
            StorageReference fileReference = storageReference.child(imageFileName);
            fileReference.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                // Retrieve download URL for the uploaded image
                fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    // Save payment details to the database
                    savePaymentDetails(userID, referenceNo, downloadUrl, orderID);
                });
            }).addOnFailureListener(e -> Toast.makeText(GCashScreen.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "User not authenticated. Please log in.", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to save payment details to the Firebase database
    private void savePaymentDetails(String userID, String referenceNo, String downloadUrl, String orderID) {
        DatabaseReference paymentRef = databaseReference.child(userID).child(orderID).push();
        String paymentID = paymentRef.getKey();
        String paymentStatus = "Pending";

        Map<String, Object> paymentData = new HashMap<>();
        paymentData.put("paymentID", paymentID);
        paymentData.put("referenceNo", referenceNo);
        paymentData.put("proofOfPaymentUrl", downloadUrl);
        paymentData.put("paymentStatus", paymentStatus);

        paymentRef.setValue(paymentData).addOnSuccessListener(aVoid -> {
            Toast.makeText(GCashScreen.this, "Payment details uploaded successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(GCashScreen.this, ReceiptScreen.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }).addOnFailureListener(e -> Toast.makeText(GCashScreen.this, "Failed to upload payment details: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}