package edu.umich.carlab.watchfon_intrusion_detection;

import android.content.Context;
import edu.umich.carlab.CLDataProvider;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.loadable.App;

import java.util.Map;


public class AppImpl extends App {
    final String TAG = "watchfon_intrusion_detection";

    // Sensors estimated by WatchFon
    final edu.umich.carlab.watchfon_estimates.MiddlewareImpl watchfon_estimates = new edu.umich.carlab.watchfon_estimates.MiddlewareImpl();

    // Sensors from the vehicle (with optional injection for intrusion detection evaluation)
    final edu.umich.carlab.watchfon_spoofed_sensors.MiddlewareImpl watchfon_spoofed_sensors = new edu.umich.carlab.watchfon_spoofed_sensors.MiddlewareImpl();


    public AppImpl(CLDataProvider cl, Context context) {
        super(cl, context);
        name = "watchfon_intrusion_detection";

        subscribe(watchfon_estimates.APP, watchfon_estimates.SPEED);
        subscribe(watchfon_estimates.APP, watchfon_estimates.STEERING);
        subscribe(watchfon_estimates.APP, watchfon_estimates.FUEL);
        subscribe(watchfon_estimates.APP, watchfon_estimates.ENGINERPM);
        subscribe(watchfon_estimates.APP, watchfon_estimates.ODOMETER);
        subscribe(watchfon_estimates.APP, watchfon_estimates.GEAR);

        subscribe(watchfon_spoofed_sensors.APP, watchfon_spoofed_sensors.SPEED);
        subscribe(watchfon_spoofed_sensors.APP, watchfon_spoofed_sensors.STEERING);
        subscribe(watchfon_spoofed_sensors.APP, watchfon_spoofed_sensors.FUEL);
        subscribe(watchfon_spoofed_sensors.APP, watchfon_spoofed_sensors.ENGINERPM);
        subscribe(watchfon_spoofed_sensors.APP, watchfon_spoofed_sensors.ODOMETER);
        subscribe(watchfon_spoofed_sensors.APP, watchfon_spoofed_sensors.GEAR);
    }


    @Override
    public void newData(DataMarshal.DataObject dObject) {
        super.newData(dObject);
        if (!isValidData(dObject)) return;
        if (dObject.device.equals(MiddlewareImpl.APP)) return;

        DataMarshal.DataObject latestEstimateSpeed = getLatestData(watchfon_estimates.APP, watchfon_estimates.SPEED);
        DataMarshal.DataObject latestSpoofedSpeed = getLatestData(watchfon_spoofed_sensors.APP, watchfon_spoofed_sensors.SPEED);
        if (latestEstimateSpeed != null && latestSpoofedSpeed != null) {
            Float estimateSpeed = latestEstimateSpeed.value[0];
            Map<String, Float> reportedSpeedMap = watchfon_spoofed_sensors.splitValues(latestSpoofedSpeed);
            Float reportedSpeed = reportedSpeedMap.get(watchfon_spoofed_sensors.SPEED);
        }
    }
}
