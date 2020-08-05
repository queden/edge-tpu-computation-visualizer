package com.google.sps.structures;

public class Delta {
  public String layer;
  public int tile;
  public int memoryAddressChanged;
  public int tensor;

  public Delta(String layer, int tile, int memoryAddressChanged, int tensor) {
    this.layer = layer;
    this.tile = tile;
    this.memoryAddressChanged = memoryAddressChanged;
    this.tensor = tensor;
  }

  @Override
  public boolean equals(Object other) {
    // No need for null check. The instanceof operator returns false if (other == null).
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