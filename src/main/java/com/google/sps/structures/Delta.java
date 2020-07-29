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
}