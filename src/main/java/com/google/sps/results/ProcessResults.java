package com.google.sps.results;

public class ProcessResults {
  public Exception error;
  public String message;
  public int[][] narrow;
  public int[][] wide;

  public ProcessResults(Exception error, int[][] narrow, int[][] wide) {
    this.error = error;
    this.message = error.getMessage();
    System.out.println(message);
    this.narrow = narrow;
    this.wide = wide;
  }
}