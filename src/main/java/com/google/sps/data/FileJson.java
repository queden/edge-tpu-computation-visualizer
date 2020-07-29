package com.google.sps.data;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

// Object to hold the user feedback information about a file after upload
public class FileJson {
  private String fileName;
  private String fileSize;
  private String fileTrace;
  private int fileTiles;
  private String narrowBytes;
  private String wideBytes;
  private String time;
  private String dateTimeString;
  private String zone;
  private String uploadUser;

  // Master constructor initializing all variables
  public FileJson(
      String fileName, 
      String fileSize, 
      String fileTrace, 
      int fileTiles, 
      String narrowBytes, 
      String wideBytes, 
      String dateTimeString, 
      String zone,
      String uploadUser) {

    this.fileName = fileName;
    this.fileSize = fileSize;
    this.fileTrace = fileTrace;
    this.fileTiles = fileTiles;
    this.narrowBytes = narrowBytes;
    this.wideBytes = wideBytes;
    this.time = "";
    this.dateTimeString = dateTimeString;
    this.zone = zone;
    this.uploadUser = uploadUser;
  }

  // Constructor to be used when time information is unknown
  public FileJson(
      String fileName, 
      String fileSize, 
      String fileTrace, 
      int fileTiles, 
      String narrowBytes, 
      String wideBytes,
      String uploadUser) {

  this(fileName, fileSize, fileTrace, fileTiles, narrowBytes, wideBytes, "", "", uploadUser);
  }

  // Final constructor to be used combining file and time information
  public FileJson(FileJson fileJson, String dateTimeString, String zone) {
    this(
        fileJson.fileName, 
        fileJson.fileSize, 
        fileJson.fileTrace, 
        fileJson.fileTiles, 
        fileJson.narrowBytes, 
        fileJson.wideBytes, 
        dateTimeString, 
        zone,
        fileJson.uploadUser);

    this.getTime();
  }

  // Constructor to be used when a file has not been uploaded but the time zone information is 
  // necessary
  public FileJson(String dateTimeString, String zone) {
    this("null", "null", "null", 0 , "null", "null", "", zone, "");
  }

  // Constructor to be used when resetting the file information
  public FileJson() {
    this("null", "null", "null", 0 , "null" , "null", "", "", "");
  }

  // Generates the appropriate time information of the file according to the time zone
  private void getTime() {
    DateTimeFormatter formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;
    ZonedDateTime dateTime = ZonedDateTime.parse(dateTimeString, formatter);
    formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");
    dateTime = dateTime.withZoneSameInstant(ZoneId.of(zone));
    time += dateTime.format(formatter);

    if (zone.equals("-04:00")) {
      time += " EDT";
    } else if (zone.equals("-09:00")) {
      time += " HDT";
    } else if (zone.equals("-06:00")) {
      time += " MDT";
    } else if (zone.equals("Z")) {
      time += " UTC";
    } else {          
      if (zone.equals("-05:00")) {
        time += " EST";
      } else if (zone.equals("-10:00")) {
        time += " HST";
      } else if (zone.equals("-07:00")) {
        time += " MST";
      } else if (zone.equals("Z")) {
        time += " UTC";
      } else {
        formatter = DateTimeFormatter.ofPattern("z");
        time += " " + dateTime.format(formatter);
      }         
    }
  }
}