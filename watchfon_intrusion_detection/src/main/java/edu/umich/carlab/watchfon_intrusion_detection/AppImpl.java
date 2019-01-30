package edu.umich.carlab.watchfon_intrusion_detection;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import edu.umich.carlab.CLDataProvider;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.loadable.App;
import edu.umich.carlab.loadable.Middleware;
import edu.umich.carlab.sensors.PhoneSensors;
import edu.umich.carlabui.appbases.SensorStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AppImpl extends App {
    final String TAG = "wf-ids";
    final int GRID_WIDTH = 480;
    final int GRID_HEIGHT = 500;


    // Sensors estimated by WatchFon
    final edu.umich.carlab.watchfon_estimates.MiddlewareImpl watchfon_estimates =
            new edu.umich.carlab.watchfon_estimates.MiddlewareImpl();

    // Sensors from the vehicle (with optional injection for intrusion detection evaluation)
    final edu.umich.carlab.watchfon_spoofed_sensors.MiddlewareImpl watchfon_spoofed_sensors =
            new edu.umich.carlab.watchfon_spoofed_sensors.MiddlewareImpl();
    final int updateUiInterval = 250;
    String[] all_sensors = {
            watchfon_estimates.SPEED,
            watchfon_estimates.STEERING,
            watchfon_estimates.FUEL,
            watchfon_estimates.ODOMETER,
            watchfon_estimates.GEAR,
            watchfon_estimates.ENGINERPM,

    };
    Map<String, SensorStream> comparisonGraphs;
    Map<String, Button> injectionButtons;

    // Map related data
    MapView mapView;
    GoogleMap googleMap;
    PolylineOptions rectOptions;
    List<LatLng> tripPoints = new ArrayList<>();
    Polyline polyline;
    long lastAdded = 0;
    Map<String, Long> lastUpdatedTime;


    // Error counters
    Map<String, Integer> errorCounters;
    Map<String, Long> lastIntrusionChecked;
    final Long intrusionCheckUpdateInterval = 100L;


    public AppImpl(CLDataProvider cl, Context context) {
        super(cl, context);

        comparisonGraphs = new HashMap<>();
        injectionButtons = new HashMap<>();
        lastUpdatedTime = new HashMap<>();
        errorCounters = new HashMap<>();
        lastIntrusionChecked = new HashMap<>();

        for (String sensor : all_sensors) {
            comparisonGraphs.put(sensor, new SensorStream(context));
            subscribe(watchfon_estimates.APP, sensor);
            subscribe(watchfon_spoofed_sensors.APP, sensor);
            subscribe(PhoneSensors.DEVICE, PhoneSensors.GPS);
            lastUpdatedTime.put(sensor, 0L);
            errorCounters.put(sensor, 0);
            lastIntrusionChecked.put(sensor, 0L);
        }

        name = "WatchFon Intrusion Detection";
    }

    void setInjectionState(final String sensor, final Boolean state) {
        if (parentActivity == null) return;
        parentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                injectionButtons.get(sensor).setPressed(state);
            }
        });
    }

    void setDetectionStateUI(final String sensor, final Boolean state) {
        if (parentActivity == null) return;

        parentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Drawable backgroundColor = (state)
                        ? parentActivity.getDrawable(R.drawable.background_green)
                        : parentActivity.getDrawable(R.drawable.background_gray);


                injectionButtons.get(sensor).setBackground(backgroundColor);
            }
        });
    }

    @Override
    public void newData(final DataMarshal.DataObject dObject) {
        super.newData(dObject);

        if (!isValidData(dObject)) return;
        if (dObject.device.equals(MiddlewareImpl.APP)) return;

        updateCharts(dObject);
        updateMap(dObject);
        updateButtons(dObject);

        checkIntrusion();
    }

    void checkIntrusion() {

        // Should only check for intrusion if we have updated data and we haven't already checked
        Long currTime = System.currentTimeMillis();
        for (String sensor : all_sensors) {
            if (currTime < lastIntrusionChecked.get(sensor) + intrusionCheckUpdateInterval) {
                continue;
            }


            DataMarshal.DataObject latestEstimate = getLatestData(watchfon_estimates.APP, sensor);
            DataMarshal.DataObject latestSpoofed = getLatestData(watchfon_spoofed_sensors.APP, sensor);
            if (latestEstimate != null && latestSpoofed != null) {
                Float estimateSensor = latestEstimate .value[0];
                Map<String, Float> reportedSensorMap = watchfon_spoofed_sensors.splitValues(latestSpoofed);
                Float reportedSensor = reportedSensorMap.get(sensor);

                double difference = Math.abs(estimateSensor - reportedSensor);
                if (difference  > MiddlewareImpl.MAGNITUDES.get(sensor)) {
                    errorCounters.put(sensor, errorCounters.get(sensor) + 1);
                } else {
                    errorCounters.put(sensor, 0);
                }


                // For now, we multiply the "duration" by 10 since the data comes in at 10 Hz anyway.
                boolean fireAlert = (errorCounters.get(sensor) > MiddlewareImpl.DURATIONS.get(sensor) * 10);
                setDetectionStateUI(sensor, fireAlert);
                outputData(MiddlewareImpl.APP, MiddlewareImpl.DETECTION, new Float[] {
                        MiddlewareImpl.ONE_HOT_SENSORS.get(sensor),
                        fireAlert ? 1f : 0f,
                });
            }

            lastIntrusionChecked.put(sensor, currTime);
        }
    }


    void updateCharts(DataMarshal.DataObject dObject) {
        for (String sensor : all_sensors)
            comparisonGraphs.get(sensor).newData(dObject);
    }

    void updateMap(final DataMarshal.DataObject dObject) {
        if (dObject.device.equals(PhoneSensors.DEVICE) && dObject.sensor.equals(PhoneSensors.GPS)) {
            final Map<String, Float> gpsValue = PhoneSensors.splitValues(dObject);
            if (gpsValue != null && googleMap != null) {
                parentActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (dObject.time == lastAdded) return;
                        lastAdded = dObject.time;

                        if (googleMap == null) return;
                        synchronized (googleMap) {
                            LatLng currMarker = new LatLng(
                                    gpsValue.get(PhoneSensors.GPS_LATITUDE),
                                    gpsValue.get(PhoneSensors.GPS_LONGITUDE));
                            if (polyline == null) {
                                rectOptions = new PolylineOptions();
                                rectOptions.color(Color.BLUE);
                                rectOptions.width(12);
                                polyline = googleMap.addPolyline(rectOptions);
                            }
                            tripPoints.add(currMarker);
                            polyline.setPoints(tripPoints);
                            googleMap.moveCamera(CameraUpdateFactory.newLatLng(currMarker));
                        }
                    }
                });
            }
        }
    }

    void updateButtons(DataMarshal.DataObject dObject) {
        Long currTime = System.currentTimeMillis();
        String sensor = dObject.sensor;

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
        v.setLayoutParams(new LinearLayout.LayoutParams(GRID_WIDTH, GRID_HEIGHT));
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

        initializeMapView();
        visWrapper.addView(mapView);


        for (String sensor : all_sensors)
            visWrapper.addView(initializeComparisonGraph(sensor));

        injectionButtons.put(watchfon_estimates.SPEED, (Button) controlWrapper.findViewById(R.id.speed_injection));
        injectionButtons.put(watchfon_estimates.STEERING, (Button) controlWrapper.findViewById(R.id.steer_injection));
        injectionButtons.put(watchfon_estimates.FUEL, (Button) controlWrapper.findViewById(R.id.fuel_injection));
        injectionButtons.put(watchfon_estimates.ODOMETER, (Button) controlWrapper.findViewById(R.id.odometer_injection));
        injectionButtons.put(watchfon_estimates.GEAR, (Button) controlWrapper.findViewById(R.id.gear_injection));
        injectionButtons.put(watchfon_estimates.ENGINERPM, (Button) controlWrapper.findViewById(R.id.rpm_injection));

        return controlWrapper;
    }

    @Override
    public void onResume() {
        if (mapView != null) mapView.onResume();
    }

    @Override
    public void onPause() {
        if (mapView != null) mapView.onPause();
    }

    void initializeMapView() {
        mapView = new MapView(context);
        mapView.setLayoutParams(new LinearLayout.LayoutParams(GRID_WIDTH, GRID_HEIGHT));
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                AppImpl.this.googleMap = googleMap;
                googleMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                                new LatLng(
                                        42.2930283,
                                        -83.7161281),
                                16)
                );
            }
        });

        // The fragment already called onCreate so it's OK to call this now
        mapView.onCreate(new Bundle());
        mapView.onResume();
    }

    @Override
    public void destroyVisualization() {
        for (String sensor : all_sensors)
            comparisonGraphs.get(sensor).destroyVisualization();

        synchronized (googleMap) {
            mapView = null;
            googleMap = null;
        }
    }
}
