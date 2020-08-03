package com.google.sps.results;

import com.google.sps.structures.Delta;
import java.util.List;

public class ProcessResults {
  public Exception error;
  public Boolean isError;
  public String message;
  public long validationEnd;
  public List<Delta> narrowDeltas;
  public List<Delta> wideDeltas;

  public ProcessResults(Exception error, Boolean isError, long validationEnd, List<Delta> narrowDeltas, List<Delta> wideDeltas) {
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