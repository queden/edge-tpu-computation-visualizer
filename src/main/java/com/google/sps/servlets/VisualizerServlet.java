package com.google.sps.servlets;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import com.google.protobuf.TextFormat;
import com.google.sps.data.*;
import com.google.sps.proto.SimulationTraceProto.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

@WebServlet("/visualizer")
@MultipartConfig()
public class VisualizerServlet extends HttpServlet {
  // Variables to hold the information about the last uploaded file, time zone, and current user
  private static String timeZone = ZoneOffset.UTC.getId();
  private static String user = "All";
  private static FileJson fileJson = new FileJson();
  private static Entity fileEntity = new Entity("File");

  @Override 
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (request.getParameter("time").equals("false")) {
      // Does NOT update the time zone

      if (request.getParameter("user").equals("false")) {
        // purgeAll(); /* Uncomment to purge datasore (BE CAREFUL)

        // Does NOT update the current user

        Query queryFile = new Query("File").addSort("time", SortDirection.DESCENDING);
        Query queryUser = new Query("User").addSort("time", SortDirection.DESCENDING);

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        PreparedQuery userResults = datastore.prepare(queryUser);
        ArrayList<User> users = new ArrayList<>();

        // Assembles a list of all known users
        for (Entity entity : userResults.asIterable()) {
          users.add(new User(entity.getKey().getId(), (String) entity.getProperty("user-name")));
        }
              
        // Appends the correct time zone to the date and time string and retrieves the JSON string 
        // containing the file upload information

        String json = getJson(timeZone, fileEntity, users);
        response.setContentType("application/json;");
        response.getWriter().println(json);
        //*/
      } else {
        // Updates the current user

        String name = request.getParameter("user-name");
        user = name;    

        // Adds the new user into datastore
        if (request.getParameter("new").equals("true")) {
          DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

          // Puts the entered user into datastore
          Entity userEntity = new Entity("User");
          userEntity.setProperty("user-name", user);
          userEntity.setProperty("time", new Date());
          datastore.put(userEntity);
        }
      }

      
    } else {
      // Updates the selected time zone
      String zone = request.getParameter("zone");

      timeZone = zone; 
    }
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) 
    throws IOException, ServletException {
    String upload = request.getParameter("upload");

    if (upload.equals("true")) {
      // Retrieve the uploaded file
      Part filePart = request.getPart("file-input");
          
      // Will not execute if the user failed to select a file after clicking "upload"
      if (filePart.getSubmittedFileName().length() > 0) {
        InputStream fileInputStream = filePart.getInputStream();
              
        // Create a simulation trace out of the uploaded file
        InputStreamReader reader = new InputStreamReader(fileInputStream, "ASCII");
        MemaccessCheckerData.Builder builder = MemaccessCheckerData.newBuilder();
        TextFormat.merge(reader, builder);

        MemaccessCheckerData simulationTrace = builder.build();

        // Put the simulation trace proto into datastore
        ZonedDateTime dateTime = ZonedDateTime.now(ZoneId.of(ZoneOffset.UTC.getId()));
        DateTimeFormatter formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;

        Entity simulationTraceUpload = new Entity("File");
        simulationTraceUpload.setProperty("date", dateTime.format(formatter));
        simulationTraceUpload.setProperty("time", new Date());
        simulationTraceUpload.setProperty(
            "name", 
            (simulationTrace.getName().equals("")) 
                ? filePart.getSubmittedFileName() 
                : simulationTrace.getName());
        simulationTraceUpload.setProperty("user", user);
        simulationTraceUpload.setProperty(
            "simulation-trace", new Blob(simulationTrace.toByteArray()));        

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(simulationTraceUpload);
        
        // Updates the last submitted file
        fileEntity = simulationTraceUpload;

        // Holds the last uploaded file information
        String fileName = filePart.getSubmittedFileName();
        String fileSize = getBytes(filePart.getSize());
        String fileTrace = simulationTrace.getName();
        int fileTiles = 
            simulationTrace.getNumTiles().equals("") 
                ? "No name provided" 
                : simulationTrace.getNumTiles();
        int narrowBytes = simulationTrace.getNarrowMemorySizeBytes();
        int wideBytes = simulationTrace.getWideMemorySizeBytes();

        fileJson =
            new FileJson(fileName, fileSize, fileTrace, fileTiles, narrowBytes, wideBytes, user);
      } else {
        // Resets the last uploaded file to "null" to help provide feedback to the user

        fileJson = new FileJson();
        fileEntity = new Entity("File");
      }
    }        

    response.sendRedirect("/index.html");
  }

  // Function to retrieve the file size information in terms of Bytes/KB/MB/GB
  private static String getBytes(long size) {
    double bytes = (double) size;
    String result = "";

    if (bytes < Math.pow(1024, 1)) {
      result += String.format("%.2f", bytes) + " Bytes";
    } else if (bytes < Math.pow(1024, 2)) {
      bytes = (double) bytes / Math.pow(1024, 1);
      result += String.format("%.2f", bytes) + " KB";
    } else if (bytes < Math.pow(1024, 3)) {
      bytes = (double) bytes / Math.pow(1024, 2);
      result += String.format("%.2f", bytes) + " MB";
    } else {
      bytes = (double) bytes / Math.pow(1024, 3);
      result += String.format("%.2f", bytes) + " GB";
    }

    return result;
  }

  // Determines the appropriate file information to be displayed on the page in the form of a JSON 
  // string
  private static String getJson(String zone, Entity fileEntity, ArrayList<User> users) {
    String dateTimeString = (String) fileEntity.getProperty("date");

    if (dateTimeString == null) {
      // Sends the time zone information only

      ZonedDateTime dateTime = ZonedDateTime.now(ZoneId.of(zone));
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("z");

      fileJson = new FileJson(dateTime.format(formatter), zone, users, user);
    } else {
      // Sends both file and time information

      fileJson = new FileJson(fileJson, dateTimeString, zone, users, user);
    }

    String json = new Gson().toJson(fileJson);

    return json;
  }

  // Clear datastore
  private static void purgeAll() {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Query queryZone = new Query("Zone");
    Query queryFile = new Query("File");
    Query queryUser = new Query("User");

    for (Entity entity : ((PreparedQuery) datastore.prepare(queryFile)).asIterable()) {
        datastore.delete(entity.getKey());
    }
    for (Entity entity : ((PreparedQuery) datastore.prepare(queryZone)).asIterable()) {
        datastore.delete(entity.getKey());
    }
    for (Entity entity : ((PreparedQuery) datastore.prepare(queryUser)).asIterable()) {
        datastore.delete(entity.getKey());
    }
    

    // Check if purge/submit actually happened
    
    System.out.println(((PreparedQuery) datastore.prepare(queryFile)).countEntities());
    System.out.println(((PreparedQuery) datastore.prepare(queryUser)).countEntities());
  }
}
