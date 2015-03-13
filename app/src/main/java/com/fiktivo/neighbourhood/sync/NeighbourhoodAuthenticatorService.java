package com.fiktivo.neighbourhood.sync;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class NeighbourhoodAuthenticatorService extends Service {

    private NeighbourhoodAuthenticator authenticator;

    @Override
    public void onCreate() {
        authenticator = new NeighbourhoodAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
