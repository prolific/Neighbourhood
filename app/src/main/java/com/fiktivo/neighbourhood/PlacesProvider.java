package com.fiktivo.neighbourhood;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.fiktivo.neighbourhood.PlacesContract.PlacesEntry;

public class PlacesProvider extends ContentProvider {

    final static int PLACES = 100;
    final static int PLACES_WITH_CATEGORY = 101;
    private static final UriMatcher uriMatcher = buildUriMatcher();
    private PlacesDBHelper placesDBHelper;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = PlacesContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, PlacesContract.PATH_PLACES, PLACES);
        matcher.addURI(authority, PlacesContract.PATH_PLACES + "/*", PLACES_WITH_CATEGORY);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        placesDBHelper = new PlacesDBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        switch (uriMatcher.match(uri)) {
            case PLACES_WITH_CATEGORY:
                cursor = placesDBHelper.getReadableDatabase().query(
                        PlacesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case PLACES:
                cursor = placesDBHelper.getReadableDatabase().query(
                        PlacesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = placesDBHelper.getWritableDatabase();
        switch (uriMatcher.match(uri)) {
            case PLACES:
                long _id = db.insert(PlacesEntry.TABLE_NAME, null, values);
                break;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        db.close();
        return null;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        SQLiteDatabase db = placesDBHelper.getWritableDatabase();
        int retCount = 0;
        switch (uriMatcher.match(uri)) {
            case PLACES:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(PlacesEntry.TABLE_NAME, null, value);
                        if (_id != -1)
                            retCount++;
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return retCount;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = placesDBHelper.getWritableDatabase();
        int rowsDeleted = 0;
        if (null == selection)
            selection = "1";
        switch (uriMatcher.match(uri)) {
            case PLACES:
                rowsDeleted = db.delete(PlacesEntry.TABLE_NAME, selection, selectionArgs);
                break;
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
