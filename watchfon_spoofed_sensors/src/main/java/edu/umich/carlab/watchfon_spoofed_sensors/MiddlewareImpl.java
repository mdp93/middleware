package edu.umich.carlab.watchfon_spoofed_sensors;

import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.loadable.Middleware;

import java.util.HashMap;
import java.util.Map;

public class MiddlewareImpl extends Middleware {
    public final static String APP = "watchfon_spoofed_sensors";
    public final static String SPEED = "speed";
    public final static String STEERING = "steering";
    public final static String ENGINERPM = "engine_rpm";
    public final static String ODOMETER = "odometer";
    public final static String FUEL = "fuel";
    public final static String GEAR = "gear";

    public final static String INJECTION_MAGNITUDE = "injection";

    @Override
    public String getName() {
        return APP;
    }

    @Override
    public Map<String, Float> splitValues(DataMarshal.DataObject dataObject) {
        Map<String, Float> splitMap = new HashMap<>();
        splitMap.put(dataObject.sensor, dataObject.value[0]);
        splitMap.put(INJECTION_MAGNITUDE, dataObject.value[1]);
        return splitMap;
    }
}




