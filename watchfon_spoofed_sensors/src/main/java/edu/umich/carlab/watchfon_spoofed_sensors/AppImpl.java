package edu.umich.carlab.watchfon_spoofed_sensors;

import android.content.Context;
import edu.umich.carlab.CLDataProvider;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.sensors.OpenXcSensors;
import edu.umich.carlabui.appbases.SensorListAppBase;

import java.util.HashMap;
import java.util.Map;

import static edu.umich.carlab.watchfon_spoofed_sensors.MiddlewareImpl.*;

/**
 * The watchfon spoof middleware takes the OpenXC sensors as input, and injects false noise based on a triggering
 * condition. The output of this middleware is the injected data and a mask to specify the magnitude of the injection.
 * This is used in the WatchFon project to detect injection into the CAN bus by comparing with the smartphone-derived
 * sensors.
 * <p>
 * The nature of the injection is controlled using this middleware's settings.
 */

public class AppImpl extends SensorListAppBase {
    final String TAG = "watchfon_spoofed_sensors";
    // From watchfon_intrusion_detection, soon to be deprecated
    final String ID_ATTACK = "attack_value";
    // From watchfon_test_suite
    final String TEST_SUITE = "watchfon_test_suite";
    final String TS_ATTACK = "attack";
    final String TS_ATTACK_SENSOR = "attack_sensor";
    final String TS_ATTACK_TYPE = "attack_type";
    Double injectionMagnitude = 0d, newValue;
    Map<String, Float> injectionMagnitudes;
    Map<String, Attack.Type> injectionTypes;
    String[] allSensors = {
            SPEED,
            STEERING,
            FUEL,
            ODOMETER,
            GEAR,
            ENGINERPM,
    };

    public AppImpl(CLDataProvider cl, Context context) {
        super(cl, context);
        name = "watchfon_spoofed_sensors";
        middlewareName = MiddlewareImpl.APP;

        injectionMagnitudes = new HashMap<>();
        injectionTypes = new HashMap<>();
        for (String sensor : allSensors) {
            injectionTypes.put(sensor, Attack.Type.DELTA);
            injectionMagnitudes.put(sensor, 0f);
        }

        subscribe(OpenXcSensors.DEVICE, OpenXcSensors.SPEED);
        subscribe(OpenXcSensors.DEVICE, OpenXcSensors.STEERING);
        subscribe(OpenXcSensors.DEVICE, OpenXcSensors.FUEL);
        subscribe(OpenXcSensors.DEVICE, OpenXcSensors.ODOMETER);
        subscribe(OpenXcSensors.DEVICE, OpenXcSensors.ENGINERPM);
        subscribe(OpenXcSensors.DEVICE, OpenXcSensors.GEAR);

        subscribe(TEST_SUITE, TS_ATTACK);
    }


    @Override
    public void newData(DataMarshal.DataObject dObject) {
        super.newData(dObject);

        String dev = dObject.device;
        String sen = dObject.sensor;

        if (dObject.dataType != DataMarshal.MessageType.DATA) return;
        if (dev.equals(MiddlewareImpl.APP)) return;
        if (dObject.value == null) return;

        if (dev.equals(TEST_SUITE) && sen.equals(TS_ATTACK)) {
            String sensor = ONE_HOT_REVERSE.get(dObject.value[0]);
            injectionMagnitudes.put(sensor, dObject.value[1]);
            injectionTypes.put(
                    sensor,
                    Attack.Type.values()[
                            dObject.value[2].intValue()]);
        } else if (dev.equals(OpenXcSensors.DEVICE)) {
            DataMarshal.DataObject outputDObject;
            String sensor = dObject.sensor;

            newValue = (injectionTypes.get(sensor) == Attack.Type.DELTA)
                    ? dObject.value[0] + injectionMagnitudes.get(sensor)
                    : injectionMagnitudes.get(sensor).doubleValue();
            outputDObject = outputData(
                    MiddlewareImpl.APP,
                    dObject,
                    sensor,
                    new Float[]{
                            newValue.floatValue(),
                            (float) (newValue - dObject.value[0]),
                    }
            );

            super.newData(outputDObject);
        }

    }
}
