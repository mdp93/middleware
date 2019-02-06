package edu.umich.carlab.watchfon_test_suite;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import edu.umich.carlab.CLDataProvider;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.loadable.App;
import edu.umich.carlab.watchfon_spoofed_sensors.Attack;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.umich.carlab.Constants.*;
import static edu.umich.carlab.watchfon_intrusion_detection.MiddlewareImpl.*;
import static edu.umich.carlab.watchfon_spoofed_sensors.MiddlewareImpl.*;

public class AppImpl extends App {
    final String TAG = "watchfon_test_suite";

    final edu.umich.carlab.watchfon_spoofed_sensors.MiddlewareImpl spoofed_sensors =
            new edu.umich.carlab.watchfon_spoofed_sensors.MiddlewareImpl();

    final edu.umich.carlab.watchfon_intrusion_detection.MiddlewareImpl intrusion_detection =
            new edu.umich.carlab.watchfon_intrusion_detection.MiddlewareImpl();
    // The test suite can also load attack specifications from the spec file
    // This might be stored from the Load_Attack_From_Specs_Key file
    JSONArray attackSpecsJson;
    float specStartTimeInSecs = -1, specEndTimeInSecs = -1;
    Long firstDataTime = null;
    boolean attackInitiated = false;
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
        middlewareName = MiddlewareImpl.APP;
        foregroundApp = true;
        sensorRows = new HashMap<>();
        attacker = new Attack(this, context);

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

        SharedPreferences globalPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (globalPrefs != null) {
            String specAttackString = globalPrefs.getString(Load_Attack_From_Specs_Key, null);
            specStartTimeInSecs = globalPrefs.getFloat(Load_From_Trace_Duration_Start, 0);
            specEndTimeInSecs = globalPrefs.getFloat(Load_From_Trace_Duration_End, 30);
            if (specAttackString != null) {
                try {
                    attackSpecsJson = new JSONArray(specAttackString);
                } catch (Exception e) {
                    Log.e(TAG, "Error loading the JSON array: " + specAttackString);
                }

                // Unset this for the future
                globalPrefs.edit().putString(Load_Attack_From_Specs_Key, null).apply();
            }
        }
    }


    @Override
    public void newData(DataMarshal.DataObject dObject) {
        super.newData(dObject);
        if (!isValidData(dObject)) return;
        if (dObject.device.equals(MiddlewareImpl.APP)) return;

        String dev = dObject.device;
        String sen = dObject.sensor;

        if (dev.equals(spoofed_sensors.APP) && firstDataTime == null)
            firstDataTime = dObject.time;

        if (attackSpecsJson != null
                && dev.equals(spoofed_sensors.APP)
                && dObject.time > (long)(firstDataTime + (specStartTimeInSecs * 1000))
                && !attackInitiated) {
            Log.v(TAG, "Conditions met. Initiating attack");
            attackInitiated = true;
            List<Attack.AttackSpec> attackSpecs = new ArrayList<>();
            for (int i = 0; i < attackSpecsJson.length(); i++) {
                try {
                    JSONObject attackSpecJsonObj = attackSpecsJson.getJSONObject(i);
                    JSONArray durationTimeJson = attackSpecJsonObj.getJSONArray("time");
                    Attack.Type attackType = Attack.Type.DELTA;
                    String attackTypeString = attackSpecJsonObj.getString("type");
                    if (attackTypeString.equals("gradual"))
                        attackType = Attack.Type.GRADUAL;
                    else if (attackTypeString.equals("sudden"))
                        attackType = Attack.Type.SUDDEN;

                    attackSpecs.add(new Attack.AttackSpec(
                            attackSpecJsonObj.getString("sensor"),
                            durationTimeJson.getLong(0),
                            durationTimeJson.getLong(1),
                            attackType,
                            (float) attackSpecJsonObj.getDouble("value")
                    ));
                } catch (Exception e) {
                }
            }

            attacker.runAttackGeneral(attackSpecs, specEndTimeInSecs - specStartTimeInSecs);
        }


        // Update attacker detection
        if (dev.equals(intrusion_detection.APP) && sen.equals(DETECTION)) {
            Map<String, Float> detectionDetails = intrusion_detection.splitValues(dObject);
            String sensor = ONE_HOT_REVERSE.get(detectionDetails.get(DETECTION_SENSOR));
            final Boolean sensorDetected = detectionDetails.get(DETECTION_FLAG) != 0;
            if (attacker != null) {
                attacker.attackDetected(sensor, sensorDetected);
            }

            final SensorRow sensorRow = sensorRows.get(sensor);
            if (sensorRow != null) {
                parentActivity.runOnUiThread(() -> sensorRow.setDetection(sensorDetected));
            }

        }

        // Update visualization
        if (visualizationInitialized) {
            if (dev.equals(intrusion_detection.APP) && sen.equals(DETECTION)) {
                Map<String, Float> detectionDetails = intrusion_detection.splitValues(dObject);
                String sensor = ONE_HOT_REVERSE.get(detectionDetails.get(DETECTION_SENSOR));
                final Boolean sensorDetected = detectionDetails.get(DETECTION_FLAG) != 0;
                final SensorRow sensorRow = sensorRows.get(sensor);
                if (sensorRow != null) {
                    parentActivity.runOnUiThread(() -> sensorRow.setDetection(sensorDetected));
                }
            } else if (dev.equals(spoofed_sensors.APP)) {
                final Map<String, Float> splitValues = spoofed_sensors.splitValues(dObject);
                final SensorRow sensorRow = sensorRows.get(sen);
                parentActivity.runOnUiThread(() -> sensorRow.setInjection(splitValues.get(INJECTION_MAGNITUDE)));
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
        attacker.setProgressBar(attackProgress);

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
