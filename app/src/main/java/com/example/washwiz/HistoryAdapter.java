package com.example.washwiz;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
    private final List<History> historyList;

    // Constructor
    public HistoryAdapter(List<History> historyList) {
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public HistoryAdapter.HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_history, parent, false);
        return new HistoryAdapter.HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryAdapter.HistoryViewHolder holder, int position) {
        History history = historyList.get(position);

        holder.orderNoTextView.setText(String.format("Order No.: %s", history.getOrderID()));
        holder.orderLaundryServiceTextView.setText(String.format("Laundry Service: %s", history.getLaundryService()));
        holder.orderTotalCost.setText(String.format("Total Amount Due: %s", history.getTotalCost()));
        holder.orderStatusTextView.setText(String.format("Order Status: %s", history.getOrderStatus()));

        if(!"Completed".equals(history.getOrderStatus()) && !"Cancelled".equals(history.getOrderStatus())) {
            holder.trackButton.setVisibility(View.VISIBLE);

            holder.trackButton.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), OrderStatusScreen.class);
                // Pass the orderID to the OrderStatusScreen (if needed)
                intent.putExtra("orderID", history.getOrderID());
                // Start the OrderStatusScreen activity
                v.getContext().startActivity(intent);
            });
        } else {
            holder.trackButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {

        TextView orderNoTextView, orderLaundryServiceTextView, orderTotalCost, orderStatusTextView;
        Button trackButton;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            orderNoTextView = itemView.findViewById(R.id.order_no);
            orderLaundryServiceTextView = itemView.findViewById(R.id.order_laundryService);
            orderTotalCost = itemView.findViewById(R.id.order_totalAmount);
            orderStatusTextView = itemView.findViewById(R.id.order_status);
            trackButton = itemView.findViewById(R.id.track_order);
        }
    }
}
