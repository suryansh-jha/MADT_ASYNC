package com.example.asyncprocessing_madt;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private EditText searchBar;
    private CurrencyAdapter adapter;
    private List<Currency> currencyList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);
        searchBar = findViewById(R.id.searchBar);

        // Fetch the available currencies and their rates
        DataLoader dataLoader = new DataLoader(this::onDataLoaded);
        dataLoader.execute("https://api.apilayer.com/exchangerates_data/symbols");

        // Add text change listener to filter the ListView based on search
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.getFilter().filter(s.toString()); // Trigger the filter based on the search input
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void onDataLoaded(List<Currency> currencies) {
        if (currencies != null) {
            currencyList = currencies;
            adapter = new CurrencyAdapter(this, currencyList);
            listView.setAdapter(adapter);
        } else {
            Toast.makeText(this, "Failed to load data", Toast.LENGTH_SHORT).show();
        }
    }
}
