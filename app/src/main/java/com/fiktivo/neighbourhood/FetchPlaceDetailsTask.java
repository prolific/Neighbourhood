package com.fiktivo.neighbourhood;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FetchPlaceDetailsTask extends AsyncTask<String, Void, String[]> {

    View rootView;

    public FetchPlaceDetailsTask(View view) {
        rootView = view;
    }

    @Override
    protected String[] doInBackground(String... params) {
        BufferedReader reader = null;
        HttpURLConnection urlConnection = null;
        String placeJsonStr = null;
        final String PLACE_ID = params[0];
        try {
            final String BASE_URL = "https://maps.googleapis.com/maps/api/place/details/json?";
            final String KEY = "AIzaSyBYDhJCyB_siLvmhJEHCCdOozV7lJbD26E";
            Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter("placeid", PLACE_ID)
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

            Log.d("FullJson", placeJsonStr);
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
                return getPlaceDetailsFromJson(placeJsonStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String[] strings) {
        if (strings != null) {
            String placeAddress = strings[0];
            String placePhoneNumber = strings[1];
            String placeWebsite = strings[2];
            ((TextView) rootView.findViewById(R.id.place_address_textview)).setText(placeAddress);
            if (!placePhoneNumber.equals(""))
                ((TextView) rootView.findViewById(R.id.place_phone_textview)).setText(placePhoneNumber);
            if (!placeWebsite.equals(""))
                ((TextView) rootView.findViewById(R.id.place_website_textview)).setText(placeWebsite);
        }
    }

    public String[] getPlaceDetailsFromJson(String placeJsonStr) throws JSONException {
        JSONObject placeJson = new JSONObject(placeJsonStr);
        JSONObject result = placeJson.getJSONObject("result");
        String placeAddress = result.getString("formatted_address");
        String placePhoneNumber = "";
        if (result.has("formatted_phone_number"))
            placePhoneNumber = result.getString("formatted_phone_number");
        String placeWebsite = "";
        if (result.has("website"))
            placeWebsite = result.getString("website");

        return new String[]{placeAddress, placePhoneNumber, placeWebsite};
    }
}
