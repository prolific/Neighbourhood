package com.fiktivo.neighbourhood.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.util.Objects;

public class NeighbourhoodSyncService extends Service {

    private static final Object syncAdapterLock = new Object();
    private static NeighbourhoodSyncAdapter neighbourhoodSyncAdapter = null;
    public static final String SYNC_FIINISHED = "com.fiktivo.neighbourhood.SYNC_FINISHED";

    @Override
    public void onCreate() {
        synchronized (syncAdapterLock) {
            if (neighbourhoodSyncAdapter == null)
                neighbourhoodSyncAdapter = new NeighbourhoodSyncAdapter(getApplicationContext(), true);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return neighbourhoodSyncAdapter.getSyncAdapterBinder();
    }
}
