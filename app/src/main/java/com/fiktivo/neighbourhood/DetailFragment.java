package com.fiktivo.neighbourhood;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private String placeID = "";
    private View rootView;
    TextView placeNameTextView;
    TextView placeRatingTextView;
    TextView placeDistanceTextView;
    ImageButton mapsImageButton;

    public static final int PLACE_LOADER = 0;
    private static final String FILENAME_PREF = "com.fiktivo.neighbourhood.preferences";

    public DetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        Bundle arguments = getArguments();
        if (arguments != null)
            placeID = arguments.getString("placeID");

        placeNameTextView = (TextView) rootView.findViewById(R.id.place_name_textview);
        placeRatingTextView = (TextView) rootView.findViewById(R.id.place_rating_textview);
        placeDistanceTextView = (TextView) rootView.findViewById(R.id.place_distance_textview);
        mapsImageButton = (ImageButton) rootView.findViewById(R.id.maps_imagebutton);

        if (isNetworkAvailable())
            updateDetails();
        else
            Toast.makeText(getActivity(), "Please check your Internet Connection", Toast.LENGTH_LONG).show();

        return rootView;
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void updateDetails() {
        if (placeID.equals(""))
            Toast.makeText(getActivity(), "Please select a place to show details", Toast.LENGTH_LONG).show();
        else {
            FetchPlaceDetailsTask fetchPlaceDetailsTask = new FetchPlaceDetailsTask(rootView);
            fetchPlaceDetailsTask.execute(placeID);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(PLACE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri placeUri = PlacesContract.PlacesEntry.CONTENT_URI;
        return new CursorLoader(getActivity(), placeUri, null,
                PlacesContract.PlacesEntry.COLUMN_NAME_Place_ID + "=?", new String[]{placeID}, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            int nameID = data.getColumnIndex(PlacesContract.PlacesEntry.COLUMN_NAME_Place_Name);
            int ratingID = data.getColumnIndex(PlacesContract.PlacesEntry.COLUMN_NAME_Place_Rating);
            int latitudeID = data.getColumnIndex(PlacesContract.PlacesEntry.COLUMN_NAME_Place_Latitude);
            int longitudeID = data.getColumnIndex(PlacesContract.PlacesEntry.COLUMN_NAME_Place_Longitude);

            final double placeLatitude = data.getDouble(latitudeID);
            final double placeLongitude = data.getDouble(longitudeID);
            SharedPreferences sharedPref = getActivity().getSharedPreferences(FILENAME_PREF, Context.MODE_PRIVATE);
            double currentLatitude = Double.parseDouble(sharedPref.getString("latitude", placeLatitude + ""));
            double currentLongitude = Double.parseDouble(sharedPref.getString("longitude", placeLongitude + ""));

            Location currentLocation = new Location("A");
            Location placeLocation = new Location("B");
            currentLocation.setLatitude(currentLatitude);
            currentLocation.setLongitude(currentLongitude);
            placeLocation.setLatitude(placeLatitude);
            placeLocation.setLongitude(placeLongitude);

            String distance = "Approx. " + (int) currentLocation.distanceTo(placeLocation) + " Meters";

            placeNameTextView.setText(data.getString(nameID));
            double rating = data.getDouble(ratingID);
            if (rating != 6)
                placeRatingTextView.setText(rating + "");
            placeDistanceTextView.setText(distance);

            mapsImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("geo:" + placeLatitude + "," + placeLongitude + "?z=18"));
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}