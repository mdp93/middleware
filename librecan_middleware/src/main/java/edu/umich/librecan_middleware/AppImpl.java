package edu.umich.librecan_middleware;

import android.content.Context;
import android.util.Pair;
import edu.umich.carlab.CLDataProvider;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.loadable.App;
import edu.umich.carlab.sensors.OpenXcSensors;
import edu.umich.carlabui.appbases.SensorListAppBase;

import java.util.Map;


/**
 * Librecan
 */
public class AppImpl extends SensorListAppBase {
    final String TAG = "librecan_middleware";

    final edu.umich.carlab.world_aligned_imu.MiddlewareImpl world_aligned_imu =  new edu.umich.carlab.world_aligned_imu.MiddlewareImpl();

    public AppImpl(CLDataProvider cl, Context context) {
        super(cl, context);
        name = "librecan_middleware";
        middlewareName = MiddlewareImpl.APP;

        subscribe(world_aligned_imu.APP, world_aligned_imu.GYRO);
        subscribe(OpenXcSensors.DEVICE, OpenXcSensors.SPEED);
        subscribe(OpenXcSensors.DEVICE, OpenXcSensors.STEERING);
    }
}
