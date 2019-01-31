package edu.umich.carlab.watchfon_spoofed_sensors;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.loadable.App;
import edu.umich.carlab.loadable.Middleware;

import java.util.Map;

import static edu.umich.carlab.watchfon_spoofed_sensors.MiddlewareImpl.ONE_HOT_SENSORS;
import static edu.umich.carlab.watchfon_spoofed_sensors.MiddlewareImpl.SPEED;
import static edu.umich.carlab.watchfon_spoofed_sensors.MiddlewareImpl.STEERING;

public class Attack {
    private Handler attackRunHandler;
    private int attackStage = 0;
    private float startingValue = 0;
    private float targetValue = 0;

    private Activity activity;
    private App parentApp;
    private ProgressBar attackProgress;
    private Type attackType;
    private String attackSensor;

    final int TOTAL_ATTACK_STEPS = 30;
    final int SLEEP_BETWEEN_STEPS = 1000;
    final int START_ATTACK_AT = 15;
    boolean ongoingAttack = false;

    int FP, TP, FN, TN;
    long startOfAttack = 0L;
    long firstDetectionTime = -1;

    final String TAG = "Attacker";

    Runnable updateAttackStep = new Runnable() {
        @Override
        public void run() {
            attackStage += 1;

            if (attackStage == START_ATTACK_AT) {
                DataMarshal.DataObject dataObject = parentApp.getLatestData(
                        MiddlewareImpl.APP,
                        attackSensor);

                if (dataObject == null)
                    startingValue = 0;
                else {
                    MiddlewareImpl spoofedMiddleware = new MiddlewareImpl();
                    Map<String, Float> values = spoofedMiddleware.splitValues(dataObject);
                    startingValue = values.get(attackSensor) - values.get(MiddlewareImpl.INJECTION_MAGNITUDE);
                }

                ongoingAttack = true;
                startOfAttack = System.currentTimeMillis();
            }

            if (attackProgress != null)
                attackProgress.setProgress(attackStage);

            if (attackStage < TOTAL_ATTACK_STEPS && attackStage >= START_ATTACK_AT) {
                // Do the actual attack
                float currentInjectValue = 0;
                float percentDone = (float)(attackStage - START_ATTACK_AT) / (float)(TOTAL_ATTACK_STEPS - START_ATTACK_AT);
                if (attackType == Type.GRADUAL)
                    currentInjectValue = percentDone * targetValue + (1 - percentDone) * startingValue;
                else
                    currentInjectValue = targetValue;


                parentApp.outputData("watchfon_test_suite", "attack", new Float[] {
                        ONE_HOT_SENSORS.get(attackSensor),
                        currentInjectValue,
                        (float)attackType.ordinal(),
                });

            } else if (attackStage > TOTAL_ATTACK_STEPS) {
                // Stop the attack
                parentApp.outputData("watchfon_test_suite", "attack", new Float[] {
                        ONE_HOT_SENSORS.get(attackSensor),
                        0f,
                        (float)Type.DELTA.ordinal(),
                });

                String scoreMessage = String.format(
                        "TP=%d, FP=%d, TN=%d, FN=%d. Seconds before detection %.02f",
                        TP, FP, TN, FN, (firstDetectionTime - startOfAttack) / 1000.0);
                Log.e(TAG, scoreMessage);
                Toast.makeText(
                        activity,
                        scoreMessage,
                        Toast.LENGTH_LONG).show();
                return;
            }


            attackRunHandler.postDelayed(
                    updateAttackStep,
                    SLEEP_BETWEEN_STEPS);
        }
    };


    public Attack(Activity activity, ProgressBar progressBar, App parentApp) {
        this.activity = activity;
        this.attackProgress = progressBar;
        this.parentApp = parentApp;


        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                attackRunHandler = new Handler();
            }
        });
    }


    public void attackDetected(String sensor, Boolean detectedFlag) {
        if (sensor.equals(attackSensor)) {
            if (ongoingAttack && detectedFlag) TP += 1;
            else if (ongoingAttack && !detectedFlag) FN += 1;
            else if (!ongoingAttack && detectedFlag) FP += 1;
            else if (!ongoingAttack && !detectedFlag) TN += 1;

            long currTime = System.currentTimeMillis();
            if (ongoingAttack && detectedFlag && firstDetectionTime == -1) {
                firstDetectionTime = currTime;
                Log.e(TAG, "Detected attack at " + currTime);
            }
        }
    }

    public void runAttack(int attackSelectionId) {
        switch (attackSelectionId) {
            case 0:
                attackType = Type.DELTA;
                attackSensor = SPEED;
                targetValue =  10;
                break;
            case 1:
                attackType = Type.GRADUAL;
                attackSensor = SPEED;
                targetValue =  4;
                break;
            case 2:
                attackType = Type.GRADUAL;
                attackSensor = STEERING;
                targetValue =  10;
                break;
            case 3:
                attackType = Type.SUDDEN;
                attackSensor = SPEED;
                targetValue =  4;
                break;
            case 4:
                attackType = Type.SUDDEN;
                attackSensor = STEERING;
                targetValue =  -10;
                break;
            case 5:
                // No attack condition
                attackType = Type.DELTA;
                attackSensor = STEERING;
                startingValue = 0;
                targetValue = 0;
        }


        if (attackProgress != null)
            attackProgress.setMax(TOTAL_ATTACK_STEPS);
        attackStage = 0;
        TP = 0; FP = 0; TN = 0; FN = 0;
        ongoingAttack = false;
        firstDetectionTime = -1;
        attackRunHandler.post(updateAttackStep);
    }

    public enum Type {
        GRADUAL, SUDDEN, DELTA
    }
}
