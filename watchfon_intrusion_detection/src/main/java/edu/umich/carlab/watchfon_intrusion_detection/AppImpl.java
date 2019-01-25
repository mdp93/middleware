package edu.umich.carlab.watchfon_intrusion_detection;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import edu.umich.carlab.CLDataProvider;
import edu.umich.carlab.DataMarshal;
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


    public AppImpl(CLDataProvider cl, Context context) {
        super(cl, context);

        comparisonGraphs = new HashMap<>();
        for (String sensor : all_sensors) {
            comparisonGraphs.put(sensor, new SensorStream(context));
            subscribe(watchfon_estimates.APP, sensor);
            subscribe(watchfon_spoofed_sensors.APP, sensor);

        }

        name = "WatchFon Intrusion Detection";
    }


    @Override
    public void newData(DataMarshal.DataObject dObject) {
        super.newData(dObject);


        if (!isValidData(dObject)) return;
        if (dObject.device.equals(MiddlewareImpl.APP)) return;

        for (String sensor : all_sensors)
            comparisonGraphs.get(sensor).newData(dObject);

    }

    View initializeComparisonGraph(String sensorName) {
        comparisonGraphs.get(sensorName).addLineGraph(watchfon_spoofed_sensors.APP, sensorName);
        comparisonGraphs.get(sensorName).addLineGraph(watchfon_estimates.APP, sensorName);
        View v = comparisonGraphs.get(sensorName).initializeVisualization(parentActivity);
        v.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 550));
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
        LinearLayout visWrapper = controlWrapper.findViewById(R.id.vis_wrapper);

        for (String sensor : all_sensors)
            visWrapper.addView(initializeComparisonGraph(sensor));

        return controlWrapper;
    }

    @Override
    public void destroyVisualization() {
        for (String sensor : all_sensors)
            comparisonGraphs.get(sensor).destroyVisualization();
    }
}
