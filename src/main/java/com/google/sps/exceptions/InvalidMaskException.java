package com.google.sps.exceptions;

import com.google.sps.proto.MemaccessCheckerDataProto.*;

/** Exception thrown if instruction does not have a mask associated with it */
public class InvalidMaskException extends Exception {
    private int instructionTag; // Instruction without mask
    private String accessType; // Trace related to instruction


    public InvalidMaskException(int instructionTag, TraceEvent.AccessType accessType) {
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
