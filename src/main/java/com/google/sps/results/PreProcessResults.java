package com.google.sps.results;

public class PreProcessResults {
  public boolean isError;
  public String message;
  public int numTraces;

  public PreProcessResults(boolean isError, String message, int numTraces) {
    this.isError = isError;
    this.message = message;
    this.numTraces = numTraces;
  }
}