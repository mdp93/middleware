package edu.umich.carlab.watchfon_spoofed_sensors;

import android.content.Context;
import android.hardware.SensorManager;
import android.util.Pair;
import edu.umich.carlab.CLDataProvider;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.loadable.App;
import edu.umich.carlab.sensors.OpenXcSensors;
import edu.umich.carlab.sensors.PhoneSensors;

import java.util.Map;

/**
 * The watchfon spoof middleware takes the OpenXC sensors as input, and injects false noise based on a triggering
 * condition. The output of this middleware is the injected data and a mask to specify the magnitude of the injection.
 * This is used in the WatchFon project to detect injection into the CAN bus by comparing with the smartphone-derived
 * sensors.
 *
 * The nature of the injection is controlled using this middleware's settings.
 */

public class AppImpl extends App {
    final String TAG = "watchfon_spoofed_sensors";

    Double injectionMagnitude = 10d, newValue;

    public AppImpl(CLDataProvider cl, Context context) {
        super(cl, context);
        name = "watchfon_spoofed_sensors";
        sensors.add(new Pair<>(OpenXcSensors.DEVICE, OpenXcSensors.SPEED));
        sensors.add(new Pair<>(OpenXcSensors.DEVICE, OpenXcSensors.STEERING));
        sensors.add(new Pair<>(OpenXcSensors.DEVICE, OpenXcSensors.FUEL));
        sensors.add(new Pair<>(OpenXcSensors.DEVICE, OpenXcSensors.ODOMETER));
        sensors.add(new Pair<>(OpenXcSensors.DEVICE, OpenXcSensors.ENGINERPM));
        sensors.add(new Pair<>(OpenXcSensors.DEVICE, OpenXcSensors.GEAR));
    }


    @Override
    public void newData(DataMarshal.DataObject dObject) {
        if (dObject.dataType != DataMarshal.MessageType.DATA) return;
        if (dObject.device.equals(MiddlewareImpl.APP)) return;
        if (dObject.value == null) return;

        newValue = dObject.value[0] + injectionMagnitude;
        outputData(MiddlewareImpl.APP, dObject, dObject.sensor, new Float[]{
                newValue.floatValue(),
                injectionMagnitude.floatValue(),
            }
        );
    }
}