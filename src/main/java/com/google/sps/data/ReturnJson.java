package com.google.sps.data;

import java.util.List;

// Object to send a JSON string of the uploaded files and all uploaded files
public class ReturnJson {
  private FileJson uploadFile;
  private List<LoadFile> files;
  private List<User> users;
  private String currentUser;
  private String zone;

  public ReturnJson(FileJson uploadFile, List<LoadFile> files, List<User> users, String currentUser, String zone, String dateTimeString) {
    this.uploadFile = uploadFile;
    this.files = files;
    this.users = users;
    this.currentUser = currentUser;
    this.zone = zone;

    this.getZone(dateTimeString);
  }

  // Gets the time zone information only
  private void getZone(String dateTimeString) {
    if (zone.equals("-04:00")) {
      zone = "Eastern Daylight Time (EDT)";
    } else if (zone.equals("-09:00")) {
      zone = "Hawaiian Daylight Time (HDT)";
    } else if (zone.equals("-06:00")) {
      zone = "Mountain Daylight Time (MDT)";
    } else if (zone.equals("Z")) {
      zone = "Coordinated Universal Time (UTC)";
    } else {          
      if (zone.equals("-05:00")) {
        zone = " Eastern Standard Time (EST)";
      } else if (zone.equals("-10:00")) {
        zone = " Hawaiian Standard Time (HST)";
      } else if (zone.equals("-07:00")) {
        zone = " Mountain Standard Time (MST)";
      } else if (zone.equals("Z")) {
        zone = "Coordinated Universal Time (UTC)"; 
      } else {
        zone += " (" + dateTimeString + ")";
        zone = zone.replace('_', ' ');
      }
    }
  }
}