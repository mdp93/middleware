package edu.umich.carlab.watchfon_test_suite;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
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

import java.util.HashMap;
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

    boolean visualizationInitialized = false;

    Map<String, SensorRow> sensorRows;

    public AppImpl(CLDataProvider cl, Context context) {
        super(cl, context);
        name = "WatchFon Test Suite";
        foregroundApp = true;
        sensorRows = new HashMap<>();

        for (String sensor : all_sensors)
            subscribe(spoofed_sensors.APP, sensor);

        subscribe(intrusion_detection.APP, intrusion_detection.DETECTION);
    }


    @Override
    public void newData(DataMarshal.DataObject dObject) {
        super.newData(dObject);
        if (!isValidData(dObject)) return;
        if (dObject.device.equals(MiddlewareImpl.APP)) return;


        String dev = dObject.device;
        String sen = dObject.sensor;

        if (visualizationInitialized) {
            if (dev.equals(intrusion_detection.APP) && sen.equals(intrusion_detection.DETECTION)) {
                Map<String, Float> detectionDetails = intrusion_detection.splitValues(dObject);
                String sensor = intrusion_detection.ONE_HOT_REVERSE.get(
                        detectionDetails.get(
                                intrusion_detection.DETECTION_SENSOR));
                final Boolean sensorDetected = detectionDetails.get(intrusion_detection.DETECTION_FLAG) != 0;
                final SensorRow sensorRow = sensorRows.get(sensor);
                if (sensorRow != null) {
                    parentActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sensorRow.setDetection(sensorDetected);
                        }
                    });
                }
            } else if (dev.equals(spoofed_sensors.APP)) {
                final Map<String, Float> splitValues = spoofed_sensors.splitValues(dObject);
                final SensorRow sensorRow = sensorRows.get(sen);

                parentActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        sensorRow.setInjection(splitValues.get(spoofed_sensors.INJECTION_MAGNITUDE));
                    }
                });

            }
        }
    }


    void initializeSensorRow (String sensor, View layout, int ID) {
        SensorRow sensorRow = layout.findViewById(ID);
        sensorRow.initializeParameters(
                intrusion_detection.DURATIONS.get(sensor),
                intrusion_detection.MAGNITUDES.get(sensor)
        );
        sensorRows.put(sensor, sensorRow);
    }

    @Override
    public View initializeVisualization(Activity parentActivity) {
        super.initializeVisualization(parentActivity);
        LayoutInflater inflater = parentActivity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.test_suite, null);

        initializeSensorRow(estimates.SPEED, layout, R.id.speed);
        initializeSensorRow(estimates.STEERING, layout, R.id.steering);
        initializeSensorRow(estimates.GEAR, layout, R.id.gear);
        initializeSensorRow(estimates.FUEL, layout, R.id.fuel);
        initializeSensorRow(estimates.ODOMETER, layout, R.id.odometer);
        initializeSensorRow(estimates.ENGINERPM, layout, R.id.rpm);

        visualizationInitialized = true;
        return layout;
    }
}
