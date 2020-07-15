package com.google.sps.exceptions;

import com.google.sps.proto.SimulationTraceProto.*;


public class MemoryAccessException extends Exception {
    private String expectedAccessType;
    private int instructionTag;
  
    public MemoryAccessException(TraceEvent.AccessType expectedAccessType, int instructionTag) {
      this.expectedAccessType = expectedAccessType.getDescriptorForType().getName();
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