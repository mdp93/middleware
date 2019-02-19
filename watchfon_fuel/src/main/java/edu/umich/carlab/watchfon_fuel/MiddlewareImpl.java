package edu.umich.carlab.watchfon_fuel;

import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.loadable.Middleware;

import java.util.HashMap;
import java.util.Map;

public class MiddlewareImpl extends Middleware {
    public final static String APP = "watchfon_fuel";
    public final static String FUEL = "fuel";

    public final static String MAX_FUEL_CAPACITY = "max_fuel_capacity";
    public final static String AVERAGE_MPG = "average_mpg";

    @Override
    public String getName() {
        return APP;
    }
}
