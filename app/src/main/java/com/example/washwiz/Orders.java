package com.example.washwiz;

public class Orders {
    private final String orderID;
    private final String orderDate;
    private final String orderStatus;
    private final String time;
    private final String reservedTimeSlotID;
    private final String orderETA;
    private final String pickupRider;
    private final String deliveryRider;

    public Orders(String orderID, String orderDate, String orderStatus, String time, String reservedTimeSlotID, String orderETA, String pickupRider, String deliveryRider) {
        this.orderID = orderID;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.time = time;
        this.reservedTimeSlotID = reservedTimeSlotID;
        this.orderETA = orderETA;
        this.pickupRider = pickupRider;
        this.deliveryRider = deliveryRider;
    }

    public String getOrderID() {
        return orderID;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public String getTime() {
        return time;
    }

    public String getReservedTimeSlotID() {
        return reservedTimeSlotID;
    }

    public String getOrderETA() {
        return orderETA;
    }

    public String getPickupRider() {
        return pickupRider;
    }

    public String getDeliveryRider() {
        return deliveryRider;
    }
}
