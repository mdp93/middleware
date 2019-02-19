package edu.umich.carlab.watchfon_rpm;

import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.loadable.Middleware;

import java.util.HashMap;
import java.util.Map;

public class MiddlewareImpl extends Middleware {
    public final static String APP = "watchfon_rpm";
    public final static String RPM = "rpm";

    public final static String FINAL_DRIVE_RATIO = "final_drive_ratio";
    public final static String TIRE_CIRCUMFERENCE = "tire_circumference";
    public final static String VEHICLE_NAME = "vehicle_name";

    // Gear ratio is special. We have to define a set of gear ratios here and just choose between them
    public static Map<String, Map<Integer, Float>> Gear_Ratio_Sets;

    public final static String ESCAPE = "escape";
    public final static String FIESTA = "fiesta";
    public final static String FOCUS = "focus";
    public final static String MKZ = "mkz";
    public final static String EXPLORER = "explorer";

    static {
        Gear_Ratio_Sets = new HashMap<>();

        // ESCAPE: { -1: 2.943, 1: 4.584, 2: 2.964, 3: 1.912, 4: 1.446, 5: 1.000, 6: 0.746 }
        Gear_Ratio_Sets.put(ESCAPE, new HashMap<Integer, Float>());
        Gear_Ratio_Sets.get(ESCAPE).put(-1, 2.943f);
        Gear_Ratio_Sets.get(ESCAPE).put(1, 4.584f);
        Gear_Ratio_Sets.get(ESCAPE).put(2, 2.964f);
        Gear_Ratio_Sets.get(ESCAPE).put(3, 1.912f);
        Gear_Ratio_Sets.get(ESCAPE).put(4, 1.446f);
        Gear_Ratio_Sets.get(ESCAPE).put(5, 1.000f);
        Gear_Ratio_Sets.get(ESCAPE).put(6, 0.746f);

        // FIESTA: { -1: 3.507, 1: 3.917, 2: 2.429, 3: 1.436, 4: 1.021, 5: 0.867, 6: 0.7 }, # We confirmed there are 6 gears
        Gear_Ratio_Sets.put(FIESTA, new HashMap<Integer, Float>());
        Gear_Ratio_Sets.get(FIESTA).put(-1, 3.507f);
        Gear_Ratio_Sets.get(FIESTA).put(1, 3.917f);
        Gear_Ratio_Sets.get(FIESTA).put(2, 2.429f);
        Gear_Ratio_Sets.get(FIESTA).put(3, 1.436f);
        Gear_Ratio_Sets.get(FIESTA).put(4, 1.021f);
        Gear_Ratio_Sets.get(FIESTA).put(5, 0.867f);
        Gear_Ratio_Sets.get(FIESTA).put(6, 0.7f);

        // FOCUS: { -1: 3.82, 1: 3.73, 2: 2.05, 3: 1.36, 4: 1.03, 5: 0.82, 6: 0.69 },
        Gear_Ratio_Sets.put(FOCUS, new HashMap<Integer, Float>());
        Gear_Ratio_Sets.get(FOCUS).put(-1, 3.82f);
        Gear_Ratio_Sets.get(FOCUS).put(1, 3.73f);
        Gear_Ratio_Sets.get(FOCUS).put(2, 2.05f);
        Gear_Ratio_Sets.get(FOCUS).put(3, 1.36f);
        Gear_Ratio_Sets.get(FOCUS).put(4, 1.03f);
        Gear_Ratio_Sets.get(FOCUS).put(5, 0.82f);
        Gear_Ratio_Sets.get(FOCUS).put(6, 0.69f);

        // MKZ: { -1: 2.943, 1: 4.584, 2: 2.964, 3: 1.912, 4: 1.446, 5: 1.000, 6: 0.746 },
        Gear_Ratio_Sets.put(MKZ, new HashMap<Integer, Float>());
        Gear_Ratio_Sets.get(MKZ).put(-1, 2.943f);
        Gear_Ratio_Sets.get(MKZ).put(1, 4.584f);
        Gear_Ratio_Sets.get(MKZ).put(2, 2.964f);
        Gear_Ratio_Sets.get(MKZ).put(3, 1.912f);
        Gear_Ratio_Sets.get(MKZ).put(4, 1.446f);
        Gear_Ratio_Sets.get(MKZ).put(5, 1.000f);
        Gear_Ratio_Sets.get(MKZ).put(6, 0.746f);

        // EXPLORER: { -1: 2.882, 1: 4.484, 2: 2.872, 3: 1.842, 4: 1.414, 5: 1, 6: 0.742 },
        Gear_Ratio_Sets.put(EXPLORER, new HashMap<Integer, Float>());
        Gear_Ratio_Sets.get(EXPLORER).put(-1, 2.882f);
        Gear_Ratio_Sets.get(EXPLORER).put(1, 4.484f);
        Gear_Ratio_Sets.get(EXPLORER).put(2, 2.872f);
        Gear_Ratio_Sets.get(EXPLORER).put(3, 1.842f);
        Gear_Ratio_Sets.get(EXPLORER).put(4, 1.414f);
        Gear_Ratio_Sets.get(EXPLORER).put(5, 1.000f);
        Gear_Ratio_Sets.get(EXPLORER).put(6, 0.742f);
    }

    @Override
    public String getName() {
        return APP;
    }
}
