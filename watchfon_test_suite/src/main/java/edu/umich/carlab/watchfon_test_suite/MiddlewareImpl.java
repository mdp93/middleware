package edu.umich.carlab.watchfon_test_suite;

import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.loadable.Middleware;

import java.util.HashMap;
import java.util.Map;

public class MiddlewareImpl extends Middleware {
    public final static String APP = "watchfon_test_suite";
    public final static String SPEED = "speed";

    @Override
    public String getName() {
        return APP;
    }
}
