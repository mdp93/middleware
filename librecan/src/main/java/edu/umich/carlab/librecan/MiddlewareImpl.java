package edu.umich.carlab.librecan;

import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.loadable.Middleware;

import java.util.HashMap;
import java.util.Map;

public class MiddlewareImpl extends Middleware {
    public final static String APP = "librecan";
    public final static String SPEED = "speed";

    @Override
    public String getName() {
        return APP;
    }
}
