package com.google.sps.data;

import java.util.List;

// Object to send a JSON string of the uploaded files and all uploaded files
public class ReturnJson {
  private FileJson uploadFile;
  private List<LoadFile> files;

  public ReturnJson(FileJson uploadFile, List<LoadFile> files) {
    this.uploadFile = uploadFile;
    this.files = files;
  }
}