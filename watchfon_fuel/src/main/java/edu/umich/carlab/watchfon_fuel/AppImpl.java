package edu.umich.carlab.watchfon_fuel;

import android.content.Context;
import android.hardware.SensorManager;
import android.util.Pair;
import edu.umich.carlab.CLDataProvider;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.loadable.App;
import edu.umich.carlab.sensors.PhoneSensors;
import java.util.Map;


public class AppImpl extends App {
    final String TAG = "watchfon_fuel";

    final Double MILE_PER_KM = 0.621371;
    final Double GALLONS_PER_LITER = 0.26417217685;
    final Double AVERAGE_MPG = 23d;
    final Double MAX_FUEL_CAPACITY = 18d;
    final edu.umich.carlab.watchfon_odometer.MiddlewareImpl watchfon_odometer = new edu.umich.carlab.watchfon_odometer.MiddlewareImpl();

    Double previousFuelLevel; // in liters
    Double lastOdometer, currOdometer;
    Double distance, fuelConsumed;

    public AppImpl(CLDataProvider cl, Context context) {
        super(cl, context);
        name = "watchfon_fuel";
        sensors.add(new Pair<>(watchfon_odometer.APP, watchfon_odometer.DISTANCE));
        previousFuelLevel = MAX_FUEL_CAPACITY;
    }


    @Override
    public void newData(DataMarshal.DataObject dObject) {
        if (dObject.dataType != DataMarshal.MessageType.DATA) return;
        if (dObject.device.equals(MiddlewareImpl.APP)) return;
        if (dObject.value == null) return;

        if (dObject.device.equals(watchfon_odometer.APP) && dObject.sensor.equals(watchfon_odometer.DISTANCE)) {
            currOdometer = dObject.value[0].doubleValue();
            if (lastOdometer != null) {
                distance = currOdometer - lastOdometer; // in meters
                distance = distance / 1000 * MILE_PER_KM; // in miles;
                fuelConsumed = distance / AVERAGE_MPG / GALLONS_PER_LITER; // in gallons
                previousFuelLevel -= fuelConsumed;
                outputData(MiddlewareImpl.APP, dObject, MiddlewareImpl.FUEL, previousFuelLevel.floatValue());
            }
        }

        lastOdometer = currOdometer;


    }
}