package edu.umich.carlab.world_aligned_imu;

import android.content.Context;
import android.hardware.SensorManager;
import android.util.Pair;
import edu.umich.carlab.CLDataProvider;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.loadable.App;
import edu.umich.carlab.sensors.PhoneSensors;


/**
 * This app experiments with vehicle-smartphone axis alignment.
 * It takes the phone's IMU sensors and always points to the
 * vehicle forward direction.
 */
public class VehicleAlignmentApp extends App {
    final String TAG = "VehicleAlignmentApp";

    private DataMarshal.DataObject  lastMagnet,
            lastGravity,
            lastGyro,
            lastAccel;

    private float[] R = new float[9];
    private float[] I = new float[9];
    private float [] lastComputedOrientation = new float[3];
    float [] [] RotMat = new float[3][3];

    public float [] getLastOrientation() {
        return lastComputedOrientation;
    }

    public VehicleAlignmentApp(CLDataProvider cl, Context context) {
        super(cl, context);

        name = "Phone Vehicle Alignment Estimation";
        sensors.add(new Pair<>(PhoneSensors.DEVICE, PhoneSensors.GRAVITY));
        sensors.add(new Pair<>(PhoneSensors.DEVICE, PhoneSensors.MAGNET));
        sensors.add(new Pair<>(PhoneSensors.DEVICE, PhoneSensors.GYRO));
        sensors.add(new Pair<>(PhoneSensors.DEVICE, PhoneSensors.ACCEL));
    }


    @Override
    public void newData(DataMarshal.DataObject dObject) {
        long time = dObject.time;
        String sensor = dObject.sensor;

        if (dObject.dataType != DataMarshal.MessageType.DATA) return;
        if (dObject.device.equals(VehicleAlignmentUtil.APP)) return;
        if (dObject.value == null) return;

        switch (sensor) {
            case PhoneSensors.GRAVITY:
                lastGravity = dObject;
                break;
            case PhoneSensors.MAGNET:
                lastMagnet = dObject;
                break;
            case PhoneSensors.GYRO:
                lastGyro = dObject;
                break;
            case PhoneSensors.ACCEL:
                lastAccel = dObject;
                break;
        }


        if (lastGravity != null && lastMagnet != null) {
            Float[] magnet = lastMagnet.value;
            Float[] gravity = lastGravity.value;
            if (magnet == null || gravity == null) return;

            float [] magnet_r = new float[] { magnet[0], magnet[1], magnet[2] };
            float [] gravity_r = new float[] { gravity[0], gravity[1], gravity[2] };


            boolean success = SensorManager.getRotationMatrix(R, I, gravity_r, magnet_r);

            if (success) {
                SensorManager.getOrientation(R, lastComputedOrientation);

                RotMat[0][0] = R[0];
                RotMat[0][1] = R[1];
                RotMat[0][2] = R[2];
                RotMat[1][0] = R[3];
                RotMat[1][1] = R[4];
                RotMat[1][2] = R[5];
                RotMat[2][0] = R[6];
                RotMat[2][1] = R[7];
                RotMat[2][2] = R[8];


                Float[] casted = new Float[3];
                casted[0] = lastComputedOrientation[0];
                casted[1] = lastComputedOrientation[1];
                casted[2] = lastComputedOrientation[2];
                outputData(dObject, VehicleAlignmentUtil.ORIENT, casted);
            }
        }



        if (lastComputedOrientation != null && lastGyro != null)
            outputData(dObject, VehicleAlignmentUtil.GYRO, MatrixMul(lastGyro.value, RotMat));


        if (lastComputedOrientation != null && lastAccel != null)
            outputData(dObject, VehicleAlignmentUtil.ACCEL, MatrixMul(lastAccel.value, RotMat));
    }

    public Float[] MatrixMul(Float[] T, float[][]RotMat){
        Float[] temp = T.clone();
        for(int i = 0; i < RotMat.length; i++)
            for(int j = 0; j < T.length; j++){
                temp[i] = temp[i] + T[j]*RotMat[j][i];
            }
        return temp;
    }

    void outputData(DataMarshal.DataObject dObject, String sensor, Float[] value) {
        DataMarshal.DataObject secondaryDataObject = dObject.clone();
        secondaryDataObject.device = VehicleAlignmentUtil.APP;
        secondaryDataObject.sensor = sensor;
        secondaryDataObject.value = value;
        cl.newData(secondaryDataObject);
    }
}
