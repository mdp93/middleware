package edu.umich.carlab.watchfon_intrusion_detection;

import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.loadable.Middleware;

import java.util.HashMap;
import java.util.Map;

public class MiddlewareImpl extends Middleware {
    public final static String APP = "watchfon_intrusion_detection";
    public final static String ATTACK = "attack_value";

    public final static String DETECTION = "detection";
    public final static String DETECTION_SENSOR = "detection_sensor";
    public final static String DETECTION_FLAG = "detection_flag";


    public final static Map<String, Float> DURATIONS = new HashMap<>();
    public final static Map<String, Float> MAGNITUDES = new HashMap<>();

    static {
        edu.umich.carlab.watchfon_estimates.MiddlewareImpl watchfon_estimates =
                new edu.umich.carlab.watchfon_estimates.MiddlewareImpl();

        // In seconds
        DURATIONS.put(watchfon_estimates.SPEED, 1.1f);
        DURATIONS.put(watchfon_estimates.STEERING, 1.1f);
        DURATIONS.put(watchfon_estimates.GEAR, 0.1f);
        DURATIONS.put(watchfon_estimates.ENGINERPM, 1.1f);
        DURATIONS.put(watchfon_estimates.ODOMETER, 1.1f);
        DURATIONS.put(watchfon_estimates.FUEL, 1.1f);


        // The units vary per attack
//        MAGNITUDES.put(watchfon_estimates.SPEED, 0.094f);
//        MAGNITUDES.put(watchfon_estimates.STEERING, 1.27f);
//        MAGNITUDES.put(watchfon_estimates.GEAR, 1.11f);
//        MAGNITUDES.put(watchfon_estimates.ENGINERPM, 76.5f);
//        MAGNITUDES.put(watchfon_estimates.ODOMETER, 0.06f);
//        MAGNITUDES.put(watchfon_estimates.FUEL, 0.036f);

        MAGNITUDES.put(watchfon_estimates.SPEED, 10f);
        MAGNITUDES.put(watchfon_estimates.STEERING, 200f);
        MAGNITUDES.put(watchfon_estimates.GEAR, 40f);
        MAGNITUDES.put(watchfon_estimates.ENGINERPM, 7600.5f);
        MAGNITUDES.put(watchfon_estimates.ODOMETER, 1000.06f);
        MAGNITUDES.put(watchfon_estimates.FUEL, 2500.036f);
    }

    @Override
    public String getName() {
        return APP;
    }

    @Override
    public Map<String, Float> splitValues(DataMarshal.DataObject dataObject) {
        if (dataObject.device.equals(APP) && dataObject.sensor.equals(DETECTION)) {
            Map<String, Float> splitMap = new HashMap<>();
            splitMap.put(DETECTION_SENSOR, dataObject.value[0]);
            splitMap.put(DETECTION_FLAG, dataObject.value[1]);
            return splitMap;
        }

        // Else
        return splitValues(dataObject);
    }
}
