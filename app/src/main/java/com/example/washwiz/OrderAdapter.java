package com.example.washwiz;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import android.graphics.Color;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private final List<Orders> orderList;
    private final DatabaseReference ordersRef; // Pass this reference for order cancellation

    // Constructor
    public OrderAdapter(List<Orders> orderList, DatabaseReference ordersRef, String userID) {
        this.orderList = orderList;
        this.ordersRef = ordersRef;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_item, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Orders order = orderList.get(position);

        String orderStatus = order.getOrderStatus();
        Log.d("OrderStatus", "Current Order Status: " + orderStatus);

        int statusColor;
        switch (orderStatus.toLowerCase()) {
            case "pending":
            case "cancelled":
                statusColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.red);
                break;
            case "for pickup":
            case "ready for delivery":
            case "out for delivery":
            case "ongoing":
                statusColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.green);
                break;
            case "completed":
                statusColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.gray);
                break;
            default:
                statusColor = Color.GRAY;
        }

        holder.orderNoTextView.setText(String.format("Order No.: %s", order.getOrderID()));
        holder.orderDateTextView.setText(String.format("Order Date: %s", order.getOrderDate()));
        holder.orderStatusLabel.setText(R.string.order_status);
        holder.orderStatusValue.setText(orderStatus);
        holder.orderStatusValue.setTextColor(statusColor);
        if ("Ongoing".equals(order.getOrderStatus()) || "Out for Delivery".equals(order.getOrderStatus())) {
            holder.orderETATextView.setVisibility(View.VISIBLE);
            String orderETA = order.getOrderETA();

            // Remove 'Z' from the timestamp to make it compatible with older APIs
            if (orderETA.endsWith("Z")) {
                orderETA = orderETA.substring(0, orderETA.length() - 1); // Remove 'Z'
            }

            // Pattern without 'X' for timezone
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());
            inputFormat.setTimeZone(TimeZone.getTimeZone("UTC")); // Set timezone to UTC manually
            SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault()); // 12-hour format with AM/PM

            try {
                Date date = inputFormat.parse(orderETA); // Parse the ETA string
                if (date != null) {
                    String formattedTime = outputFormat.format(date); // Format the date to time
                    holder.orderETATextView.setText(String.format("Receive by: %s", formattedTime));
                }
            } catch (Exception e) {
                Log.e("TAG", "Error occurred: ", e);
                holder.orderETATextView.setText(R.string.receive_by_invalid_time);
            }
        } else {
            holder.orderETATextView.setVisibility(View.GONE);
        }

        // Handle cancel button visibility
        if ("Pending".equals(order.getOrderStatus())) {
            holder.cancelButton.setVisibility(View.VISIBLE);

            holder.cancelButton.setOnClickListener(v -> {
                // Call cancel order function when cancel button is clicked
                cancelOrder(order, holder.orderStatusValue, holder.cancelButton);
            });
        } else {
            holder.cancelButton.setVisibility(View.GONE); // Hide the button if not "For Pickup"
        }

        if("For Pickup".equals(order.getOrderStatus())) {
            holder.pickupRiderTextView.setVisibility(View.VISIBLE);
            holder.pickupRiderTextView.setText(String.format("Pickup Rider: %s", order.getPickupRider()));
        } else {
            holder.pickupRiderTextView.setVisibility(View.GONE);
        }

        if("Out for Delivery".equals(order.getOrderStatus())) {
            holder.deliveryRiderTextView.setVisibility(View.VISIBLE);
            holder.deliveryRiderTextView.setText(String.format("Delivery Rider: %s", order.getDeliveryRider()));
            holder.orderCompleteBtn.setVisibility(View.VISIBLE);
            holder.orderCompleteBtn.setOnClickListener(v -> {
                completeOrder(order, holder.orderStatusValue, holder.orderCompleteBtn);
                Intent intent = new Intent(v.getContext(), FeedbackScreen.class);
                intent.putExtra("orderID", order.getOrderID());
                v.getContext().startActivity(intent);
            });
        } else {
            holder.orderETATextView.setVisibility(View.GONE);
            holder.deliveryRiderTextView.setVisibility(View.GONE);
            holder.orderCompleteBtn.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    // Cancel order logic
    private void cancelOrder(Orders order, TextView orderStatusTextView, Button cancelButton) {
        String orderID = order.getOrderID();
        String chosenDate = order.getOrderDate(); // Adjust based on actual field storing the date
        String chosenTime = order.getTime();      // Ensure you have time stored for cancellation
        String uniqueKey = order.getReservedTimeSlotID(); // Ensure you have the unique key

        // Update order status in Firebase
        ordersRef.child(orderID).child("orderStatus").setValue("Cancelled").addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Update UI after cancellation
                orderStatusTextView.setText(R.string.order_status_cancelled);
                cancelButton.setVisibility(View.GONE); // Hide the cancel button after cancelling
                Toast.makeText(cancelButton.getContext(), "Order has been cancelled", Toast.LENGTH_SHORT).show();

                // Remove the reserved time slot
                if (chosenDate != null && chosenTime != null && uniqueKey != null) {
                    removeReservedTimeSlot(chosenDate, chosenTime, uniqueKey);
                }
            } else {
                Toast.makeText(cancelButton.getContext(), "Failed to cancel order", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to remove reserved time slot
    private void removeReservedTimeSlot(String chosenDate, String chosenTime, String uniqueKey) {
        DatabaseReference reservedSlotsRef = FirebaseDatabase.getInstance()
                .getReference().child("ReservedTimeSlots").child(chosenDate).child(chosenTime);

        reservedSlotsRef.child(uniqueKey).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("Reservation", "Time slot freed successfully for " + chosenDate + " " + chosenTime);
            } else {
                Log.e("Reservation", "Failed to free time slot for " + chosenDate + " " + chosenTime);
            }
        }).addOnFailureListener(e -> Log.e("Reservation", "Error freeing time slot: " + e.getMessage()));
    }

    // Method to complete the order
    private void completeOrder(Orders order, TextView orderStatusTextView, Button orderCompleteBtn) {
        String orderID = order.getOrderID();

        // Update order status in Firebase
        ordersRef.child(orderID).child("orderStatus").setValue("Completed").addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Update UI after completing the order
                orderStatusTextView.setText(R.string.order_status_completed);
                orderCompleteBtn.setVisibility(View.GONE);
                Toast.makeText(orderCompleteBtn.getContext(), "Order marked as completed", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(orderCompleteBtn.getContext(), "Failed to mark order as completed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {

        TextView orderNoTextView, orderDateTextView, orderStatusLabel, orderStatusValue, orderETATextView, pickupRiderTextView, deliveryRiderTextView;
        Button cancelButton, orderCompleteBtn;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderNoTextView = itemView.findViewById(R.id.order_no);
            orderDateTextView = itemView.findViewById(R.id.order_date);
            orderStatusLabel = itemView.findViewById(R.id.order_status_label);
            orderStatusValue = itemView.findViewById(R.id.order_status_value);
            orderETATextView = itemView.findViewById(R.id.order_ETA);
            pickupRiderTextView = itemView.findViewById(R.id.pickup_rider);
            deliveryRiderTextView = itemView.findViewById(R.id.delivery_rider);
            cancelButton = itemView.findViewById(R.id.cancel_button);
            orderCompleteBtn = itemView.findViewById(R.id.completeButton);
        }
    }
}