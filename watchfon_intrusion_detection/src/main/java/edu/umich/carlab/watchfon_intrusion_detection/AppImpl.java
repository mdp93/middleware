package edu.umich.carlab.watchfon_intrusion_detection;

import android.content.Context;
import android.hardware.SensorManager;
import android.provider.ContactsContract;
import android.util.Pair;
import edu.umich.carlab.CLDataProvider;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.loadable.App;
import edu.umich.carlab.sensors.PhoneSensors;

import java.util.Map;


public class AppImpl extends App {
    final String TAG = "watchfon_intrusion_detection";

    // Sensors estimated by WatchFon
    final edu.umich.carlab.watchfon_speed.MiddlewareImpl watchfon_speed = new edu.umich.carlab.watchfon_speed.MiddlewareImpl();
    final edu.umich.carlab.watchfon_steering.MiddlewareImpl watchfon_steering = new edu.umich.carlab.watchfon_steering.MiddlewareImpl();
    final edu.umich.carlab.watchfon_fuel.MiddlewareImpl watchfon_fuel = new edu.umich.carlab.watchfon_fuel.MiddlewareImpl();
    final edu.umich.carlab.watchfon_rpm.MiddlewareImpl watchfon_rpm = new edu.umich.carlab.watchfon_rpm.MiddlewareImpl();
    final edu.umich.carlab.watchfon_odometer.MiddlewareImpl watchfon_odometer = new edu.umich.carlab.watchfon_odometer.MiddlewareImpl();
    final edu.umich.carlab.watchfon_gear.MiddlewareImpl watchfon_gear = new edu.umich.carlab.watchfon_gear.MiddlewareImpl();

    // Sensors from the vehicle (with optional injection for intrusion detection evaluation)
    final edu.umich.carlab.watchfon_spoofed_sensors.MiddlewareImpl watchfon_spoofed_sensors = new edu.umich.carlab.watchfon_spoofed_sensors.MiddlewareImpl();


    public AppImpl(CLDataProvider cl, Context context) {
        super(cl, context);
        name = "watchfon_intrusion_detection";

        subscribe(watchfon_speed.APP, watchfon_speed.SPEED);
        subscribe(watchfon_steering.APP, watchfon_steering.STEERING);
        subscribe(watchfon_fuel.APP, watchfon_fuel.FUEL);
        subscribe(watchfon_rpm.APP, watchfon_rpm.RPM);
        subscribe(watchfon_odometer.APP, watchfon_odometer.DISTANCE);
        subscribe(watchfon_gear.APP, watchfon_gear.GEAR);


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

        DataMarshal.DataObject latestEstimateSpeed = getLatestData(watchfon_speed.APP, watchfon_speed.SPEED);
        DataMarshal.DataObject latestSpoofedSpeed = getLatestData(watchfon_spoofed_sensors.APP, watchfon_spoofed_sensors.SPEED);
        if (latestEstimateSpeed != null && latestSpoofedSpeed != null) {
            Float estimateSpeed = latestEstimateSpeed.value[0];
            Map<String, Float> reportedSpeedMap = watchfon_spoofed_sensors.splitValues(latestSpoofedSpeed);
            Float reportedSpeed = reportedSpeedMap.get(watchfon_spoofed_sensors.SPEED);
        }
    }
}
