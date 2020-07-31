package com.google.sps.exceptions;

import com.google.sps.structures.Interval;

public class OverlappingIntervalsException extends Exception {
    private Interval firstInterval;
    private Interval secondInterval;

    public OverlappingIntervalsException(Interval firstInterval, Interval secondInterval) { 
       super();
       this.firstInterval = firstInterval;
       this.secondInterval = secondInterval;
    }

    public Interval getFirstInterval() {
      return firstInterval;
    }

    public Interval getSecondInterval() {
      return secondInterval;
    }

    @Override
    public String getMessage() {
        return "Overlapping intervals";
    }
}
