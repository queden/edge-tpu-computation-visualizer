package com.google.sps.exceptions;

/** Exception for when a trace entry performs an invalid read */
public class InvalidTensorReadException extends Exception {
    private int tensor; // Expected tensor
    private String layer; // Layer of trace event
    private int instructionTag; // Trace event's instruction tag
    private int tile; // Tile that it was operating on
    private int address; // Address that trace should operate on
    private int invalidTensor; // Tensor that was found instead
    private String memory; // Wide or narrow memory type
    private long cycle; // Trace event's cycle

    public InvalidTensorReadException(int tensor, String layer, int instructionTag, int tile, int address, int invalidTensor, String memory, long cycle) {
        super();
        this.tensor = tensor;
        this.layer = layer;
        this.instructionTag = instructionTag;
        this.tile = tile;
        this.address = address;
        this.invalidTensor = invalidTensor;
        this.memory = memory;
        this.cycle = cycle;
    }

    @Override
    public String getMessage() {
        if (tensor <= 0) {
            return "Trace event at cycle "
                + cycle
                + " corresponding to instruction "
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
        
        return "Trace event at cycle "
            + cycle
            + " corresponding to instruction "
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
