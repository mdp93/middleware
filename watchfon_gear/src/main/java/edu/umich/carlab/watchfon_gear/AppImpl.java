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
    final edu.umich.carlab.world_aligned_imu.MiddlewareImpl world_aligned_imu = new edu.umich.carlab.world_aligned_imu.MiddlewareImpl();
    final edu.umich.carlab.watchfon_speed.MiddlewareImpl watchfon_speed = new edu.umich.carlab.watchfon_speed.MiddlewareImpl();
    final MiddlewareImpl middleware = new MiddlewareImpl();

    Interpreter tflite;


    public AppImpl(CLDataProvider cl, Context context) {
        super(cl, context);
        name = "watchfon_gear";
        middlewareName = MiddlewareImpl.APP;

        if (context != null) {
            String vehicleName = middleware.getParameterOrDefault(context, middleware.VEHICLE_NAME, "");

            try {
                // 1. Get model filename by taking MD5 hash
                String hex = new String(Hex.encodeHex(DigestUtils.md5(vehicleName)));
                String modelfilename = String.format("%s.jpg", hex);

                // 2. Load model file using tensorflow lite Interpreter
                AssetManager assetManager = context.getAssets();
                tflite = new Interpreter(loadModelFile(assetManager, modelfilename));
            } catch (Exception e) {
                Log.e(TAG, "Unable to find the model file");
            }
        }

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

        // TODO. For now we just output a placeholder.
        outputData(MiddlewareImpl.APP, dObject, MiddlewareImpl.GEAR, 1f);

        // TODO. Since the data comes at a fast rate, we can't overload the prediction algorithm. Check if there is an ongoing prediction and return early if there is.

        // 1. Check if the model is loaded. If not, return. We should have already loaded it.
        // 2. Get the speed samples at the last [-4, -3, -2, -1, 0] seconds (similar to getLatestData(dev, sen))
            /*
            In general, this API can be:

            getDataAt(dev, sen, -3.5):
                It returns the data point that is closest to that time offset from now.
                It returns the actual data (float value) and the time offset to that time (as close as it can)

            getEstimateDataAt(dev, sen, -3.5):
                Returns data point interpolated to that exact time
                Returns distance to the nearest data point (a proxy for confidence in our return value)
            */
        // 3. Use feature set to make prediction
    }
}
