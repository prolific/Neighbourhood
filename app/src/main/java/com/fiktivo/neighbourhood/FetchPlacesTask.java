package com.fiktivo.neighbourhood;

import android.content.ContentValues;
import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Vector;

import com.fiktivo.neighbourhood.PlacesContract.PlacesEntry;

public class FetchPlacesTask extends AsyncTask<String, Void, Void> {

    private final Context context;

    public FetchPlacesTask(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(String... params) {
        BufferedReader reader = null;
        HttpURLConnection urlConnection = null;
        String placesJsonStr = null;
        final String CATEGORY = params[0];
        try {
            final String BASE_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
            final String RANKBY = "distance";
            final String KEY = "AIzaSyBYDhJCyB_siLvmhJEHCCdOozV7lJbD26E";
            final String TYPES = getTypes(CATEGORY);
            final String LOCATION = params[1]; //"28.474388,77.50399";
            Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter("rankby", RANKBY)
                    .appendQueryParameter("types", TYPES)
                    .appendQueryParameter("location", LOCATION)
                    .appendQueryParameter("key", KEY)
                    .build();
            Log.v("URI", builtUri.toString());
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

            Log.d("FullJson", placesJsonStr);
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
        return null;
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
            placeValues.put(PlacesEntry.COLUMN_NAME_Category, placeCategory);
            placeValues.put(PlacesEntry.COLUMN_NAME_Place_ID, placeID);
            placeValues.put(PlacesEntry.COLUMN_NAME_Place_Name, placeName);
            placeValues.put(PlacesEntry.COLUMN_NAME_Place_Rating, placeRating);
            placeValues.put(PlacesEntry.COLUMN_NAME_Place_Latitude, placeLatitude);
            placeValues.put(PlacesEntry.COLUMN_NAME_Place_Longitude, placeLongitude);

            resultsVector.add(placeValues);
        }

        if (resultsVector.size() > 0) {
            ContentValues[] resultsArray = new ContentValues[resultsVector.size()];
            resultsVector.toArray(resultsArray);
            context.getContentResolver().delete(PlacesEntry.CONTENT_URI, PlacesEntry.COLUMN_NAME_Category + "=?", new String[]{placeCategory});
            Log.d("database", "rows deleted");
            context.getContentResolver().bulkInsert(PlacesEntry.CONTENT_URI, resultsArray);
            Log.d("database", "All rows inserted");
        }
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
}
