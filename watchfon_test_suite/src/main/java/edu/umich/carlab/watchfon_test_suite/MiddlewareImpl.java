package edu.umich.carlab.watchfon_test_suite;

import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.loadable.Middleware;

import java.util.HashMap;
import java.util.Map;

public class MiddlewareImpl extends Middleware {
    public final static String APP = "watchfon_test_suite";

    public final static String ATTACK = "attack";
    public final static String ATTACK_SENSOR = "attack_sensor";
    public final static String ATTACK_VALUE = "attack_value";
    public final static String ATTACK_TYPE = "attack_type";

    @Override
    public Map<String, Float> splitValues(DataMarshal.DataObject dataObject) {
        if (dataObject.device.equals(APP) && dataObject.sensor.equals(ATTACK)) {
            Map<String, Float> splitMap = new HashMap<>();
            splitMap.put(ATTACK_SENSOR, dataObject.value[0]);
            splitMap.put(ATTACK_VALUE, dataObject.value[1]);
            splitMap.put(ATTACK_TYPE, dataObject.value[2]);
            return splitMap;
        }

        // Else
        return splitValues(dataObject);
    }

    @Override
    public String getName() {
        return APP;
    }
}
