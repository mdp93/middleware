package edu.umich.carlab.watchfon_fuel;

import android.app.Activity;
import android.content.Context;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import edu.umich.carlab.CLDataProvider;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.loadable.App;


public class AppImpl extends App {
    final String TAG = "watchfon_fuel";
    final String FUEL_KEY = "fuel";

    final Double MILE_PER_KM = 0.621371;
    final Double GALLONS_PER_LITER = 0.26417217685;
    final edu.umich.carlab.watchfon_odometer.MiddlewareImpl watchfon_odometer = new edu.umich.carlab.watchfon_odometer.MiddlewareImpl();
    // Parameters
    Float AVERAGE_MPG = 23f;
    Float MAX_FUEL_CAPACITY = 18f;
    MiddlewareImpl middleware = new MiddlewareImpl();

    Double previousFuelLevel; // in liters
    Double lastOdometer, currOdometer;
    Double distance, fuelConsumed;


    public AppImpl(CLDataProvider cl, Context context) {
        super(cl, context);
        name = "watchfon_fuel";

        middlewareName = MiddlewareImpl.APP;
        sensors.add(new Pair<>(watchfon_odometer.APP, watchfon_odometer.DISTANCE));

        if (context != null) {
            previousFuelLevel = loadValue(FUEL_KEY, (double) MAX_FUEL_CAPACITY);
            AVERAGE_MPG = middleware.getParameterOrDefault(context, middleware.AVERAGE_MPG, AVERAGE_MPG);
            MAX_FUEL_CAPACITY = middleware.getParameterOrDefault(
                    context,
                    middleware.MAX_FUEL_CAPACITY,
                    MAX_FUEL_CAPACITY);
        }
    }


    @Override
    public void newData(DataMarshal.DataObject dObject) {
        super.newData(dObject);
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

    @Override
    public View initializeVisualization(Activity parentActivity) {
        super.initializeVisualization(parentActivity);
        Button resetButton = new Button(parentActivity);
        resetButton.setText("Reset Fuel to Maximum");
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                previousFuelLevel = (double) MAX_FUEL_CAPACITY;
                saveValue(FUEL_KEY, (double) MAX_FUEL_CAPACITY);
            }
        });
        return resetButton;
    }


    @Override
    public void shutdown() {
        super.shutdown();
        saveValue(FUEL_KEY, previousFuelLevel);
    }
}
