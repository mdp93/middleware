package edu.umich.carlab.pothole_detection;

import android.content.Context;
import android.hardware.SensorManager;
import android.util.Pair;
import edu.umich.carlab.CLDataProvider;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.loadable.App;
import edu.umich.carlab.sensors.PhoneSensors;

import java.util.Map;


public class AppImpl extends App {
    final String TAG = "pothole_detection";

    public AppImpl(CLDataProvider cl, Context context) {
        super(cl, context);
        name = "pothole_detection";

        subscribe(PhoneSensors.DEVICE, PhoneSensors.GPS);
        subscribe(OBD.DEVICE, OBD.RPM);
        subscribe(OBD.DEVICE, OBD.RAW_CAN);
    }


    @Override
    public void newData(DataMarshal.DataObject dObject) {
        super.newData(dObject);
        if (!isValidData(dObject)) return;
        if (dObject.device.equals(MiddlewareImpl.APP)) return;

        DataMarshal.DataObject  latestValue = getLatestData(PhoneSensors.DEVICE, PhoneSensors.GPS);
        if (latestValue != null) {
          Map<String, Float> gpsSplit = PhoneSensors.splitValues(latestValue);
          Float speed = gpsSplit.get(PhoneSensors.GPS_SPEED);
          outputData(MiddlewareImpl.APP, dObject, MiddlewareImpl.SPEED, speed);
        }
    }
}
