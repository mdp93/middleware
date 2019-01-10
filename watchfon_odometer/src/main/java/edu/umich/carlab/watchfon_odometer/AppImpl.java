package edu.umich.carlab.watchfon_odometer;

import android.content.Context;
import android.hardware.SensorManager;
import android.location.Location;
import android.util.Pair;
import edu.umich.carlab.CLDataProvider;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.loadable.App;
import edu.umich.carlab.sensors.PhoneSensors;

import java.util.Map;


public class AppImpl extends App {
    final String TAG = "watchfon_odometer";
    Map<String, Float> lastValues = null;
    Location lastLoc, currLoc;

    public AppImpl(CLDataProvider cl, Context context) {
        super(cl, context);
        lastLoc = new Location("");
        currLoc = new Location("");

        name = "watchfon_odometer";
        sensors.add(new Pair<>(PhoneSensors.DEVICE, PhoneSensors.GPS));
    }


    @Override
    public void newData(DataMarshal.DataObject dObject) {
        if (dObject.dataType != DataMarshal.MessageType.DATA) return;
        if (dObject.device.equals(MiddlewareImpl.APP)) return;
        if (dObject.value == null) return;

        Map<String, Float> values = PhoneSensors.splitValues(dObject);
        if (lastValues != null) {
            lastLoc.setLatitude(values.get(PhoneSensors.GPS_LATITUDE));
            lastLoc.setLongitude(values.get(PhoneSensors.GPS_LONGITUDE));
            currLoc.setLatitude(values.get(PhoneSensors.GPS_LATITUDE));
            currLoc.setLongitude(values.get(PhoneSensors.GPS_LONGITUDE));
            outputData(
                    MiddlewareImpl.APP,
                    dObject,
                    MiddlewareImpl.DISTANCE,
                    currLoc.distanceTo(lastLoc)
            );
        }
        lastValues = values;

    }
}
