package com.example.washwiz;

public class History {
    String orderID, laundryService, totalCost, orderStatus;

    public History(String orderID, String laundryService, String totalCost, String orderStatus) {
        this.orderID = orderID;
        this.laundryService = laundryService;
        this.totalCost = totalCost;
        this.orderStatus = orderStatus;
    }

    public String getOrderID() {
        return orderID;
    }

    public String getLaundryService() {
        return laundryService;
    }

    public String getTotalCost() {
        return totalCost;
    }

    public String getOrderStatus() {
        return orderStatus;
    }
}
