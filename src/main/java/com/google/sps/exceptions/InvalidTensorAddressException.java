package com.google.sps.exceptions;

import com.google.sps.data.*;

public class InvalidTensorAddressException extends Exception {

    private int baseAddress;
    private int instruction;
    private String memoryAccessMessage;

    public InvalidTensorAddressException(int baseAddress, int instruction, AccessTypesEnum memoryAccess) {
        this.baseAddress = baseAddress;
        this.instruction = instruction;
        this.memoryAccessMessage = memoryAccess.toString();
    }

    @Override
    public String getMessage() {
        return "No tensor allocation found at address " + baseAddress + " while validating a " + memoryAccessMessage + "for instruction " + instruction + ".";
    }
}