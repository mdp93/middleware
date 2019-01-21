package edu.umich.librecan_middleware;

import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.loadable.Middleware;

import java.util.HashMap;
import java.util.Map;

public class MiddlewareImpl extends Middleware {
    public final static String APP = "librecan_middleware";

    @Override
    public String getName() {
        return APP;
    }
}
