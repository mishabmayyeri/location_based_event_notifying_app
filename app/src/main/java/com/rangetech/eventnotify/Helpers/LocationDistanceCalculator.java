package com.rangetech.eventnotify.Helpers;

import android.location.Location;

public class LocationDistanceCalculator {

    public double getDistance(
            double startLat,double startLong,
            double endLat,double endLong
    ){
        Location startPoint=new Location("locationA");
        startPoint.setLatitude(startLat);
        startPoint.setLongitude(startLong);

        Location endPoint=new Location("locationB");
        endPoint.setLatitude(endLat);
        endPoint.setLongitude(endLong);

        return startPoint.distanceTo(endPoint);
    }
}
