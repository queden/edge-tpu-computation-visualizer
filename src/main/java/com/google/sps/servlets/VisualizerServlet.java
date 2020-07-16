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
import com.google.sps.proto.SimulationTraceProto.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
  // Variable to hold the information about the last uploaded file
  private static FileJson fileJson = new FileJson();

  @Override 
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (request.getParameter("time").equals("false")) {
      Query queryZone = new Query("Zone").addSort("time", SortDirection.DESCENDING);
      Query queryFile = new Query("File").addSort("time", SortDirection.DESCENDING);

      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

      // Uncomment to purge datastore of all entities
      // Query queryZone = new Query("Zone");
      // Query queryFile = new Query("File");
      // for (Entity entity : ((PreparedQuery) datastore.prepare(queryFile)).asIterable()) {
      //     datastore.delete(entity.getKey());
      // }
      // for (Entity entity : ((PreparedQuery) datastore.prepare(queryZone)).asIterable()) {
      //     datastore.delete(entity.getKey());
      // }

      // System.out.println(((PreparedQuery) datastore.prepare(queryZone)).countEntities());
      // System.out.println(((PreparedQuery) datastore.prepare(queryFile)).countEntities());
      //

      // Pulls the last submitted file and time zone
      Entity zoneEntity = ((PreparedQuery) datastore.prepare(queryZone)).asIterator().next();
      Entity fileEntity = ((PreparedQuery) datastore.prepare(queryFile)).asIterator().next();

      String zone = zoneEntity.getProperty("time-zone").toString();
            
      // Appends the correct time zone to the date and time string and retrieves the JSON string 
      // containing the file upload information
      String json = getJson(zone, fileEntity);

      response.setContentType("application/json;");
      response.getWriter().println(json);

    } else {
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

      // Puts the selected time zone into datastore
      String zone = request.getParameter("zone");
      Entity timeEntity = new Entity("Zone");
      timeEntity.setProperty("time-zone", zone);
      timeEntity.setProperty("time", new Date());
      datastore.put(timeEntity);
      }
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) 
    throws IOException, ServletException {        
    // Retrieve the uploaded file
    Part filePart = request.getPart("file-input");
        
    // Will not execute if the user failed to select a file after clicking "upload"
    if (filePart.getSubmittedFileName().length() > 0) {
      InputStream fileInputStream = filePart.getInputStream();
            
      // Create a simulation trace out of the uploaded file
      InputStreamReader reader = new InputStreamReader(fileInputStream, "ASCII");
      SimulationTrace.Builder builder = SimulationTrace.newBuilder();
      TextFormat.merge(reader, builder);

      SimulationTrace simulationTrace = builder.build();

      // Put the simulation trace proto into datastore
      ZonedDateTime dateTime = ZonedDateTime.now(ZoneId.of(ZoneOffset.UTC.getId()));
      DateTimeFormatter formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;

      Entity simulationTraceUpload = new Entity("File");
      simulationTraceUpload.setProperty("date", dateTime.format(formatter));
      simulationTraceUpload.setProperty("time", new Date());
      simulationTraceUpload.setProperty("name", simulationTrace.getName());
      simulationTraceUpload.setProperty(
          "simulation-trace", new Blob(simulationTrace.toByteArray()));

      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      datastore.put(simulationTraceUpload);

      // Updates the last uploaded file information
      String fileName = filePart.getSubmittedFileName();
      String fileSize = getBytes(filePart.getSize());
      String fileTrace = simulationTrace.getName();
      int fileTiles = simulationTrace.getNumTiles();
      int narrowBytes = simulationTrace.getNarrowMemorySizeBytes();
      int wideBytes = simulationTrace.getWideMemorySizeBytes();

      fileJson = new FileJson(fileName, fileSize, fileTrace, fileTiles, narrowBytes, wideBytes);
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
  private static String getJson(String zone, Entity fileEntity) {
    String dateTimeString = fileEntity.getProperty("date").toString();

    fileJson = new FileJson(fileJson, dateTimeString, zone);
    String json = new Gson().toJson(fileJson);

    // Resets the last uploaded file to "null" to help provide feedback to the user
    fileJson = new FileJson();

    return json;
  }
}
