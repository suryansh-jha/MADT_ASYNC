package com.example.asyncprocessing_madt;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CurrencyAdapter extends ArrayAdapter<Currency> {

    private List<Currency> originalList; // Original list (before filtering)
    private List<Currency> filteredList; // The filtered list (after search)
    private CurrencyFilter filter;

    public CurrencyAdapter(Context context, List<Currency> currencies) {
        super(context, 0, currencies);
        this.originalList = new ArrayList<>(currencies);
        this.filteredList = currencies;  // Initially, the filtered list is the same as the original
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.currency_item, parent, false);
        }

        Currency currency = getItem(position); // Get the filtered currency
        TextView currencyCodeView = convertView.findViewById(R.id.currencyCode);
        TextView rateView = convertView.findViewById(R.id.rate);

        if (currency != null) {
            currencyCodeView.setText(currency.getCode()); // Display the currency code
            rateView.setText(String.format("%.3f", currency.getRate())); // Display the rate with 3 decimals
        }

        return convertView;
    }

    @Override
    public int getCount() {
        return filteredList.size(); // Return the size of the filtered list
    }

    @Override
    public Currency getItem(int position) {
        return filteredList.get(position); // Get the item from the filtered list
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new CurrencyFilter();
        }
        return filter;
    }

    // Custom filter class to filter the list based on currency code
    private class CurrencyFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            List<Currency> filteredCurrencies = new ArrayList<>();

            // If the constraint is null or empty, show all items
            if (constraint == null || constraint.length() == 0) {
                filteredCurrencies.addAll(originalList);
            } else {
                // Convert to lowercase and trim to match input
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (Currency currency : originalList) {
                    if (currency.getCode().toLowerCase().contains(filterPattern)) {
                        filteredCurrencies.add(currency);
                    }
                }
            }

            // Set the filtered list and count the results
            results.values = filteredCurrencies;
            results.count = filteredCurrencies.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results.values != null) {
                filteredList = (List<Currency>) results.values; // Update filtered list
                notifyDataSetChanged(); // Notify that the data has changed
            }
        }
    }
}
