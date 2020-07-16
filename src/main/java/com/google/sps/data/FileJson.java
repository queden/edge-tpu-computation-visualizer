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
  private int narrowBytes;
  private int wideBytes;
  private String time;
  private String dateTimeString;
  private String zone;

    // Master constructor initializing all variables
    public FileJson(
        String fileName, 
        String fileSize, 
        String fileTrace, 
        int fileTiles, 
        int narrowBytes, 
        int wideBytes, 
        String dateTimeString, 
        String zone) {
      this.fileName = fileName;
      this.fileSize = fileSize;
      this.fileTrace = fileTrace;
      this.fileTiles = fileTiles;
      this.narrowBytes = narrowBytes;
      this.wideBytes = wideBytes;
      this.time = "";
      this.dateTimeString = dateTimeString;
      this.zone = zone;
    }

    // Constructor to be used when time information is unknown
    public FileJson(String fileName, 
        String fileSize, 
        String fileTrace, 
        int fileTiles, 
        int narrowBytes, 
        int wideBytes) {
      this(fileName, fileSize, fileTrace, fileTiles, narrowBytes, wideBytes, "", "");
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
          zone);
      this.getTimeZone();
    }

    // Constructor to be used when resetting the file information
    public FileJson() {
      this("null", "null", "null", 0 , 0 , 0);
    }

    // Generates the appropriate time information of the file according to the time zone
  private void getTimeZone() {
    DateTimeFormatter formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;
    ZonedDateTime dateTime = ZonedDateTime.parse(dateTimeString, formatter);
    formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");
    dateTime = dateTime.withZoneSameInstant(ZoneId.of(zone));
    time += dateTime.format(formatter);

    if (zone.equals("-04:00")) {
      time += " EDT";
      zone = "Eastern Daylight Time EDT";
    } else if (zone.equals("-09:00")) {
      time += " HDT";
      zone = "Hawaiian Daylight Time HDT";
    } else if (zone.equals("-06:00")) {
      time += " MDT";
      zone = "Mountain Daylight Time MDT";
    } else {          
      if (zone.equals("-05:00")) {
        time += " EST";
        zone = " Eastern Standard Time EST";
      } else if (zone.equals("-10:00")) {
        time += " HST";
        zone = " Hawaiian Standard Time HST";
      } else if (zone.equals("-07:00")) {
        time += " MST";
        zone = " Mountain Standard Time MDT";
      } else {
        formatter = DateTimeFormatter.ofPattern("z");
        time += " " + dateTime.format(formatter);
        zone += " " + dateTime.format(formatter);
      }         
    }
  }
}