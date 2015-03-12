package com.fiktivo.neighbourhood;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public class PlacesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private PlacesAdapter placesAdapter;
    public static final int PLACES_LOADER = 0;
    private static String category = "restaurant";
    private static final String FILENAME_PREF = "com.fiktivo.neighbourhood.preferences";
    private int selectedPosition = ListView.INVALID_POSITION;
    private ListView placesListView;

    public interface Callback {
        public void onItemSelected(String placeID);
    }

    public PlacesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_places, container, false);
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra("type")) {
            category = intent.getStringExtra("type");
            getActivity().setTitle(category);
        }
        Uri placesWithCategoryUri = PlacesContract.PlacesEntry.buildPlacesCategory(category);
        Cursor cursor = getActivity().getContentResolver().query(placesWithCategoryUri, null, null, null, null);
        placesAdapter = new PlacesAdapter(getActivity(), cursor, 0);
        placesListView = (ListView) rootView.findViewById(R.id.places_listview);
        placesListView.setAdapter(placesAdapter);
        placesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (cursor != null) {
                    int index = cursor.getColumnIndex(PlacesContract.PlacesEntry.COLUMN_NAME_Place_ID);
                    String placeID = cursor.getString(index);
                    ((Callback) getActivity()).onItemSelected(placeID);
                }
                selectedPosition = position;
            }
        });
        if (savedInstanceState != null && savedInstanceState.containsKey("selectedPosition"))
            selectedPosition = savedInstanceState.getInt("selectedPosition");
        Log.v("Places", "ListView Created");
        if (isNetworkAvailable()) {
            Log.v("Places", "Network Found");
            getCurrentLocationAndUpdatePlaces();
        } else
            Toast.makeText(getActivity(), "Please check your Internet Connection", Toast.LENGTH_LONG).show();
        return rootView;
    }

    public boolean isNetworkAvailable() {
        Log.v("Places", "network check");
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void updatePlaces(Location currentLocation) {
        Log.v("Location", "Location Found");
        String location = currentLocation.getLatitude() + "," + currentLocation.getLongitude();
        Log.v("Location", location);
        FetchPlacesTask fetchPlacesTask = new FetchPlacesTask(getActivity());
        fetchPlacesTask.execute(category, location);
    }

    public void getCurrentLocationAndUpdatePlaces() {
        final LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.v("Places", "location found");
                if (location != null) {
                    Log.v("Places", "location not null");
                    SharedPreferences sharedPref = getActivity().getSharedPreferences(FILENAME_PREF, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("latitude", location.getLatitude() + "");
                    editor.putString("longitude", location.getLongitude() + "");
                    editor.commit();
                    updatePlaces(location);
                    locationManager.removeUpdates(this);
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(PLACES_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (selectedPosition != ListView.INVALID_POSITION)
            outState.putInt("selectedPosition", selectedPosition);
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Uri placesWithCategoryUri = PlacesContract.PlacesEntry.buildPlacesCategory(category);
        return new CursorLoader(getActivity(), placesWithCategoryUri, null,
                PlacesContract.PlacesEntry.COLUMN_NAME_Category + "=?", new String[]{category}, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        placesAdapter.swapCursor(cursor);
        if (selectedPosition != ListView.INVALID_POSITION)
            placesListView.setSelection(selectedPosition);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        placesAdapter.swapCursor(null);
    }
}