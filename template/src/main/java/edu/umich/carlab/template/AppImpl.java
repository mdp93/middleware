package edu.umich.carlab.template;

import android.content.Context;
import android.hardware.SensorManager;
import android.util.Pair;
import edu.umich.carlab.CLDataProvider;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.loadable.App;
import edu.umich.carlab.sensors.PhoneSensors;

import java.util.Map;


public class AppImpl extends App {
    final String TAG = "template";

    public AppImpl(CLDataProvider cl, Context context) {
        super(cl, context);
        name = "template";
        
        subscribe(PhoneSensors.DEVICE, PhoneSensors.GPS);
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
