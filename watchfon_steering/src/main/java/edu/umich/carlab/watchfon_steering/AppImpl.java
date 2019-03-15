package edu.umich.carlab.watchfon_steering;

import android.content.Context;
import edu.umich.carlab.CLDataProvider;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlabui.appbases.SensorListAppBase;

import java.util.Map;


/**
 * Steering wheel estimation. It uses vehicle properties, and
 * the estimated speed.
 */
public class AppImpl extends SensorListAppBase {
    final String TAG = "WatchfonSteering";
    final double INCHES_TO_METERS = 0.0254;
    final edu.umich.carlab.world_aligned_imu.MiddlewareImpl world_aligned_imu = new edu.umich.carlab.world_aligned_imu.MiddlewareImpl();
    final edu.umich.carlab.watchfon_speed.MiddlewareImpl watchfon_speed = new edu.umich.carlab.watchfon_speed.MiddlewareImpl();
    final MiddlewareImpl middleware = new MiddlewareImpl();
    // Parameters
    float STEERING_RATIO = 14.8f;
    float VEHICLE_LENGTH = (float) (193.9 * INCHES_TO_METERS); // Finally in meters
    Float lastSpeed = null;
    Float lastYaw = null;

    public AppImpl(CLDataProvider cl, Context context) {
        super(cl, context);
        name = "watchfon_steering";
        middlewareName = MiddlewareImpl.APP;

        if (context != null) {
            STEERING_RATIO = middleware.getParameterOrDefault(context, MiddlewareImpl.STEERING_RATIO, STEERING_RATIO);
            VEHICLE_LENGTH = middleware.getParameterOrDefault(context, MiddlewareImpl.VEHICLE_LENGTH, VEHICLE_LENGTH);
        }

        subscribe(watchfon_speed.APP, watchfon_speed.SPEED);
        subscribe(world_aligned_imu.APP, world_aligned_imu.GYRO);
    }

    @Override
    public void newData(DataMarshal.DataObject dObject) {
        super.newData(dObject);

        if (dObject.dataType != DataMarshal.MessageType.DATA) return;
        if (dObject.device.equals(MiddlewareImpl.APP)) return;
        if (dObject.value == null) return;

        startClock();
        if (dObject.device.equals(edu.umich.carlab.watchfon_speed.MiddlewareImpl.APP))
            lastSpeed = dObject.value[0];

        if (dObject.device.equals(edu.umich.carlab.world_aligned_imu.MiddlewareImpl.APP)) {
            Map<String, Float> gyroParts = world_aligned_imu.splitValues(dObject);
            lastYaw = gyroParts.get(edu.umich.carlab.world_aligned_imu.MiddlewareImpl.GYRO_Y);
        }

        if (lastSpeed != null && lastYaw != null) {
            Double steering = (double) (lastSpeed / lastYaw);
            steering = Math.asin(VEHICLE_LENGTH / steering);
            steering = STEERING_RATIO * steering;
            steering *= 180 / Math.PI;
            outputData(
                    MiddlewareImpl.APP,
                    dObject,
                    MiddlewareImpl.STEERING,
                    steering.floatValue()
            );
            endClock();
        }

    }
}
