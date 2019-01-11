package edu.umich.carlab.watchfon_odometer;

import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.loadable.Middleware;

import java.util.HashMap;
import java.util.Map;

public class MiddlewareImpl extends Middleware {
    public final static String APP = "watchfon_odometer";
    public final static String DISTANCE = "distance"; // in meters

    @Override
    public String getName() {
        return APP;
    }
}
