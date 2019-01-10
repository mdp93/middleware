package edu.umich.carlab.world_aligned_imu;

import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.loadable.Middleware;

import java.util.HashMap;
import java.util.Map;

public class MiddlewareImpl extends Middleware {
    public final static String APP = "world_aligned_imu";
    public final static String ACCEL = "accel";
    public final static String GYRO = "gyro";
    public final static String ORIENT = "orient";


    /* Split into these */
    public final static String ACCEL_X = "accel_x";
    public final static String ACCEL_Y = "accel_y";
    public final static String ACCEL_Z = "accel_z";
    public final static String GYRO_X = "gyro_x";
    public final static String GYRO_Y = "gyro_y";
    public final static String GYRO_Z = "gyro_z";
    public final static String ORIENT_AZIMUTH = "orient_azimuth";
    public final static String ORIENT_PITCH = "orient_pitch";
    public final static String ORIENT_ROLL = "orient_roll";

    @Override
    public String getName() {
        return APP;
    }

    /**
     * This function splits an grouped together sensor source to it's complementary parts
     * @param dataObject
     * @return
     */
    public Map<String, Float> splitValues(DataMarshal.DataObject dataObject) {
        Map<String, Float> splitMap = new HashMap<>();
        String device = dataObject.device;

        if (!device.equals(APP)) return null;

        String sensor = dataObject.sensor;
        Float [] values;

        // This happens for status messages
        // We still want to split and broadcast, though
        if (dataObject.value.length == 1) {
            values = new Float[] {
                    dataObject.value[0],
                    dataObject.value[0],
                    dataObject.value[0]
            };
        } else {
            values = dataObject.value;
        }


        switch (sensor) {
            case ACCEL:
                splitMap.put(ACCEL_X, values[0]);
                splitMap.put(ACCEL_Y, values[1]);
                splitMap.put(ACCEL_Z, values[2]);
                break;
            case GYRO:
                splitMap.put(GYRO_X, values[0]);
                splitMap.put(GYRO_Y, values[1]);
                splitMap.put(GYRO_Z, values[2]);
                break;
            case ORIENT:
                splitMap.put(ORIENT_AZIMUTH, values[0]);
                splitMap.put(ORIENT_PITCH, values[1]);
                splitMap.put(ORIENT_ROLL, values[2]);
                break;
        }
        return splitMap;
    }
}
