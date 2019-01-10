package edu.umich.carlab.watchfon_gear;

import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.loadable.Middleware;

import java.util.HashMap;
import java.util.Map;

public class MiddlewareImpl extends Middleware {
    public final static String APP = "watchfon_gear";
    public final static String GEAR = "gear";

    @Override
    public String getName() {
        return APP;
    }
}
