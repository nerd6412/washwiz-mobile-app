package com.example.washwiz;

public class ClothingItem {
    private String subCategory; // E.g., "Cotton Shirt"
    private String specificOption; // E.g., "Shirts"
    private int quantity; // E.g., 1

    // Constructor
    public ClothingItem(String subCategory, String specificOption, int quantity) {
        this.subCategory = subCategory;
        this.specificOption = specificOption;
        this.quantity = quantity;
    }

    // Getter and Setter for subCategory
    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    // Getter and Setter for specificOption
    public String getSpecificOption() {
        return specificOption;
    }

    public void setSpecificOption(String specificOption) {
        this.specificOption = specificOption;
    }

    // Getter and Setter for quantity
    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}