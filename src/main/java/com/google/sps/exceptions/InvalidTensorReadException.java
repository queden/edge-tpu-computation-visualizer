package com.google.sps.exceptions;

import com.google.sps.data.*;

public class InvalidTensorReadException extends Exception {
    private int tensor;
    private int tile;
    private int address;
    private int invalidTensor;
    private String memory;

    public InvalidTensorReadException(int tensor, int tile, int address, int invalidTensor, String memory) {
        super();
        this.tensor = tensor;
        this.tile = tile;
        this.address = address;
        this.invalidTensor = invalidTensor;
        this.memory = memory;
    }

    @Override
    public String getMessage() {
        if (tensor <= 0) {
            return "Tried to read tensor: " +
                tensor +
                " at " +
                memory +
                "memory location (tile: " +
                tile +
                " address: " +
                address +
                ") but memory location was not allocated";
        }
        
        return "Tried to read tensor: " +
            tensor +
            " at " +
            memory +
            "memory location (tile: " +
            tile +
            " address: " +
            address +
            ") but was: " +
            invalidTensor +
            ".";
    }
}
