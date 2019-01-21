package edu.umich.carlab.watchfon_steering;

import android.content.Context;
import android.util.Pair;
import edu.umich.carlab.CLDataProvider;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.loadable.App;
import edu.umich.carlabui.appbases.SensorListAppBase;

import java.util.Map;


/**
 * Steering wheel estimation. It uses vehicle properties, and
 * the estimated speed.
 */
public class AppImpl extends SensorListAppBase {
    final String TAG = "WatchfonSteering";
    final double STEERING_RATIO = 14.8;
    final double INCHES_TO_METERS = 0.0254;
    final double VEHICLE_LENGTH = 193.9 * INCHES_TO_METERS; // Finally in meters

    Float lastSpeed = null;
    Float lastYaw = null;

    final edu.umich.carlab.world_aligned_imu.MiddlewareImpl world_aligned_imu =  new edu.umich.carlab.world_aligned_imu.MiddlewareImpl();
    final edu.umich.carlab.watchfon_speed.MiddlewareImpl watchfon_speed = new edu.umich.carlab.watchfon_speed.MiddlewareImpl();

    public AppImpl(CLDataProvider cl, Context context) {
        super(cl, context);
        name = "WatchFon/Steering";
        subscribe(watchfon_speed.APP, watchfon_speed.SPEED);
        subscribe(world_aligned_imu.APP, world_aligned_imu.GYRO);
    }

    @Override
    public void newData(DataMarshal.DataObject dObject) {
        super.newData(dObject);

        if (dObject.dataType != DataMarshal.MessageType.DATA) return;
        if (dObject.device.equals(MiddlewareImpl.APP)) return;
        if (dObject.value == null) return;

        if (dObject.device.equals(watchfon_speed.APP))
            lastSpeed = dObject.value[0];

        if (dObject.device.equals(world_aligned_imu.APP)) {
            Map<String, Float> gyroParts = world_aligned_imu.splitValues(dObject);
            lastYaw = gyroParts.get(world_aligned_imu.GYRO_Y);
        }

        if (lastSpeed != null && lastYaw != null) {
            Double steering = (double)(lastSpeed / lastYaw);
            steering = Math.asin(VEHICLE_LENGTH / steering);
            steering = STEERING_RATIO * steering;
            steering *= 180 / Math.PI;
            outputData(
                MiddlewareImpl.APP,
                dObject,
                MiddlewareImpl.STEERING,
                steering.floatValue()
            );
        }

    }
}
