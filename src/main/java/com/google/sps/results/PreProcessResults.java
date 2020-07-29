package com.google.sps.results;

import com.google.sps.proto.MemaccessCheckerDataProto.*;

public class PreProcessResults {
  public boolean isError;
  public String message;
  public int numTraces;
  public int numTiles;
  public int narrowSize;
  public int wideSize;
  List<TensorLayerAllocationTable> tensorAllocationsNarrow;
  List<TensorLayerAllocationTable> tensorAllocationWide;

  public PreProcessResults(boolean isError, String message, int numTraces, int numTiles, int narrowSize, int wideSize, List<TensorLayerAllocationTable> tensorAllocationsNarrow, List<TensorLayerAllocationTable> tensorAllocationWide) {
    this.isError = isError;
    this.message = message;
    this.numTraces = numTraces;
    this.numTiles = numTiles;
    this.narrowSize = narrowSize;
    this.wideSize = wideSize;
    this.tensorAllocationNarrow = tensorAllocationNarrow;
    this.tensorAllocationWide = tensorAllocationWide;
  }
}