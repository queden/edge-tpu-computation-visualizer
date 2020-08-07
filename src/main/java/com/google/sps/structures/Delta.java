package com.google.sps.structures;

/** Changes to memory locations passed to front end to visualize trace operations */
public class Delta {
  public String layer; // Layer of memory location change 
  public int tile; // Tile of memory location change
  public int memoryAddressChanged; // Memory address changed
  public int tensor; // Label of tensor written

  public Delta(String layer, int tile, int memoryAddressChanged, int tensor) {
    this.layer = layer;
    this.tile = tile;
    this.memoryAddressChanged = memoryAddressChanged;
    this.tensor = tensor;
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof Delta)) {
      return false;
    }
    Delta otherDelta = (Delta) other;
    return layer.equals(otherDelta.layer) 
        && tile == otherDelta.tile
        && memoryAddressChanged == otherDelta.memoryAddressChanged
        && tensor == otherDelta.tensor;
  }

  @Override
  public String toString() {
    return "(layer: "
        + layer
        + ", tile: "
        + tile
        + ", memory address: "
        + memoryAddressChanged
        + ", tensor: "
        + tensor
        + ")";
  }
}