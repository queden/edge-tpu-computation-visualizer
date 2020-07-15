package com.google.sps.servlets;

import java.util.Date;

public class LoadFile {
    private long id;
    private String name;
    private String dateTime;
    private String zone;

    public LoadFile(long id, String name, String dateTime, String zone) {
        this.id = id;
        this.name = name;
        this.dateTime = dateTime;
        this.zone = zone;
    }
}