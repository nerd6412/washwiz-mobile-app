package com.example.washwiz;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ClothingAdapter extends RecyclerView.Adapter<ClothingAdapter.ClothingViewHolder> {

    private final List<ClothingItem> clothingItemList;
    private final Context context;

    public ClothingAdapter(List<ClothingItem> clothingItemList, Context context) {
        this.clothingItemList = clothingItemList;
        this.context = context;
    }

    @NonNull
    @Override
    public ClothingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_clothing, parent, false);
        return new ClothingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ClothingViewHolder holder, int position) {
        ClothingItem clothingItem = clothingItemList.get(position);

        // Set up the Type Spinner
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(
                context, R.array.clothing_type_options, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.clothingTypeSpinner.setAdapter(typeAdapter);

        // Set previously selected type
        if (clothingItem.getType() != null) {
            int spinnerPosition = typeAdapter.getPosition(clothingItem.getType());
            holder.clothingTypeSpinner.setSelection(spinnerPosition);
        }

        // Update subcategory spinner based on type
        updateSubCategorySpinner(holder, clothingItem.getType());

        // Listen for changes in type selection
        holder.clothingTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int selectedPosition, long id) {
                String selectedType = parentView.getItemAtPosition(selectedPosition).toString();
                clothingItem.setType(selectedType);
                updateSubCategorySpinner(holder, selectedType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Handle case when nothing is selected if necessary
            }
        });

        // Listen for changes in subcategory selection
        holder.clothingSubCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int selectedPosition, long id) {
                String selectedSubCategory = parentView.getItemAtPosition(selectedPosition).toString();
                clothingItem.setSubCategory(selectedSubCategory);
                updateSpecificOptions(holder, selectedSubCategory);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Handle case when nothing is selected if necessary
            }
        });

        // Set quantity input
        holder.clothingQuantityEditText.setText(String.valueOf(clothingItem.getQuantity()));
        holder.clothingQuantityEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    clothingItem.setQuantity(Integer.parseInt(s.toString()));
                } catch (NumberFormatException e) {
                    clothingItem.setQuantity(0);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Set up delete button to remove the item
        holder.deleteRowButton.setOnClickListener(v -> removeItem(position));
    }

    @Override
    public int getItemCount() {
        return clothingItemList.size();
    }

    // Method to remove item
    private void removeItem(int position) {
        if (position >= 0 && position < clothingItemList.size()) {
            clothingItemList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, clothingItemList.size());
        }
    }

    public static class ClothingViewHolder extends RecyclerView.ViewHolder {
        Spinner clothingTypeSpinner;
        Spinner clothingSubCategorySpinner;
        Spinner specificOptionsSpinner;
        EditText clothingQuantityEditText;
        ImageButton deleteRowButton; // Added delete button

        public ClothingViewHolder(View itemView) {
            super(itemView);
            clothingTypeSpinner = itemView.findViewById(R.id.clothingTypeSpinner);
            clothingSubCategorySpinner = itemView.findViewById(R.id.clothingSubCategorySpinner);
            specificOptionsSpinner = itemView.findViewById(R.id.specificOptionsSpinner);
            clothingQuantityEditText = itemView.findViewById(R.id.clothingQuantityEditText);
            deleteRowButton = itemView.findViewById(R.id.deleteRowButton); // Link delete button
        }
    }

    private void updateSubCategorySpinner(ClothingViewHolder holder, String selectedType) {
        int subCategoryArrayId;

        switch (selectedType) {
            case "Cotton/Silky":
                subCategoryArrayId = R.array.cotton_silky_options;
                break;
            case "Comforter/Bulky":
                subCategoryArrayId = R.array.comforter_bulky_options;
                break;
            case "Accessories":
                subCategoryArrayId = R.array.accessories_options;
                break;
            default:
                subCategoryArrayId = R.array.default_subcategories;
                break;
        }

        ArrayAdapter<CharSequence> subCategoryAdapter = ArrayAdapter.createFromResource(
                context, subCategoryArrayId, android.R.layout.simple_spinner_item);
        subCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.clothingSubCategorySpinner.setAdapter(subCategoryAdapter);
    }

    private void updateSpecificOptions(ClothingViewHolder holder, String selectedSubCategory) {
        int specificOptionsArrayId;

        switch (selectedSubCategory) {
            case "Shirts":
                specificOptionsArrayId = R.array.shirts_options;
                break;
            case "Pants":
                specificOptionsArrayId = R.array.pants_options;
                break;
            case "Shorts":
                specificOptionsArrayId = R.array.shorts_options;
                break;
            case "Underwear":
                specificOptionsArrayId = R.array.underwear_options;
                break;
            case "Skirts":
                specificOptionsArrayId = R.array.skirts_options;
                break;
            case "Pajamas":
                specificOptionsArrayId = R.array.pajamas_options;
                break;
            case "Socks":
                specificOptionsArrayId = R.array.socks_options;
                break;
            case "Comforters/Pillows":
                specificOptionsArrayId = R.array.comforters_options;
                break;
            case "Towels/Rugs":
                specificOptionsArrayId = R.array.towels_options;
                break;
            case "Bed Sheets":
                specificOptionsArrayId = R.array.bed_sheets_options;
                break;
            case "Curtains":
                specificOptionsArrayId = R.array.curtains_options;
                break;
            case "Tops":
                specificOptionsArrayId = R.array.tops_options;
                break;
            case "Dresses":
                specificOptionsArrayId = R.array.dresses_options;
                break;
            case "Shoes":
                specificOptionsArrayId = R.array.shoes_options;
                break;
            case "Bags":
                specificOptionsArrayId = R.array.bags_options;
                break;
            case "Hats":
                specificOptionsArrayId = R.array.hats_options;
                break;
            case "Scarves":
                specificOptionsArrayId = R.array.scarves_options;
                break;
            case "Gloves":
                specificOptionsArrayId = R.array.gloves_options;
                break;
            default:
                specificOptionsArrayId = R.array.default_subcategories;
                break;
        }

        ArrayAdapter<CharSequence> specificOptionsAdapter = ArrayAdapter.createFromResource(
                context, specificOptionsArrayId, android.R.layout.simple_spinner_item);
        specificOptionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.specificOptionsSpinner.setAdapter(specificOptionsAdapter);

        // Optionally, set the first specific option as default
        holder.specificOptionsSpinner.setSelection(0);
    }
}
