package edu.umich.carlab.watchfon_gear;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.util.Log;
import edu.umich.carlab.CLDataProvider;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlabui.appbases.SensorListAppBase;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;


public class AppImpl extends SensorListAppBase {
    final String TAG = "watchfon_gear";
    final edu.umich.carlab.watchfon_speed.MiddlewareImpl watchfon_speed = new edu.umich.carlab.watchfon_speed.MiddlewareImpl();
    final MiddlewareImpl middleware = new MiddlewareImpl();

    Interpreter tflite;

    // It's a 10-sized one-hot encoding
    float[][] labelProb = new float[1][10];
    float[][] inputBuffer = new float[5][1];
    Object runningPredictionLock = new Object();

    public AppImpl(CLDataProvider cl, Context context) {
        super(cl, context);
        name = "watchfon_gear";
        middlewareName = MiddlewareImpl.APP;
        enableHistoricalLogging = true;

        if (context != null) {
            String vehicleName = middleware.getParameterOrDefault(context, middleware.VEHICLE_NAME, "");

            try {
                // 1. Get model filename by taking MD5 hash
                String hex = new String(Hex.encodeHex(DigestUtils.md5(vehicleName)));
                String modelfilename = String.format("%s.jpg", hex);
                // FIXME We are using a dummy model file for now
                modelfilename = "b0ca7d986ad940d6d5b5001dc0c8dc4e.jpg";
                Log.v(TAG, "Loading model file: " + modelfilename);

                // 2. Load model file using tensorflow lite Interpreter
                AssetManager assetManager = context.getAssets();
                tflite = new Interpreter(loadModelFile(assetManager, modelfilename), null);
            } catch (Exception e) {
                Log.e(TAG, "Unable to find the model file");
            }
        }

        // In kilometers per hour
        subscribe(watchfon_speed.APP, watchfon_speed.SPEED);
    }

    /**
     * Memory-map the model file in Assets.
     */
    private static MappedByteBuffer loadModelFile(AssetManager assets, String modelFilename)
            throws IOException {
        AssetFileDescriptor fileDescriptor = assets.openFd(modelFilename);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    @Override
    public void newData(DataMarshal.DataObject dObject) {
        super.newData(dObject);
        if (dObject.dataType != DataMarshal.MessageType.DATA) return;
        if (dObject.device.equals(MiddlewareImpl.APP)) return;
        if (dObject.value == null) return;

        String dev, sen;
        dev = dObject.device;
        sen = dObject.sensor;
        if (dev.equals(watchfon_speed.APP) && sen.equals(watchfon_speed.SPEED)) {
            // 1. Check if the model is loaded. If not, return. We should have already loaded it.
            if (tflite == null) {
                Log.e(TAG, "Tensor Flow Lite model not initialized");
                return;
            }

            synchronized (runningPredictionLock) {
                // 2. Get the speed samples at the last [-4, -3, -2, -1, 0] seconds (similar to getLatestData(dev, sen))
                DataSample f1 = getDataAt(watchfon_speed.APP, watchfon_speed.SPEED, 4000L);
                DataSample f2 = getDataAt(watchfon_speed.APP, watchfon_speed.SPEED, 3000L);
                DataSample f3 = getDataAt(watchfon_speed.APP, watchfon_speed.SPEED, 2000L);
                DataSample f4 = getDataAt(watchfon_speed.APP, watchfon_speed.SPEED, 1000L);
                DataSample f5 = getDataAt(watchfon_speed.APP, watchfon_speed.SPEED, 0L);

                if (f1 == null || f2 == null || f3 == null || f4 == null || f5 == null) {
                    Log.e(TAG,
                            String.format("Couldn't construct feature vector [%d, %d, %d, %d, %d]",
                                    (f1 == null) ? 0 : 1,
                                    (f2 == null) ? 0 : 1,
                                    (f3 == null) ? 0 : 1,
                                    (f4 == null) ? 0 : 1,
                                    (f5 == null) ? 0 : 1));
                    return;
                }


                /*
                // This should give us 2
                f1.value = 15.24f;
                f2.value = 17.88f;
                f3.value = 18.88f;
                f4.value = 18.39f;
                f5.value = 18.25f;
                */
                inputBuffer[0][0] = f1.value;
                inputBuffer[1][0] = f2.value;
                inputBuffer[2][0] = f3.value;
                inputBuffer[3][0] = f4.value;
                inputBuffer[4][0] = f5.value;

                // 3. Use feature set to make prediction
                tflite.run(inputBuffer, labelProb);

                // 4. Use reverse one-hot encoding to output the gear value
                float gearValue = oneHotDecode(labelProb);
                Log.v(TAG, String.format("[%.02f, %.02f, %.02f, %.02f, %.02f] -> %d",
                        f1.value, f2.value, f3.value, f4.value, f5.value, (int) gearValue));
//                Log.v(TAG, String.format("[%.02f, %.02f, %.02f, %.02f, %.02f] -> [%.04f, %.04f, %.04f, %.04f, %.04f, %.04f, %.04f, %.04f, %.04f, %.04f] -> %d",
//                        f1.value, f2.value, f3.value, f4.value, f5.value,
//                        labelProb[0][0],labelProb[0][1],labelProb[0][2],labelProb[0][3],labelProb[0][4],labelProb[0][5],labelProb[0][6],labelProb[0][7],labelProb[0][8],labelProb[0][9],
//                        (int)gearValue));

                outputData(middleware.APP, middleware.GEAR, (float) gearValue);
            }
        }
    }

    Integer oneHotDecode(float[][] labelProb) {
        float maxVal = labelProb[0][0];
        int maxIdx = 0;
        float val;

        for (int i = 1; i < labelProb[0].length; i++) {
            val = labelProb[0][i];
            if (val > maxVal) {
                maxVal = val;
                maxIdx = i;
            }
        }

        // One hot encoding basically shifts the gear values over.
        return maxIdx - 1;
    }
}
