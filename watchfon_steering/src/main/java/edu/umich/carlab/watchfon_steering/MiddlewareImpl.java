package edu.umich.carlab.watchfon_steering;

import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.loadable.Middleware;

import java.util.HashMap;
import java.util.Map;

public class MiddlewareImpl extends Middleware {
    public final static String APP = "watchfon_steering";
    public final static String STEERING = "steering";

    @Override
    public String getName() {
        return APP;
    }
}
