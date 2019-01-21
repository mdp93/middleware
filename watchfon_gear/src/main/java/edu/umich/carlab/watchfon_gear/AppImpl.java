package edu.umich.carlab.watchfon_gear;

import android.content.Context;
import android.hardware.SensorManager;
import android.util.Pair;
import edu.umich.carlab.CLDataProvider;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.loadable.App;
import edu.umich.carlab.sensors.PhoneSensors;
import edu.umich.carlabui.appbases.SensorListAppBase;

import java.util.Map;


public class AppImpl extends SensorListAppBase {
    final String TAG = "watchfon_gear";
    final edu.umich.carlab.world_aligned_imu.MiddlewareImpl world_aligned_imu = new edu.umich.carlab.world_aligned_imu.MiddlewareImpl();
    final edu.umich.carlab.watchfon_speed.MiddlewareImpl watchfon_speed = new edu.umich.carlab.watchfon_speed.MiddlewareImpl();


    public AppImpl(CLDataProvider cl, Context context) {
        super(cl, context);
        name = "watchfon_gear";
        subscribe(world_aligned_imu.APP, world_aligned_imu.ACCEL);
        subscribe(watchfon_speed.APP, watchfon_speed.SPEED);
    }


    @Override
    public void newData(DataMarshal.DataObject dObject) {
        super.newData(dObject);
        if (dObject.dataType != DataMarshal.MessageType.DATA) return;
        if (dObject.device.equals(MiddlewareImpl.APP)) return;
        if (dObject.value == null) return;

        // TODO. For now we just output a placeholder.
        outputData(MiddlewareImpl.APP, dObject, MiddlewareImpl.GEAR, 1f);
    }
}
