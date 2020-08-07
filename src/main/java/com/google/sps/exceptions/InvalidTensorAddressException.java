package com.google.sps.exceptions;

/**
 * Exception thrown when a base address operated on by an instruction does not have a tensor
 * allocated to it.
 */
public class InvalidTensorAddressException extends Exception {

  private int baseAddress; // Address operarted on by instruction
  private int instruction; // Tag of instruction performing the operation
  private String memoryType; // Narrow or wide memory type

  public InvalidTensorAddressException(int baseAddress, int instruction, boolean isNarrow) {
    this.baseAddress = baseAddress;
    this.instruction = instruction;
    
    if (isNarrow) {
        memoryType = "narrow";
    } else {
        memoryType = "wide";
    }
  }

  /** Outputs information related to the exception. */
  @Override
  public String getMessage() {
    return "No tensor allocation found at address "
        + baseAddress
        + " in "
        + memoryType
        + " memory for instruction "
        + instruction
        + ".";
  }
}