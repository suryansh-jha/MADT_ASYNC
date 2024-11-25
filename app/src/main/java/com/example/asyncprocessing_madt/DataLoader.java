package com.example.asyncprocessing_madt;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DataLoader extends AsyncTask<String, Void, List<Currency>> {

    private final DataLoadedCallback callback;

    public DataLoader(DataLoadedCallback callback) {
        this.callback = callback;
    }

    @Override
    @SuppressWarnings("deprecation")
    protected List<Currency> doInBackground(String... urls) {
        try {
            String apiUrl = urls[0]; // URL passed as parameter
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("apikey", "Gq1VdgFbprDGFswxyLkkFKrLp5GKv9ZZ"); // API Key
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();

            // Log the full response for debugging
            Log.d("DataLoader", "Response: " + response.toString());

            return parseJson(response.toString());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("DataLoader", "Error fetching data", e);
            return null;
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void onPostExecute(List<Currency> currencies) {
        // Notify the main thread once the data is fetched
        if (currencies != null) {
            callback.onDataLoaded(currencies);
        } else {
            callback.onDataLoaded(null);
        }
    }

    // Parse the JSON response into a list of Currency objects
    private List<Currency> parseJson(String json) throws Exception {
        if (json == null || json.isEmpty()) {
            Log.e("DataLoader", "Received empty response");
            return null;
        }

        // Choose the appropriate parsing method based on the API endpoint response format
        JSONObject jsonObject = new JSONObject(json);
        if (jsonObject.has("rates")) {
            // For latest exchange rates, parse the "rates" object
            return parseExchangeRates(jsonObject);
        } else if (jsonObject.has("symbols")) {
            // For symbols (available currencies), parse the "symbols" object
            return parseSymbols(jsonObject);
        } else {
            // Handle other responses, such as error responses or conversion results
            return null;
        }
    }

    // Parse exchange rates from the /latest endpoint
    private List<Currency> parseExchangeRates(JSONObject jsonObject) throws Exception {
        JSONObject rates = jsonObject.getJSONObject("rates");
        List<Currency> currencies = new ArrayList<>();

        // Log the rates object to debug
        Log.d("DataLoader", "Rates: " + rates.toString());

        Iterator<String> keys = rates.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            double rate = rates.getDouble(key);
            // Log each rate to verify the data
            Log.d("DataLoader", "Currency: " + key + " Rate: " + rate);
            currencies.add(new Currency(key, rate));  // Using constructor for /latest
        }

        return currencies;
    }

    // Parse the available currencies from the /symbols endpoint
    private List<Currency> parseSymbols(JSONObject jsonObject) throws Exception {
        JSONObject symbols = jsonObject.getJSONObject("symbols");
        List<Currency> currencies = new ArrayList<>();

        // Log the symbols object to debug
        Log.d("DataLoader", "Symbols: " + symbols.toString());

        // Fetch all available currencies
        Iterator<String> keys = symbols.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            String currencyName = symbols.getString(key);
            Log.d("DataLoader", "Currency: " + key + " Name: " + currencyName);
            currencies.add(new Currency(key, currencyName));  // Currency code and name
        }

        // Now, fetch exchange rates for these currencies using the /latest endpoint
        // Fetch exchange rates using USD as base currency
        List<Currency> currencyWithRates = fetchExchangeRatesForCurrencies(currencies);
        return currencyWithRates;
    }

    // Fetch exchange rates for a list of currencies
    private List<Currency> fetchExchangeRatesForCurrencies(List<Currency> currencies) {
        List<Currency> currenciesWithRates = new ArrayList<>();
        try {

            StringBuilder symbols = new StringBuilder();
            for (Currency currency : currencies) {
                symbols.append(currency.getCode()).append(",");
            }

            symbols.setLength(symbols.length() - 1);

            String url = "https://api.apilayer.com/exchangerates_data/latest?base=USD&symbols=" + symbols.toString();
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("apikey", "Gq1VdgFbprDGFswxyLkkFKrLp5GKv9ZZ");
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();


            Log.d("DataLoader", "Exchange Rates Response: " + response.toString());

            // Parse the exchange rates response
            JSONObject responseObject = new JSONObject(response.toString());
            JSONObject rates = responseObject.getJSONObject("rates");

            for (Currency currency : currencies) {
                if (rates.has(currency.getCode())) {
                    double rate = rates.getDouble(currency.getCode());
                    currenciesWithRates.add(new Currency(currency.getCode(), rate));  // Adding currency with rate
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("DataLoader", "Error fetching exchange rates", e);
        }
        return currenciesWithRates;
    }


    public interface DataLoadedCallback {
        void onDataLoaded(List<Currency> currencies);
    }
}
