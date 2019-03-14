package edu.umich.carlab.watchfon_speed;

import android.content.Context;
import edu.umich.carlab.CLDataProvider;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.sensors.PhoneSensors;
import edu.umich.carlabui.appbases.SensorListAppBase;

import java.util.Map;


/**
 * Speed estimation for WatchFon. This app uses GPS sensors and reads out
 * the speed. The next version of this app will use a complementary filter,
 * vehicle-aligned IMU, and GPS to estimate the speed.
 */
public class AppImpl extends SensorListAppBase {
    final String TAG = "WatchfonSpeed";

    final float MPS_TO_KMPH = 1 / 0.621371f;

    public AppImpl(CLDataProvider cl, Context context) {
        super(cl, context);
        name = "watchfon_speed";
        middlewareName = MiddlewareImpl.APP;
        subscribe(PhoneSensors.DEVICE, PhoneSensors.GPS);
    }


    @Override
    public void newData(DataMarshal.DataObject dObject) {
        super.newData(dObject);

        if (dObject.dataType != DataMarshal.MessageType.DATA) return;
        if (dObject.device.equals(MiddlewareImpl.APP)) return;
        if (dObject.value == null) return;

        Map<String, Float> gpsSplit = PhoneSensors.splitValues(dObject);
        Float speed = gpsSplit.get(PhoneSensors.GPS_SPEED) * MPS_TO_KMPH;
        outputData(MiddlewareImpl.APP, dObject, MiddlewareImpl.SPEED, speed);
    }
}
