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


    public final static Map<String, Float> ONE_HOT_SENSORS = new HashMap<>();
    public final static Map<String, Float> DURATIONS = new HashMap<>();
    public final static Map<String, Float> MAGNITUDES = new HashMap<>();

    static {
        edu.umich.carlab.watchfon_estimates.MiddlewareImpl watchfon_estimates =
                new edu.umich.carlab.watchfon_estimates.MiddlewareImpl();
        ONE_HOT_SENSORS.put(watchfon_estimates.SPEED, 1.0f);
        ONE_HOT_SENSORS.put(watchfon_estimates.STEERING, 2.0f);
        ONE_HOT_SENSORS.put(watchfon_estimates.GEAR, 3.0f);
        ONE_HOT_SENSORS.put(watchfon_estimates.ENGINERPM, 4.0f);
        ONE_HOT_SENSORS.put(watchfon_estimates.ODOMETER, 5.0f);
        ONE_HOT_SENSORS.put(watchfon_estimates.FUEL, 6.0f);



        DURATIONS.put(watchfon_estimates.SPEED, 10f);
        DURATIONS.put(watchfon_estimates.STEERING, 10f);
        DURATIONS.put(watchfon_estimates.GEAR, 10f);
        DURATIONS.put(watchfon_estimates.ENGINERPM, 10f);
        DURATIONS.put(watchfon_estimates.ODOMETER, 10f);
        DURATIONS.put(watchfon_estimates.FUEL, 10f);


        MAGNITUDES.put(watchfon_estimates.SPEED, 5f);
        MAGNITUDES.put(watchfon_estimates.STEERING, 5f);
        MAGNITUDES.put(watchfon_estimates.GEAR, 5f);
        MAGNITUDES.put(watchfon_estimates.ENGINERPM, 5f);
        MAGNITUDES.put(watchfon_estimates.ODOMETER, 5f);
        MAGNITUDES.put(watchfon_estimates.FUEL, 5f);
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
