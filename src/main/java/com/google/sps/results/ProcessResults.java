package com.google.sps.results;

public class ProcessResults {
  public Exception error;
  public int[][] narrow;
  public int[][] wide;

  public ProcessResults(Exception error, int[][] narrow, int[][] wide) {
    this.error = error;
    this.narrow = narrow;
    this.wide = wide;
  }
}