package com.google.sps.results;

public class ProcessResults {
  public Exception error;
  public String message;
  public List<Delta> narrowDeltas;
  public List<Delta> wideDeltas;

  public ProcessResults(Exception error, List<Delta> narrowDeltas, List<Delta> wideDeltas) {
    this.error = error;
    this.message = error.getMessage();
    this.narrowDeltas = narrowDeltas;
    this.wideDeltas = wideDeltas;
  }
}