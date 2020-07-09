package com.google.sps.results;

public class PreProcessResults {
  public boolean isError;
  public String message;

  public PreProcessResults(boolean isError, String message) {
    this.isError = isError;
    this.message = message;
  }
}