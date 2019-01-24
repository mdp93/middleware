package edu.umich.carlab.watchfon_intrusion_detection;

import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.loadable.Middleware;

import java.util.HashMap;
import java.util.Map;

public class MiddlewareImpl extends Middleware {
    public final static String APP = "watchfon_intrusion_detection";
    public final static String ATTACK = "attack_value";

    @Override
    public String getName() {
        return APP;
    }
}
