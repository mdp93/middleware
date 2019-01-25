package edu.umich.carlab.watchfon_speed;

import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.loadable.Middleware;

import java.util.HashMap;
import java.util.Map;

public class MiddlewareImpl extends Middleware {
    public final static String APP = "watchfon_speed";
    public final static String SPEED = "speed"; // in kmph

    @Override
    public String getName() {
        return APP;
    }
}
