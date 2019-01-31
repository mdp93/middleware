package edu.umich.carlab.watchfon_spoofed_sensors;

import android.app.Activity;
import android.os.Handler;
import android.widget.ProgressBar;

import static edu.umich.carlab.watchfon_spoofed_sensors.MiddlewareImpl.*;

public class Attack {
    Handler attackRunHandler;
    int attackStage = 0;
    int currentValue = 0;


    Activity activity;
    ProgressBar attackProgress;
    Type attackType;
    String attackSensor;

    /*
        <item>Delta, Speed, +10 kmph</item>
        <item>Gradual, Speed, 4 kmph</item>
        <item>Gradual, Steering, 10 deg.</item>
        <item>Sudden, Speed, 4 kmph</item>
        <item>Sudden, Steering, -10 deg.</item>
     */

    Runnable updateAttackStep = new Runnable() {
        @Override
        public void run() {
            attackStage += 1;

            if (attackProgress != null)
                attackProgress.setProgress(attackStage);

            if (attackStage < 30)
                attackRunHandler.postDelayed(
                        updateAttackStep,
                        1000);
        }
    };


    public enum Type {
        GRADUAL, SUDDEN, DELTA
    }


    public Attack (Activity activity) {
        this.activity = activity;
    }

    public Attack (Activity activity, ProgressBar progressBar) {
        this.activity = activity;
        this.attackProgress = progressBar;


        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                attackRunHandler = new Handler();
            }
        });

    }

    public void runAttack(int attackSelectionId) {
        switch (attackSelectionId) {
            case 0:
                attackType = Type.DELTA;
                attackSensor = SPEED;
                break;
            case 1:
                attackType = Type.GRADUAL;
                attackSensor = SPEED;
                break;
            case 2:
                attackType = Type.GRADUAL;
                attackSensor = STEERING;
                break;
            case 3:
                attackType = Type.SUDDEN;
                attackSensor = SPEED;
                break;
            case 4:
                attackType = Type.SUDDEN;
                attackSensor = STEERING;
                break;
        }


        if (attackProgress != null)
            attackProgress.setMax(30);
        attackStage = 0;
        attackRunHandler.post(updateAttackStep);
    }
}
