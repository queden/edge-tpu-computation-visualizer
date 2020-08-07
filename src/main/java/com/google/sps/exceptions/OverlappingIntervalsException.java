package com.google.sps.exceptions;

import com.google.sps.structures.Interval;

/** Exception for when tensors overlap on one tile within a layer */
public class OverlappingIntervalsException extends Exception {
    private Interval firstInterval; // Tensor allocation that overlaps
    private Interval secondInterval; // Tensor allocation taht overlaps

    public OverlappingIntervalsException(Interval firstInterval, Interval secondInterval) { 
       super();
       this.firstInterval = firstInterval;
       this.secondInterval = secondInterval;
    }

    /**
     * Get first interval that overlaps
     * 
     * @return the first interval that overlaps
     */
    public Interval getFirstInterval() {
      return firstInterval;
    }

    /**
     * Get second interval that overlaps
     * 
     * @return the second interval that overlaps
     */
    public Interval getSecondInterval() {
      return secondInterval;
    }

    @Override
    public String getMessage() {
        return "Overlapping intervals";
    }
}
