package edu.umich.carlab.watchfon_rpm;

import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.loadable.Middleware;

import java.util.HashMap;
import java.util.Map;

public class MiddlewareImpl extends Middleware {
    public final static String APP = "watchfon_rpm";
    public final static String RPM = "rpm";

    @Override
    public String getName() {
        return APP;
    }
}
