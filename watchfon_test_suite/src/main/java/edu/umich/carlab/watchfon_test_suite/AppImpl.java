package edu.umich.carlab.watchfon_test_suite;

import android.app.Activity;
import android.content.Context;
import android.hardware.SensorManager;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import edu.umich.carlab.CLDataProvider;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.loadable.App;
import edu.umich.carlab.sensors.PhoneSensors;

import java.util.Map;


public class AppImpl extends App {
    final String TAG = "watchfon_test_suite";

    final edu.umich.carlab.watchfon_estimates.MiddlewareImpl estimates =
            new edu.umich.carlab.watchfon_estimates.MiddlewareImpl();

    final edu.umich.carlab.watchfon_spoofed_sensors.MiddlewareImpl spoofed_sensors =
            new edu.umich.carlab.watchfon_spoofed_sensors.MiddlewareImpl();

    final edu.umich.carlab.watchfon_intrusion_detection.MiddlewareImpl intrusion_detection =
            new edu.umich.carlab.watchfon_intrusion_detection.MiddlewareImpl();

    String[] all_sensors = {
            estimates.SPEED,
            estimates.STEERING,
            estimates.FUEL,
            estimates.ODOMETER,
            estimates.GEAR,
            estimates.ENGINERPM,

    };

    public AppImpl(CLDataProvider cl, Context context) {
        super(cl, context);
        name = "WatchFon Test Suite";

        for (String sensor : all_sensors)
            subscribe(spoofed_sensors.APP, sensor);

        subscribe(intrusion_detection.APP, intrusion_detection.DETECTION);
    }


    @Override
    public void newData(DataMarshal.DataObject dObject) {
        super.newData(dObject);
        if (!isValidData(dObject)) return;
        if (dObject.device.equals(MiddlewareImpl.APP)) return;
    }


    @Override
    public View initializeVisualization(Activity parentActivity) {
        super.initializeVisualization(parentActivity);
        LayoutInflater inflater = parentActivity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.test_suite, null);
        return layout;
    }
}
