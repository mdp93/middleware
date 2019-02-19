package edu.umich.carlab.watchfon_steering;

import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.loadable.Middleware;

import java.util.*;

public class MiddlewareImpl extends Middleware {
    public final static String APP = "watchfon_steering";
    public final static String STEERING = "steering";

    public final static String STEERING_RATIO = "steering_ratio";
    public final static String VEHICLE_LENGTH = "vehicle_length";

    @Override
    public String getName() {
        return APP;
    }
}
