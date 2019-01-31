package edu.umich.carlab.watchfon_test_suite;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import edu.umich.carlab.CLDataProvider;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.loadable.App;
import edu.umich.carlab.watchfon_spoofed_sensors.Attack;

import java.util.HashMap;
import java.util.Map;

import static edu.umich.carlab.watchfon_intrusion_detection.MiddlewareImpl.*;
import static edu.umich.carlab.watchfon_spoofed_sensors.MiddlewareImpl.*;

public class AppImpl extends App {
    final String TAG = "watchfon_test_suite";

    final edu.umich.carlab.watchfon_spoofed_sensors.MiddlewareImpl spoofed_sensors =
            new edu.umich.carlab.watchfon_spoofed_sensors.MiddlewareImpl();

    final edu.umich.carlab.watchfon_intrusion_detection.MiddlewareImpl intrusion_detection =
            new edu.umich.carlab.watchfon_intrusion_detection.MiddlewareImpl();

    private boolean visualizationInitialized = false;
    private Spinner attackSelection;
    private Map<String, SensorRow> sensorRows;


    private Attack attacker;


    private View.OnClickListener runAttackListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int attackSelectionId = attackSelection.getSelectedItemPosition();
            attacker.runAttack(attackSelectionId);
        }
    };

    public AppImpl(CLDataProvider cl, Context context) {
        super(cl, context);
        name = "WatchFon Test Suite";
        foregroundApp = true;
        sensorRows = new HashMap<>();

        String[] all_sensors = {
                SPEED,
                STEERING,
                FUEL,
                ODOMETER,
                GEAR,
                ENGINERPM,
        };

        for (String sensor : all_sensors)
            subscribe(
                    spoofed_sensors.APP,
                    sensor);

        subscribe(
                intrusion_detection.APP,
                DETECTION);
    }


    @Override
    public void newData(DataMarshal.DataObject dObject) {
        super.newData(dObject);
        if (!isValidData(dObject)) return;
        if (dObject.device.equals(MiddlewareImpl.APP)) return;


        String dev = dObject.device;
        String sen = dObject.sensor;

        if (visualizationInitialized) {
            if (dev.equals(intrusion_detection.APP) && sen.equals(DETECTION)) {
                Map<String, Float> detectionDetails = intrusion_detection.splitValues(dObject);
                String sensor = ONE_HOT_REVERSE.get(
                        detectionDetails.get(DETECTION_SENSOR));
                final Boolean sensorDetected = detectionDetails.get(DETECTION_FLAG) != 0;
                final SensorRow sensorRow = sensorRows.get(sensor);
                if (sensorRow != null) {
                    parentActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sensorRow.setDetection(sensorDetected);
                        }
                    });
                }

                if (attacker != null) {
                    attacker.attackDetected(sensor, sensorDetected);
                }
            } else if (dev.equals(spoofed_sensors.APP)) {
                final Map<String, Float> splitValues = spoofed_sensors.splitValues(dObject);
                final SensorRow sensorRow = sensorRows.get(sen);

                parentActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        sensorRow.setInjection(splitValues.get(INJECTION_MAGNITUDE));
                    }
                });

            }
        }
    }


    void initializeSensorRow(String sensor, View layout, int ID) {
        SensorRow sensorRow = layout.findViewById(ID);
        sensorRow.initializeParameters(
                DURATIONS.get(sensor),
                MAGNITUDES.get(sensor)
        );
        sensorRows.put(sensor, sensorRow);
    }

    @Override
    public View initializeVisualization(Activity parentActivity) {
        super.initializeVisualization(parentActivity);
        LayoutInflater inflater = parentActivity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.test_suite, null);

        Button runAttack = layout.findViewById(R.id.attack_start);
        attackSelection = layout.findViewById(R.id.attack_selection);
        ProgressBar attackProgress = layout.findViewById(R.id.attack_progress);
        attacker = new Attack(parentActivity, attackProgress, this);
        runAttack.setOnClickListener(runAttackListener);

        initializeSensorRow(SPEED, layout, R.id.speed);
        initializeSensorRow(STEERING, layout, R.id.steering);
        initializeSensorRow(GEAR, layout, R.id.gear);
        initializeSensorRow(FUEL, layout, R.id.fuel);
        initializeSensorRow(ODOMETER, layout, R.id.odometer);
        initializeSensorRow(ENGINERPM, layout, R.id.rpm);

        visualizationInitialized = true;
        return layout;
    }
}
