package edu.umich.carlab.world_aligned_imu;

import android.content.Context;
import android.hardware.SensorManager;
import edu.umich.carlab.CLDataProvider;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.sensors.PhoneSensors;
import edu.umich.carlabui.appbases.SensorListAppBase;


/**
 * This app experiments with vehicle-smartphone axis alignment.
 * It takes the phone's IMU sensors and always points to the
 * vehicle forward direction.
 */
public class AppImpl extends SensorListAppBase {
    final String TAG = "AppImpl";
    float[][] RotMat = new float[3][3];
    private DataMarshal.DataObject lastMagnet,
            lastGravity,
            lastGyro,
            lastAccel;
    private float[] R = new float[9];
    private float[] I = new float[9];
    private float[] lastComputedOrientation = new float[3];

    public AppImpl(CLDataProvider cl, Context context) {
        super(cl, context);

        name = "world_aligned_imu";
        middlewareName = MiddlewareImpl.APP;
        subscribe(PhoneSensors.DEVICE, PhoneSensors.GRAVITY);
        subscribe(PhoneSensors.DEVICE, PhoneSensors.MAGNET);
        subscribe(PhoneSensors.DEVICE, PhoneSensors.GYRO);
        subscribe(PhoneSensors.DEVICE, PhoneSensors.ACCEL);
    }

    public float[] getLastOrientation() {
        return lastComputedOrientation;
    }

    void getRotationMatrix(float[][] rm, Float[] m, Float[] g) {
        // Cross product of magnet and gravity
        float[] c = { m[1]*g[2]-m[2]*g[1] , m[2]*g[0]-m[0]*g[2], m[0]*g[1]-m[1]*g[0]};

        // Divide by norm
        float norm = (float)Math.sqrt(c[0]*c[0] + c[1]*c[1] + c[2]*c[2]);
        c[0] /= norm; c[1] /= norm; c[2] /= norm;
        norm = (float)Math.sqrt(g[0]*g[0] + g[1]*g[1] + g[2]*g[2]);
        g[0] /= norm; g[1] /= norm; g[2] /= norm;

        // Reconstruct the magnet using another cross product
        float[] nm = { g[1]*c[2]-g[2]*c[1], g[2]*c[0]-g[0]*c[2], g[0]*c[1]-g[1]*c[0] };

        // Assign values
        rm[0][0] = c[0]; rm[0][1] = c[1]; rm[0][2] = c[2];
        rm[1][0] = nm[0]; rm[1][1] = nm[1]; rm[1][2] = nm[2];
        rm[2][0] = g[0]; rm[2][1] = g[1]; rm[2][2] = g[2];
    }

    @Override
    public void newData(DataMarshal.DataObject dObject) {
        super.newData(dObject);

        long time = dObject.time;
        String sensor = dObject.sensor;

        if (dObject.dataType != DataMarshal.MessageType.DATA) return;
        if (dObject.device.equals(MiddlewareImpl.APP)) return;
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

            float[] magnet_r = new float[]{magnet[0], magnet[1], magnet[2]};
            float[] gravity_r = new float[]{gravity[0], gravity[1], gravity[2]};


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
                outputData(MiddlewareImpl.APP, dObject, MiddlewareImpl.ORIENT, casted);
            }
        }


        if (lastComputedOrientation != null && lastGyro != null)
            outputData(
                    MiddlewareImpl.APP,
                    dObject,
                    MiddlewareImpl.GYRO,
                    MatrixMul(lastGyro.value, RotMat));


        if (lastComputedOrientation != null && lastAccel != null)
            outputData(
                    MiddlewareImpl.APP,
                    dObject,
                    MiddlewareImpl.ACCEL,
                    MatrixMul(lastAccel.value, RotMat));
    }

    public Float[] MatrixMul(Float[] T, float[][] RotMat) {
        Float[] temp = T.clone();
        for (int i = 0; i < RotMat.length; i++)
            for (int j = 0; j < T.length; j++) {
                temp[i] = temp[i] + T[j] * RotMat[j][i];
            }
        return temp;
    }
}
