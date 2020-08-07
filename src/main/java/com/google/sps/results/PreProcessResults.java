package com.google.sps.results;

import com.google.sps.proto.MemaccessCheckerDataProto.*;
import java.util.List;

/** Response object returned to the front end after preprocesing step. */
public class PreProcessResults {
  public boolean isError; // Boolean of if exception occurred during preprocessing
  public String message; // Message describing result of preprocessing (exception or success)
  public int numTraces; // Number of traces present in protobuf
  public int numTiles; // Number of tiles present in protobuf
  public int narrowSize; // Size of protobuf's narrow memory
  public int wideSize; // Size of protobuf's wide memory
  List<TensorLayerAllocationTable> tensorAllocationNarrow; // Tensors allocated in Narrow Memory
  List<TensorLayerAllocationTable> tensorAllocationWide; // Tensors allocated in Wide Memory

  public PreProcessResults(
      boolean isError,
      String message,
      int numTraces,
      int numTiles,
      int narrowSize,
      int wideSize,
      List<TensorLayerAllocationTable> tensorAllocationNarrow,
      List<TensorLayerAllocationTable> tensorAllocationWide) {
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