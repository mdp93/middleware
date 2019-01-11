package edu.umich.carlab.watchfon_estimates;

import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.loadable.Middleware;

import java.util.HashMap;
import java.util.Map;

public class MiddlewareImpl extends Middleware {
    public final static String APP = "watchfon_estimates";
    public final static String SPEED = "speed";
    public final static String STEERING = "steering";
    public final static String ENGINERPM = "engine_rpm"; // rpm
    public final static String ODOMETER = "odometer"; // distance
    public final static String FUEL = "fuel";
    public final static String GEAR = "gear";

    @Override
    public String getName() {
        return APP;
    }
}
