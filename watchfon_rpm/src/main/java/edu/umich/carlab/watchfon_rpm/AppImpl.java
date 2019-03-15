package edu.umich.carlab.watchfon_rpm;

import android.content.Context;
import edu.umich.carlab.CLDataProvider;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlabui.appbases.SensorListAppBase;

import java.util.HashMap;
import java.util.Map;


public class AppImpl extends SensorListAppBase {
    final String TAG = "watchfon_rpm";
    final edu.umich.carlab.watchfon_speed.MiddlewareImpl watchfon_speed = new edu.umich.carlab.watchfon_speed.MiddlewareImpl();
    final edu.umich.carlab.watchfon_gear.MiddlewareImpl watchfon_gear = new edu.umich.carlab.watchfon_gear.MiddlewareImpl();
    final double INCHES_TO_METERS = 0.0254;
    final double IDLE_RPM = 800;
    final double TIRE_DIAM = (245 / 1000.0 * 45 / 100.0) * 2 + (18 * INCHES_TO_METERS);
    Integer lastGear = null;
    Float lastSpeed = null;
    Double rpm = 0.0;
    Map<Integer, Float> Gear_Ratio;
    float FINAL_DRIVE_RATIO = 3.36f;
    float TIRE_CIRCUM = (float) (TIRE_DIAM * Math.PI / 1000.0);

    MiddlewareImpl middleware = new MiddlewareImpl();

    public AppImpl(CLDataProvider cl, Context context) {
        super(cl, context);

        Gear_Ratio = new HashMap<>();
        Gear_Ratio.put(-1, 2.882f);
        Gear_Ratio.put(1, 4.584f);
        Gear_Ratio.put(2, 2.964f);
        Gear_Ratio.put(3, 1.912f);
        Gear_Ratio.put(4, 1.446f);
        Gear_Ratio.put(5, 1.0f);
        Gear_Ratio.put(6, 0.742f);

        name = "watchfon_rpm";
        middlewareName = MiddlewareImpl.APP;

        if (context != null) {
            TIRE_CIRCUM = middleware.getParameterOrDefault(context, middleware.TIRE_CIRCUMFERENCE, TIRE_CIRCUM);
            FINAL_DRIVE_RATIO = middleware.getParameterOrDefault(
                    context,
                    middleware.FINAL_DRIVE_RATIO,
                    FINAL_DRIVE_RATIO);
            String VEHICLE_NAME = middleware.getParameterOrDefault(context, middleware.VEHICLE_NAME, "");
            if (middleware.Gear_Ratio_Sets.containsKey(VEHICLE_NAME))
                Gear_Ratio = middleware.Gear_Ratio_Sets.get(VEHICLE_NAME);

        }

        subscribe(watchfon_gear.APP, watchfon_gear.GEAR);
        subscribe(watchfon_speed.APP, watchfon_speed.SPEED);
    }


    @Override
    public void newData(DataMarshal.DataObject dObject) {
        super.newData(dObject);

        if (dObject.dataType != DataMarshal.MessageType.DATA) return;
        if (dObject.device.equals(MiddlewareImpl.APP)) return;
        if (dObject.value == null) return;

        startClock();
        if (
                dObject.device.equals(watchfon_gear.APP)
                        && dObject.sensor.equals(watchfon_gear.GEAR)
                )
            lastGear = Math.round(dObject.value[0]);

        if (
                dObject.device.equals(watchfon_speed.APP)
                        && dObject.sensor.equals(watchfon_speed.SPEED)
                )
            lastSpeed = dObject.value[0];

        if (lastGear != null && lastSpeed != null) {
            if (!Gear_Ratio.containsKey(lastGear)) return;
            rpm = (FINAL_DRIVE_RATIO * Gear_Ratio.get(lastGear)) / TIRE_CIRCUM / 60.0;
            if (rpm < IDLE_RPM) rpm = IDLE_RPM;
            outputData(MiddlewareImpl.APP, dObject, MiddlewareImpl.RPM, rpm.floatValue());

            // We might not always "endClock()" but that's OK because we just start
            // clock the next time around and override the clock value
            endClock();
        }

    }
}
