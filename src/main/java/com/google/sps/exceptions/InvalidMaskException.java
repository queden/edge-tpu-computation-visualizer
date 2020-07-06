package com.google.sps.exceptions;

import com.google.sps.proto.SimulationTraceProto.*;

public class InvalidMaskException extends Exception {
    private int instructionTag;
    private String accessType;


    public InvalidMaskException(int instructionTag, TraceEntry.AccessType accessType) {
        super();
        this.instructionTag = instructionTag;
        this.accessType = accessType.getDescriptorForType().getName();
    }

    @Override
    public String getMessage() {
        return "Instruction "
        + instructionTag 
        + " does not have a mask associated with it on accesstype "
        + accessType 
        + ".";
    }
}
