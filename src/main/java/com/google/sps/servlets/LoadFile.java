package com.google.sps.servlets;

import com.google.appengine.api.datastore.Key;
import com.google.sps.proto.SimulationTraceProto.*;
import java.util.Date;

public class LoadFile {
    private long id;
    private String name;
    private SimulationTrace simulationTrace;
    private String date;

    public LoadFile(long id, String name, SimulationTrace simulationTrace, String date) {
        this.id = id;
        this.name = name;
        this.simulationTrace = simulationTrace;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public SimulationTrace getSimulationTrace() {
        return simulationTrace;
    }

    public String getDate() {
        return date;
    }
}