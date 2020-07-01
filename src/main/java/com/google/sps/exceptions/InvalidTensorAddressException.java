package com.google.sps.exceptions;

/**
 * Exception thrown when a base address operated on by an instruction does not have a tensor
 * allocated to it.
 */
public class InvalidTensorAddressException extends Exception {

  private int baseAddress; // Address operarted on by instruction
  private int instruction; // Tag of instruction performing the operation
  private String memoryAccess; // String representing the access type

  public InvalidTensorAddressException(int baseAddress, int instruction, String memoryAccess) {
    this.baseAddress = baseAddress;
    this.instruction = instruction;
    this.memoryAccess = memoryAccess;
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