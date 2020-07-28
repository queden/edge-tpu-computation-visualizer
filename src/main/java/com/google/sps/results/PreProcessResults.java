package com.google.sps.results;

public class PreProcessResults {
  public boolean isError;
  public String message;
  public int numTraces;
  public int numTiles;
  public int narrowSize;
  public int wideSize;

  public PreProcessResults(boolean isError, String message, int numTraces, int numTiles, int narrowSize, int wideSize) {
    this.isError = isError;
    this.message = message;
    this.numTraces = numTraces;
    this.numTiles = numTiles;
    this.narrowSize = narrowSize;
    this.wideSize = wideSize;
  }
}