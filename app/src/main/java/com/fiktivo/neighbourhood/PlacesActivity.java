package com.fiktivo.neighbourhood;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;


public class PlacesActivity extends ActionBarActivity implements PlacesFragment.Callback {

    boolean isTwoPane = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);
        if (findViewById(R.id.detail_fragment_container) != null) {
            isTwoPane = true;
            if (savedInstanceState == null)
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.detail_fragment_container, new DetailFragment())
                        .commit();
        } else
            isTwoPane = false;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_places, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.terms_privacy_policy) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://sites.google.com/site/neighbourhoodapp"));
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onItemSelected(String placeID) {
        if (isTwoPane) {
            Bundle args = new Bundle();
            args.putString("placeID", placeID);

            DetailFragment detailFragment = new DetailFragment();
            detailFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_fragment_container, detailFragment)
                    .commit();

        } else {
            Intent intent = new Intent(this, DetailActivity.class).putExtra("placeID", placeID);
            startActivity(intent);
        }
    }
}
