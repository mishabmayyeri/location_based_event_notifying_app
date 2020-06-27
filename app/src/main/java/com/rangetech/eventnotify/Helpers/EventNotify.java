package com.rangetech.eventnotify.Helpers;

import android.app.Application;

import com.firebase.client.Firebase;

public class EventNotify extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Firebase.setAndroidContext(this);

    }
}
