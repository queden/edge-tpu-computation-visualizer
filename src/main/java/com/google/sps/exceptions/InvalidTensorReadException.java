package com.google.sps.exceptions;

public class InvalidTensorReadException extends Exception {
    private int tensor;
    private String layer;
    private int instructionTag;
    private int tile;
    private int address;
    private int invalidTensor;
    private String memory;

    public InvalidTensorReadException(int tensor, String layer, int instructionTag, int tile, int address, int invalidTensor, String memory) {
        super();
        this.tensor = tensor;
        this.layer = layer;
        this.instructionTag = instructionTag;
        this.tile = tile;
        this.address = address;
        this.invalidTensor = invalidTensor;
        this.memory = memory;
    }

    @Override
    public String getMessage() {
        if (tensor <= 0) {
            return "Trace event corresponding to instruction "
                + instructionTag
                + " tried to read tensor " +
                tensor +
                " at " +
                memory +
                " memory location (layer: "
                + layer
                + ", tile: " +
                tile +
                ", address: " +
                address +
                ") but memory location was not allocated";
        }
        
        return "Trace event corresponding to instruction "
            + instructionTag
            + " tried to read tensor " +
            tensor +
            " at " +
            memory +
            " memory location (layer: "
            + layer
            + " tile: " +
            tile +
            " address: " +
            address +
            ") but was: " +
            invalidTensor +
            ".";
    }
}
