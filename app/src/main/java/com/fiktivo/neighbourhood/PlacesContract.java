package com.fiktivo.neighbourhood;

import android.net.Uri;
import android.provider.BaseColumns;

public class PlacesContract {

    public static final String CONTENT_AUTHORITY = "com.fiktivo.neighbourhood";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_PLACES = "places";

    public PlacesContract() {
    }

    public static final class PlacesEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLACES).build();

        public static final String TABLE_NAME = "places";
        public static final String COLUMN_NAME_Category = "category";
        public static final String COLUMN_NAME_Place_ID = "placeid";
        public static final String COLUMN_NAME_Place_Name = "placename";
        public static final String COLUMN_NAME_Place_Rating = "placerating";
        public static final String COLUMN_NAME_Place_Latitude = "placelatitude";
        public static final String COLUMN_NAME_Place_Longitude = "placelongitude";

        public static Uri buildPlacesCategory(String category) {
            return CONTENT_URI.buildUpon().appendPath(category).build();
        }
    }
}
