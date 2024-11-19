package com.example.washwiz;

public class ClothingItem {
    private String type;
    private String subCategory;
    private int quantity;

    public ClothingItem(String type, String subCategory, int quantity) {
        this.type = type;
        this.subCategory = subCategory;
        this.quantity = quantity;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
