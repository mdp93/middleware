package edu.umich.carlab.watchfon_rpm;

import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.loadable.Middleware;

import java.util.HashMap;
import java.util.Map;

public class MiddlewareImpl extends Middleware {
    public final static String APP = "watchfon_rpm";
    public final static String RPM = "rpm";

    public final static String FINAL_DRIVE_RATIO = "final_drive_ratio";
    public final static String TIRE_CIRCUMFERENCE = "tire_circumference";

    @Override
    public String getName() {
        return APP;
    }
}
