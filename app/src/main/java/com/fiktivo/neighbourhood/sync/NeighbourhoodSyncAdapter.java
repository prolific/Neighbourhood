package com.fiktivo.neighbourhood.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Bundle;

import com.fiktivo.neighbourhood.PlacesContract;
import com.fiktivo.neighbourhood.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

public class NeighbourhoodSyncAdapter extends AbstractThreadedSyncAdapter {

    private static String CATEGORY;
    private static String PLACEID;
    private static String FETCHTYPE;
    private static final String FILENAME_PREF = "com.fiktivo.neighbourhood.preferences";

    public NeighbourhoodSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        if (FETCHTYPE.equals("places"))
            fetchPlaces();
        else
            fetchDetails();
    }

    public void fetchPlaces() {
        BufferedReader reader = null;
        HttpURLConnection urlConnection = null;
        String placesJsonStr = null;
        try {
            final String BASE_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
            final String RANKBY = "distance";
            final String KEY = ""; //PUT YOUR API KEY HERE
            String TYPES = getTypes(CATEGORY);
            SharedPreferences sharedPref = getContext().getSharedPreferences(FILENAME_PREF, Context.MODE_PRIVATE);
            double currentLatitude = Double.parseDouble(sharedPref.getString("latitude", ""));
            double currentLongitude = Double.parseDouble(sharedPref.getString("longitude", ""));
            String LOCATION = currentLatitude + "," + currentLongitude;
            Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter("rankby", RANKBY)
                    .appendQueryParameter("types", TYPES)
                    .appendQueryParameter("location", LOCATION)
                    .appendQueryParameter("key", KEY)
                    .build();
            URL url = new URL(builtUri.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null)
                buffer.append(line + "\n");

            placesJsonStr = buffer.toString();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        try {
            if (placesJsonStr.length() != 0)
                getPlacesDataFromJson(placesJsonStr, CATEGORY);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return;
    }

    public void fetchDetails() {
        BufferedReader reader = null;
        HttpURLConnection urlConnection = null;
        String placeJsonStr = null;
        try {
            final String BASE_URL = "https://maps.googleapis.com/maps/api/place/details/json?";
            final String KEY = "AIzaSyBYDhJCyB_siLvmhJEHCCdOozV7lJbD26E";
            Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter("placeid", PLACEID)
                    .appendQueryParameter("key", KEY)
                    .build();
            URL url = new URL(builtUri.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null)
                buffer.append(line + "\n");

            placeJsonStr = buffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        try {
            if (placeJsonStr.length() != 0)
                getPlaceDetailsFromJson(placeJsonStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return;
    }

    public void getPlacesDataFromJson(String placesJsonStr, String placeCategory) throws JSONException {
        JSONObject placesJson = new JSONObject(placesJsonStr);
        JSONArray results = placesJson.getJSONArray("results");

        Vector<ContentValues> resultsVector = new Vector<ContentValues>(results.length());

        for (int i = 0; i < results.length(); i++) {
            JSONObject place = results.getJSONObject(i);
            String placeID = place.getString("place_id");
            String placeName = place.getString("name");
            double placeRating;
            if (place.has("rating"))
                placeRating = place.getDouble("rating");
            else
                placeRating = 6;
            JSONObject placeLocation = place.getJSONObject("geometry").getJSONObject("location");
            double placeLatitude = placeLocation.getDouble("lat");
            double placeLongitude = placeLocation.getDouble("lng");

            ContentValues placeValues = new ContentValues();
            placeValues.put(PlacesContract.PlacesEntry.COLUMN_NAME_Category, placeCategory);
            placeValues.put(PlacesContract.PlacesEntry.COLUMN_NAME_Place_ID, placeID);
            placeValues.put(PlacesContract.PlacesEntry.COLUMN_NAME_Place_Name, placeName);
            placeValues.put(PlacesContract.PlacesEntry.COLUMN_NAME_Place_Rating, placeRating);
            placeValues.put(PlacesContract.PlacesEntry.COLUMN_NAME_Place_Latitude, placeLatitude);
            placeValues.put(PlacesContract.PlacesEntry.COLUMN_NAME_Place_Longitude, placeLongitude);

            resultsVector.add(placeValues);
        }

        if (resultsVector.size() > 0) {
            ContentValues[] resultsArray = new ContentValues[resultsVector.size()];
            resultsVector.toArray(resultsArray);
            getContext().getContentResolver().delete(PlacesContract.PlacesEntry.CONTENT_URI, PlacesContract.PlacesEntry.COLUMN_NAME_Category + "=?", new String[]{placeCategory});
            getContext().getContentResolver().bulkInsert(PlacesContract.PlacesEntry.CONTENT_URI, resultsArray);
        }
    }

    public void getPlaceDetailsFromJson(String placeJsonStr) throws JSONException {
        JSONObject placeJson = new JSONObject(placeJsonStr);
        JSONObject result = placeJson.getJSONObject("result");
        String placeAddress = result.getString("formatted_address");
        String placePhoneNumber = "";
        if (result.has("formatted_phone_number"))
            placePhoneNumber = result.getString("formatted_phone_number");
        String placeWebsite = "";
        if (result.has("website"))
            placeWebsite = result.getString("website");

        Intent i = new Intent(NeighbourhoodSyncService.SYNC_FIINISHED);
        i.putExtra("Details", new String[]{placeAddress, placePhoneNumber, placeWebsite});
        getContext().sendBroadcast(i);

    }

    public String getTypes(String category) {
        String types = "";
        switch (category) {
            case "Restaurants and Cafe":
                types = "restaurant|cafe|food";
                break;
            case "Hospitals":
                types = "hospital|health";
                break;
            case "Malls":
                types = "shopping_mall";
                break;
            case "ATM":
                types = "atm";
                break;
            case "Bars":
                types = "bar";
                break;
            case "Hotels":
                types = "lodging";
                break;
            case "Movie Theatres":
                types = "movie_theater";
                break;
            case "Tourist Places":
                types = "museum|amusement_park|church|hindu_temple|mosque|zoo";
                break;
        }
        return types;
    }

    public static void syncImmediately(Context context, String category, String placeID, String fetchType) {
        CATEGORY = category;
        PLACEID = placeID;
        FETCHTYPE = fetchType;
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    public static Account getSyncAccount(Context context) {
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        if (null == accountManager.getPassword(newAccount))
            if (!accountManager.addAccountExplicitly(newAccount, "", null))
                return null;

        return newAccount;
    }
}
