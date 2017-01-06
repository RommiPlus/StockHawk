package com.udacity.stockhawk.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.udacity.stockhawk.R;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class PrefUtils {

    private PrefUtils() {
    }

    public static Set<String> getStocks(Context context) {
        String stocksKey = context.getString(R.string.pref_stocks_key);
        String initializedKey = context.getString(R.string.pref_stocks_initialized_key);
        String[] defaultStocksList = context.getResources().getStringArray(R.array.default_stocks);

        HashSet<String> defaultStocks = new HashSet<>(Arrays.asList(defaultStocksList));
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);


        boolean initialized = prefs.getBoolean(initializedKey, false);

        if (!initialized) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(initializedKey, true);
            editor.putStringSet(stocksKey, defaultStocks);
            editor.apply();
            return defaultStocks;
        }
        return prefs.getStringSet(stocksKey, new HashSet<String>());

    }

    public static Set<String> getTestStocks(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String stocksKey = context.getString(R.string.pref_test_stocks_key);

        return prefs.getStringSet(stocksKey, new HashSet<String>());

    }

    public static void addStock(Context context, String symbol) {
        String key = context.getString(R.string.pref_stocks_key);
        Set<String> stocks = getStocks(context);
        editStockPref(context, key, stocks, symbol, true);
    }

    public static void removeStock(Context context, String symbol) {
        String key = context.getString(R.string.pref_stocks_key);
        Set<String> stocks = getStocks(context);
        editStockPref(context, key, stocks, symbol, false);
    }

    public static void addTestStock(Context context, String symbol) {
        String key = context.getString(R.string.pref_test_stocks_key);
        Set<String> stocks = getTestStocks(context);
        editStockPref(context, key, stocks, symbol, true);
    }

    public static void removeTestStock(Context context, String symbol) {
        String key = context.getString(R.string.pref_test_stocks_key);
        Set<String> stocks = getTestStocks(context);
        editStockPref(context, key, stocks, symbol, false);
    }

    private static void editStockPref(Context context, String key, Set<String> stocks,  String symbol, Boolean add) {
        if (add) {
            stocks.add(symbol);
        } else {
            stocks.remove(symbol);
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(key, stocks);
        editor.commit();
    }

    public static String getDisplayMode(Context context) {
        String key = context.getString(R.string.pref_display_mode_key);
        String defaultValue = context.getString(R.string.pref_display_mode_default);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(key, defaultValue);
    }

    public static void toggleDisplayMode(Context context) {
        String key = context.getString(R.string.pref_display_mode_key);
        String absoluteKey = context.getString(R.string.pref_display_mode_absolute_key);
        String percentageKey = context.getString(R.string.pref_display_mode_percentage_key);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String displayMode = getDisplayMode(context);

        SharedPreferences.Editor editor = prefs.edit();

        if (displayMode.equals(absoluteKey)) {
            editor.putString(key, percentageKey);
        } else {
            editor.putString(key, absoluteKey);
        }

        editor.apply();
    }

}
