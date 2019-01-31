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

    public final static Map<String, Float> ONE_HOT_SENSORS = new HashMap<>();
    public final static Map<Float, String> ONE_HOT_REVERSE = new HashMap<>();

    static {
        ONE_HOT_SENSORS.put(SPEED, 1.0f);
        ONE_HOT_SENSORS.put(STEERING, 2.0f);
        ONE_HOT_SENSORS.put(GEAR, 3.0f);
        ONE_HOT_SENSORS.put(ENGINERPM, 4.0f);
        ONE_HOT_SENSORS.put(ODOMETER, 5.0f);
        ONE_HOT_SENSORS.put(FUEL, 6.0f);

        ONE_HOT_REVERSE.put(1.0f, SPEED);
        ONE_HOT_REVERSE.put(2.0f, STEERING);
        ONE_HOT_REVERSE.put(3.0f, GEAR);
        ONE_HOT_REVERSE.put(4.0f, ENGINERPM);
        ONE_HOT_REVERSE.put(5.0f, ODOMETER);
        ONE_HOT_REVERSE.put(6.0f, FUEL);
    }

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




