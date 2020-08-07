package com.google.sps.results;

import com.google.sps.structures.Delta;
import java.util.List;

/** Response object returned to the front end after processing trace validation's step. */
public class ProcessResults {
  public Exception error; // Exception that occured in processing, null if none
  public Boolean isError; // Whether error occured in processing
  public String message; // Exception's message, empty string if non occured
  public long validationEnd; // Where the last validation ended
  public List<Delta> narrowDeltas; // Changes in narrow memory
  public List<Delta> wideDeltas; // Changes in wide memory

  public ProcessResults(
      Exception error,
      Boolean isError,
      long validationEnd,
      List<Delta> narrowDeltas,
      List<Delta> wideDeltas) {
    this.isError = isError;

    if (isError) {
      this.error = error;
      this.message = error.getMessage();
    } else {
      this.error = null;
      this.message = "";
    }

    this.validationEnd = validationEnd;
    this.narrowDeltas = narrowDeltas;
    this.wideDeltas = wideDeltas;
  }
}