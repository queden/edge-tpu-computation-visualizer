package com.google.sps.exceptions;

import com.google.sps.proto.MemaccessCheckerDataProto.*;

/** Exception for when trace entry has a certain memory access unreciprocated by its instruction */
public class MemoryAccessException extends Exception {
    private String expectedAccessType; // Expected access type of the trace
    private int instructionTag; // Instruction without access type
  
    public MemoryAccessException(TraceEvent.AccessType expectedAccessType, int instructionTag) {
      this.expectedAccessType = expectedAccessType.getValueDescriptor().getName();
      this.instructionTag = instructionTag;
    }
  
    /** Outputs information related to the exception. */
    @Override
    public String getMessage() {
      return "Instruction "
        + instructionTag
        + " does not have a  "
        + expectedAccessType 
        + " memory access as expected by trace entry.";
    }
}