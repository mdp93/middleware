package edu.umich.carlab.watchfon_steering;

import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.loadable.Middleware;

import java.util.*;

public class MiddlewareImpl extends Middleware {
    public final static String APP = "watchfon_steering";
    public final static String STEERING = "steering";

    public final static String STEERING_RATIO = "steering_ratio";
    public final static String VEHICLE_LENGTH = "vehicle_length";
    public static List<String> configurableParameters = new ArrayList<>(Arrays.asList(
            STEERING_RATIO,
            VEHICLE_LENGTH
    ));


    @Override
    public String getName() {
        return APP;
    }
}
