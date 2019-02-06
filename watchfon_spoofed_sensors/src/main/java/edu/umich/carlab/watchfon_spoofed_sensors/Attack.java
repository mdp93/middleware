package edu.umich.carlab.watchfon_spoofed_sensors;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ProgressBar;
import edu.umich.carlab.Constants;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.io.CLTripWriter;
import edu.umich.carlab.loadable.App;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.umich.carlab.watchfon_spoofed_sensors.MiddlewareImpl.*;

public class Attack {
    final int SLEEP_BETWEEN_STEPS = 250;
    final String TAG = "Attacker";
    // Sensors from the vehicle (with optional injection for intrusion detection evaluation)
    final edu.umich.carlab.watchfon_spoofed_sensors.MiddlewareImpl watchfon_spoofed_sensors =
            new edu.umich.carlab.watchfon_spoofed_sensors.MiddlewareImpl();
    String[] all_sensors = {
            watchfon_spoofed_sensors.SPEED,
            watchfon_spoofed_sensors.STEERING,
            watchfon_spoofed_sensors.FUEL,
            watchfon_spoofed_sensors.ODOMETER,
            watchfon_spoofed_sensors.GEAR,
            watchfon_spoofed_sensors.ENGINERPM,

    };
    private Handler attackRunHandler;
    private int attackStage = 0;
    private Map<String, Float> startingValues;
    private App parentApp;
    private Context context;
    private boolean initializedAttack = false;
    private ProgressBar attackProgress;
    private Map<String, Boolean> ongoingAttacks;
    private List<AttackSpec> attackSpecs;
    private Long endAttackerThreadAtTime;
    private Map<String, Integer> FP, TP, FN, TN;
    private Map<String, Long> firstDetectionTimeMap;
    private Map<String, Boolean> attackInitialized;
    private Map<String, Long> attackStartingTimes;
    Runnable stepAttack = new Runnable() {
        @Override
        public void run() {
        /*
            Step through at 250 ms intervals
            IF the time is within the start of any of the attacks:
                Load that attack
                Try to get that starting value
                Set that attack to "initialized"
                Set the ongoing attack for this sensor to true
                Set the starting time step for this attack, for interpolation purposes
         */


            Long currTime = System.currentTimeMillis();
            attackStage += 1;
            if (attackProgress != null) {
                attackProgress.setProgress(attackStage);
                Log.e(TAG, "Updating progress: " + attackStage);
            }

            Log.e(TAG, "Time is: " + currTime + " and end time was " + endAttackerThreadAtTime);

            String sensor;
            Long attackStartTime, attackEndTime;
            Float attackTargetValue;
            Type attackType;

            for (AttackSpec attackSpec : attackSpecs) {
                sensor = attackSpec.sensor;
                attackStartTime = attackSpec.startTime;
                attackEndTime = attackSpec.endTime;
                attackTargetValue = attackSpec.value;
                attackType = attackSpec.type;

                // 1. Initialize
                initializeIfNeeded(attackSpec, currTime);

                // 2. Progress attack
                if (currTime > attackStartTime && currTime < attackEndTime) {
                    float currentInjectValue = 0;
                    float percentDone = (float) (currTime - attackStartTime)
                            / (float) (attackEndTime - attackStartTime);
                    if (attackType == Type.GRADUAL)
                        currentInjectValue = percentDone * attackTargetValue
                                + (1 - percentDone) * startingValues.get(sensor);
                    else
                        currentInjectValue = attackTargetValue;

                    parentApp.outputData("watchfon_test_suite", "attack", new Float[]{
                            ONE_HOT_SENSORS.get(sensor),
                            currentInjectValue,
                            (float) attackType.ordinal(),
                    });
                } else if (currTime > attackEndTime) {
                    // 3. Shut down attack if needed
                    ongoingAttacks.put(sensor, false);
                    parentApp.outputData("watchfon_test_suite", "attack", new Float[]{
                            ONE_HOT_SENSORS.get(sensor),
                            0f,
                            (float) Type.DELTA.ordinal(),
                    });
                }
            }

            // 4. Close runnable if done
            if (currTime > endAttackerThreadAtTime) {
                Log.e(TAG, "Done. Exiting " + currTime + " and end time was " + endAttackerThreadAtTime);
                saveResults();
                return;
            }

            attackRunHandler.postDelayed(
                    stepAttack,
                    SLEEP_BETWEEN_STEPS);
        }
    };


    public Attack(App parentApp, Context context) {
        this.parentApp = parentApp;
        this.context = context;

        Looper mainLooper = Looper.getMainLooper();
        if (mainLooper == null)
            Log.e(TAG, "Unable to start attacker. Looper not present");
        else
            attackRunHandler = new Handler(mainLooper);
    }


    public void setProgressBar(ProgressBar pbar) {
        attackProgress = pbar;
    }

    void saveResults() {
        File resultsDir = CLTripWriter.GetResultsDir(context);
        String filename = String.format("%d.json", System.currentTimeMillis());
        File ofile = new File(resultsDir, filename);

        try {
            JSONObject resultObj = new JSONObject();
            for (String sensor : all_sensors) {
                JSONObject sensorResult = new JSONObject();
                sensorResult.put("tp", TP.get(sensor));
                sensorResult.put("fp", FP.get(sensor));
                sensorResult.put("tn", TN.get(sensor));
                sensorResult.put("fn", FN.get(sensor));
                resultObj.put(sensor, sensorResult);
            }

            FileOutputStream fos = new FileOutputStream(ofile);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            osw.write(resultObj.toString());
            osw.close();
            fos.close();

            Intent intent = new Intent(Constants.DONE_RUNNING_SPEC_FILE);
            intent.putExtra(Constants.SPEC_RESULT_FILENAME, ofile.getAbsolutePath());
            Log.v(TAG, "Finished running spec file evaluation. Saving file to " + ofile.getAbsolutePath());
            context.sendBroadcast(intent);
        } catch (Exception e) {
            Log.e(TAG, "Trouble saving result");
        }
    }

    void initializeIfNeeded(AttackSpec attackSpec, Long currTime) {
        String sensor_ = attackSpec.sensor;
        if (currTime > attackSpec.startTime
                && attackInitialized.containsKey(sensor_)
                && !attackInitialized.get(sensor_)) {

            // Attack initialization
            // Try to get the starting value
            DataMarshal.DataObject dataObject = parentApp.getLatestData(
                    MiddlewareImpl.APP,
                    sensor_);

            if (dataObject != null) {
                MiddlewareImpl spoofedMiddleware = new MiddlewareImpl();
                Map<String, Float> values = spoofedMiddleware.splitValues(dataObject);
                startingValues.put(sensor_, values.get(sensor_) - values.get(MiddlewareImpl.INJECTION_MAGNITUDE));
            }

            ongoingAttacks.put(sensor_, true);
            attackStartingTimes.put(sensor_, currTime);
        }
    }

    public void attackDetected(String sensor, Boolean detectedFlag) {
        /*
            TP: A particular sensor is injected, and we detected that sensor
            FP: A particular sensor is not injected, but we flagged that sensor
            TN: Sensor X was not injected, and we didn't flag it
            FN: X was injected, but we didn't flag it
         */

        if (!initializedAttack) return;
        Log.v(TAG, "Attack detected and initialized " + sensor);

        boolean ongoingAttack = ongoingAttacks.get(sensor);

        if (ongoingAttack && detectedFlag) TP.put(sensor, TP.get(sensor) + 1);
        else if (ongoingAttack && !detectedFlag) FN.put(sensor, FN.get(sensor) + 1);
        else if (!ongoingAttack && detectedFlag) FP.put(sensor, FP.get(sensor) + 1);
        else if (!ongoingAttack && !detectedFlag) TN.put(sensor, TN.get(sensor) + 1);

        long currTime = System.currentTimeMillis();
        if (ongoingAttack && detectedFlag && firstDetectionTimeMap.get(sensor) == -1) {
            firstDetectionTimeMap.put(sensor, currTime);
            Log.e(TAG, "Detected attack at " + currTime);
        }
    }

    public void runAttackGeneral(List<AttackSpec> attackSpecs, Float duration) {

        this.attackSpecs = attackSpecs;
        Long currTime = System.currentTimeMillis();
        endAttackerThreadAtTime = (long) (currTime + (long) (duration * 1000));

        if (attackProgress != null)
            attackProgress.setMax((int) (duration * 1000 / SLEEP_BETWEEN_STEPS));
        attackStage = 0;
        TP = new HashMap<>();
        FP = new HashMap<>();
        TN = new HashMap<>();
        FN = new HashMap<>();

        ongoingAttacks = new HashMap<>();
        firstDetectionTimeMap = new HashMap<>();
        startingValues = new HashMap<>();
        attackInitialized = new HashMap<>();
        attackStartingTimes = new HashMap<>();

        for (AttackSpec attack : attackSpecs) {
            ongoingAttacks.put(attack.sensor, false);
            firstDetectionTimeMap.put(attack.sensor, -1L);
            startingValues.put(attack.sensor, 0F);
            attackInitialized.put(attack.sensor, false);
            attackStartingTimes.put(attack.sensor, -1L);

            TP.put(attack.sensor, 0);
            FP.put(attack.sensor, 0);
            TN.put(attack.sensor, 0);
            FN.put(attack.sensor, 0);
        }

        for (String sensor : all_sensors) {
            if (!TP.containsKey(sensor)) {
                TP.put(sensor, 0);
                FP.put(sensor, 0);
                TN.put(sensor, 0);
                FN.put(sensor, 0);
                firstDetectionTimeMap.put(sensor, -1L);
                ongoingAttacks.put(sensor, false);
            }
        }

        Log.v(TAG, "Attack started. Posting");
        initializedAttack = true;
        attackRunHandler.post(stepAttack);
    }


    public void runAttack(int attackSelectionId) {
        Type attackType = Type.DELTA;
        String attackSensor = SPEED;
        Float targetValue = 0f;

        switch (attackSelectionId) {
            case 0:
                attackType = Type.DELTA;
                attackSensor = SPEED;
                targetValue = 10f;
                break;
            case 1:
                attackType = Type.GRADUAL;
                attackSensor = SPEED;
                targetValue = 4f;
                break;
            case 2:
                attackType = Type.GRADUAL;
                attackSensor = STEERING;
                targetValue = 10f;
                break;
            case 3:
                attackType = Type.SUDDEN;
                attackSensor = SPEED;
                targetValue = 4f;
                break;
            case 4:
                attackType = Type.SUDDEN;
                attackSensor = STEERING;
                targetValue = -10f;
                break;
            case 5:
                // No attack condition
                attackType = Type.DELTA;
                attackSensor = STEERING;
                targetValue = 0f;
        }

        List<AttackSpec> attackSpecs = new ArrayList<>();
        attackSpecs.add(new AttackSpec(
                attackSensor,
                System.currentTimeMillis() + 7 * 1000L,
                System.currentTimeMillis() + 15 * 1000L,
                attackType,
                targetValue
        ));
        runAttackGeneral(attackSpecs, 15.0f);
    }

    public enum Type {
        GRADUAL, SUDDEN, DELTA
    }

    public static class AttackSpec {
        String sensor;
        Long startTime;
        Long endTime;
        Type type;
        Float value;

        public AttackSpec(
                String sensor,
                Long startTime,
                Long endTime,
                Type type,
                Float value) {
            this.sensor = sensor;
            this.startTime = startTime;
            this.endTime = endTime;
            this.type = type;
            this.value = value;
        }
    }
}
