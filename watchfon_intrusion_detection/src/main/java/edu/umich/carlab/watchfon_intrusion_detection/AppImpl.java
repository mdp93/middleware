package edu.umich.carlab.watchfon_intrusion_detection;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import edu.umich.carlab.CLDataProvider;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.hal.HardwareAbstractionLayer;
import edu.umich.carlab.loadable.App;
import edu.umich.carlab.loadable.Middleware;
import edu.umich.carlabui.appbases.SensorListAppBase;
import edu.umich.carlabui.appbases.SensorStream;
import edu.umich.carlabui.appbases.SensorStreamAppBase;

import java.util.HashMap;
import java.util.Map;


public class AppImpl extends App {
    final String TAG = "watchfon_intrusion_detection";



    // Sensors estimated by WatchFon
    final edu.umich.carlab.watchfon_estimates.MiddlewareImpl watchfon_estimates =
            new edu.umich.carlab.watchfon_estimates.MiddlewareImpl();

    // Sensors from the vehicle (with optional injection for intrusion detection evaluation)
    final edu.umich.carlab.watchfon_spoofed_sensors.MiddlewareImpl watchfon_spoofed_sensors =
            new edu.umich.carlab.watchfon_spoofed_sensors.MiddlewareImpl();



    String [] all_sensors = {
            watchfon_estimates.SPEED,
            watchfon_estimates.STEERING,
            watchfon_estimates.FUEL,
            watchfon_estimates.ODOMETER,
            watchfon_estimates.GEAR,
            watchfon_estimates.ENGINERPM,

    };

    Map<String, SensorStream> comparisonGraphs;
    Map<String, Button> injectionButtons;

    final int updateUiInterval = 250;
    Map<String, Long> lastUpdatedTime;

    public AppImpl(CLDataProvider cl, Context context) {
        super(cl, context);

        comparisonGraphs = new HashMap<>();
        injectionButtons = new HashMap<>();
        lastUpdatedTime = new HashMap<>();

        for (String sensor : all_sensors) {
            comparisonGraphs.put(sensor, new SensorStream(context));
            subscribe(watchfon_estimates.APP, sensor);
            subscribe(watchfon_spoofed_sensors.APP, sensor);
            lastUpdatedTime.put(sensor, 0L);
        }

        name = "WatchFon Intrusion Detection";
    }

    void setInjectionState(final String sensor, final Boolean state) {
        parentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                injectionButtons.get(sensor).setPressed(state);
            }
        });
    }

    @Override
    public void newData(DataMarshal.DataObject dObject) {
        super.newData(dObject);


        if (!isValidData(dObject)) return;
        if (dObject.device.equals(MiddlewareImpl.APP)) return;

        for (String sensor : all_sensors)
            comparisonGraphs.get(sensor).newData(dObject);

        String sensor = dObject.sensor;

        // UI updates
        Long currTime = System.currentTimeMillis();
        if (!injectionButtons.containsKey(sensor)) return;
        if (currTime < lastUpdatedTime.get(sensor) + updateUiInterval) return;


        // If injection is happening, then depress that button
        if (dObject.device.equals(watchfon_spoofed_sensors.APP)) {
            Map<String, Float> splitData = watchfon_spoofed_sensors.splitValues(dObject);
            if (splitData.get(watchfon_spoofed_sensors.INJECTION_MAGNITUDE) != 0) {
                setInjectionState(sensor, true);
            } else {
                setInjectionState(sensor, false);
            }

            lastUpdatedTime.put(sensor, currTime);
        }

    }

    View initializeComparisonGraph(String sensorName) {
        comparisonGraphs.get(sensorName).addLineGraph(watchfon_spoofed_sensors.APP, sensorName);
        comparisonGraphs.get(sensorName).addLineGraph(watchfon_estimates.APP, sensorName);
        View v = comparisonGraphs.get(sensorName).initializeVisualization(parentActivity);
        v.setLayoutParams(new LinearLayout.LayoutParams(330, 300));
        return v;
    }

    @Override
    public View initializeVisualization(Activity parentActivity) {
        super.initializeVisualization(parentActivity);

        LayoutInflater inflater = parentActivity.getLayoutInflater();
        View controlWrapper = inflater.inflate(R.layout.watchfon_control, null);
        Button attackTriggerButton = controlWrapper.findViewById(R.id.trigger_attack_button);
        attackTriggerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DataMarshal.DataObject d = new DataMarshal.DataObject();
                d.time = System.currentTimeMillis();
                d.device = MiddlewareImpl.APP;
                d.sensor = MiddlewareImpl.ATTACK;
                d.dataType = DataMarshal.MessageType.DATA;
                outputData(MiddlewareImpl.APP, d, MiddlewareImpl.ATTACK, 1.0f);
            }
        });
        GridLayout visWrapper = controlWrapper.findViewById(R.id.vis_wrapper);

        for (String sensor : all_sensors)
            visWrapper.addView(initializeComparisonGraph(sensor));

        injectionButtons.put(watchfon_estimates.SPEED, (Button)controlWrapper.findViewById(R.id.speed_injection));
        injectionButtons.put(watchfon_estimates.STEERING, (Button)controlWrapper.findViewById(R.id.steer_injection));
        injectionButtons.put(watchfon_estimates.FUEL, (Button)controlWrapper.findViewById(R.id.fuel_injection));
        injectionButtons.put(watchfon_estimates.ODOMETER, (Button)controlWrapper.findViewById(R.id.odometer_injection));
        injectionButtons.put(watchfon_estimates.GEAR, (Button)controlWrapper.findViewById(R.id.gear_injection));
        injectionButtons.put(watchfon_estimates.ENGINERPM, (Button)controlWrapper.findViewById(R.id.rpm_injection));

        return controlWrapper;
    }

    @Override
    public void destroyVisualization() {
        for (String sensor : all_sensors)
            comparisonGraphs.get(sensor).destroyVisualization();
    }
}
