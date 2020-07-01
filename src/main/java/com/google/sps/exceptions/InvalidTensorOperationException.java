package com.google.sps.exceptions;

import com.google.sps.proto.SimulationTraceProto.*;


public class InvalidTensorOperationException extends Exception {
    private int expectedTensor;
    private int actualTensor;
    private int instruction; // Tag of instruction performing the operation
    private String memoryAccess; // String representing the access type
  
    public InvalidTensorOperationException(int expectedTensor, int actualTensor, int instruction, TraceEntry.AccessType memoryAccess) {
      this.expectedTensor = expectedTensoor;
      this.instruction = instruction;
      this.memoryAccess = memoryAccess.getDescriptorForType().getName();
    }
  
    /** Outputs information related to the exception. */
    @Override
    public String getMessage() {
      return "No tensor allocation found at address "
          + baseAddress
          + " while validating a "
          + memoryAccess
          + "for instruction "
          + instruction
          + ".";
    }
}