package edu.umich.carlab.watchfon_estimates;

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
    final String TAG = "watchfon_estimates";
    final edu.umich.carlab.watchfon_speed.MiddlewareImpl watchfon_speed = new edu.umich.carlab.watchfon_speed.MiddlewareImpl();
    final edu.umich.carlab.watchfon_steering.MiddlewareImpl watchfon_steering = new edu.umich.carlab.watchfon_steering.MiddlewareImpl();
    final edu.umich.carlab.watchfon_fuel.MiddlewareImpl watchfon_fuel = new edu.umich.carlab.watchfon_fuel.MiddlewareImpl();
    final edu.umich.carlab.watchfon_rpm.MiddlewareImpl watchfon_rpm = new edu.umich.carlab.watchfon_rpm.MiddlewareImpl();
    final edu.umich.carlab.watchfon_odometer.MiddlewareImpl watchfon_odometer = new edu.umich.carlab.watchfon_odometer.MiddlewareImpl();
    final edu.umich.carlab.watchfon_gear.MiddlewareImpl watchfon_gear = new edu.umich.carlab.watchfon_gear.MiddlewareImpl();


    public AppImpl(CLDataProvider cl, Context context) {
        super(cl, context);
        name = "watchfon_estimates";

        subscribe(watchfon_speed.APP, watchfon_speed.SPEED);
        subscribe(watchfon_steering.APP, watchfon_steering.STEERING);
        subscribe(watchfon_fuel.APP, watchfon_fuel.FUEL);
        subscribe(watchfon_rpm.APP, watchfon_rpm.RPM);
        subscribe(watchfon_odometer.APP, watchfon_odometer.DISTANCE);
        subscribe(watchfon_gear.APP, watchfon_gear.GEAR);
    }


    @Override
    public void newData(DataMarshal.DataObject dObject) {
        super.newData(dObject);
        if (!isValidData(dObject)) return;
        if (dObject.device.equals(MiddlewareImpl.APP)) return;

        if (dObject.sensor.equals(watchfon_rpm.RPM))
            outputData(MiddlewareImpl.APP, dObject, MiddlewareImpl.ENGINERPM, dObject.value);

        else if (dObject.sensor.equals(watchfon_odometer.DISTANCE))
            outputData(MiddlewareImpl.APP, dObject, MiddlewareImpl.ODOMETER, dObject.value);

        else
            outputData(MiddlewareImpl.APP, dObject, dObject.sensor, dObject.value);
    }
}
