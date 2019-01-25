package edu.umich.carlab.watchfon_odometer;

import android.content.Context;
import android.hardware.SensorManager;
import android.location.Location;
import android.util.Pair;
import edu.umich.carlab.CLDataProvider;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.loadable.App;
import edu.umich.carlab.sensors.PhoneSensors;
import edu.umich.carlabui.appbases.SensorListAppBase;

import java.util.Map;


public class AppImpl extends SensorListAppBase {
    final String TAG = "watchfon_odometer";
    final String DISTANCE_KEY = "distance";

    Map<String, Float> lastValues = null;
    Location lastLoc, currLoc;

    // Distance in meters
    Double distance = 0d;

    public AppImpl(CLDataProvider cl, Context context) {
        super(cl, context);
        lastLoc = new Location("");
        currLoc = new Location("");

        name = "watchfon_odometer";
        subscribe(PhoneSensors.DEVICE, PhoneSensors.GPS);

        if (context != null)
            distance = loadValue(DISTANCE_KEY, 0d);
    }


    @Override
    public void newData(DataMarshal.DataObject dObject) {
        super.newData(dObject);

        if (dObject.dataType != DataMarshal.MessageType.DATA) return;
        if (dObject.device.equals(MiddlewareImpl.APP)) return;
        if (dObject.value == null) return;

        Map<String, Float> values = PhoneSensors.splitValues(dObject);
        if (lastValues != null) {
            lastLoc.setLatitude(lastValues.get(PhoneSensors.GPS_LATITUDE));
            lastLoc.setLongitude(lastValues.get(PhoneSensors.GPS_LONGITUDE));
            currLoc.setLatitude(values.get(PhoneSensors.GPS_LATITUDE));
            currLoc.setLongitude(values.get(PhoneSensors.GPS_LONGITUDE));
            distance += currLoc.distanceTo(lastLoc);
            outputData(
                    MiddlewareImpl.APP,
                    dObject,
                    MiddlewareImpl.DISTANCE,
                    distance.floatValue()
            );
        }
        lastValues = values;
    }

    @Override
    public void shutdown() {
        super.shutdown();
        saveValue(DISTANCE_KEY, distance);
    }
}
