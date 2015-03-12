package com.fiktivo.neighbourhood;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class PlacesAdapter extends CursorAdapter {

    private static final String FILENAME_PREF = "com.fiktivo.neighbourhood.preferences";

    public PlacesAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_place, parent, false);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        int id_place_name = cursor.getColumnIndex(PlacesContract.PlacesEntry.COLUMN_NAME_Place_Name);
        int id_place_rating = cursor.getColumnIndex(PlacesContract.PlacesEntry.COLUMN_NAME_Place_Rating);
        int id_place_latitude = cursor.getColumnIndex(PlacesContract.PlacesEntry.COLUMN_NAME_Place_Latitude);
        int id_place_longitude = cursor.getColumnIndex(PlacesContract.PlacesEntry.COLUMN_NAME_Place_Longitude);

        double placeLatitude = cursor.getDouble(id_place_latitude);
        double placeLongitude = cursor.getDouble(id_place_longitude);
        SharedPreferences sharedPref = context.getSharedPreferences(FILENAME_PREF, Context.MODE_PRIVATE);
        double currentLatitude = Double.parseDouble(sharedPref.getString("latitude", placeLatitude + ""));
        double currentLongitude = Double.parseDouble(sharedPref.getString("longitude", placeLongitude + ""));

        Location currentLocation = new Location("A");
        Location placeLocation = new Location("B");
        currentLocation.setLatitude(currentLatitude);
        currentLocation.setLongitude(currentLongitude);
        placeLocation.setLatitude(placeLatitude);
        placeLocation.setLongitude(placeLongitude);

        String distance = "~" + (int)currentLocation.distanceTo(placeLocation) + " Meters";

        TextView placeNameTextView = (TextView) view.findViewById(R.id.place_name_textview);
        TextView placeDistanceTextView = (TextView) view.findViewById(R.id.place_distance_textview);
        TextView placeRatingTextView = (TextView) view.findViewById(R.id.place_rating_textview);
        ImageView placeRatingImageView = (ImageView) view.findViewById(R.id.place_rating_imageview);

        placeNameTextView.setText(cursor.getString(id_place_name));
        placeDistanceTextView.setText(distance);
        double rating = cursor.getDouble(id_place_rating);
        if (rating == 6) {
            placeRatingTextView.setText("NA");
            placeRatingImageView.setImageResource(R.drawable.item_rating_dull);
        } else {
            placeRatingTextView.setText("" + cursor.getDouble(id_place_rating));
            placeRatingImageView.setImageResource(R.drawable.item_rating);
        }
    }
}
