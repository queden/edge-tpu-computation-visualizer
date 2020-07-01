package com.google.sps.exceptions;

import com.google.sps.proto.SimulationTraceProto.*;

public class InvalidTensorWriteException extends Exception {
    private int instructionTag;
    private int address;
    private String accessType;

    public InvalidTensorWriteException(int instructionTag, int address, TraceEntry.AccessType accessType) {
        super();
        this.instructionTag = instructionTag;
        this.address = address;
        this.accessType = accessType.getDescriptorForType().getName();
    }

    @Override
    public String getMessage() {
        return "Instruction "
        + instructionTag 
        + " does not have a tensor associated with "
        + accessType 
        + " on address "
        + address
        + ".";
    }
}
