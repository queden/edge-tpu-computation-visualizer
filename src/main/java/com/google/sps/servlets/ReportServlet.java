package com.google.sps.servlets;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory.Builder;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import com.google.sps.data.*;
// import com.google.sps.Validation;
import com.google.sps.proto.MemaccessCheckerDataProto.*;
import com.google.sps.results.*;
import java.io.IOException;
import java.time.ZoneOffset;
import java.util.ArrayList;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/report")
public class ReportServlet extends HttpServlet {
  private static MemaccessCheckerData memaccessCheckerData;
  // private static Validation validation;
  private static String user = "All";
  private static String timeZone = ZoneOffset.UTC.getId();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (request.getParameter("time").equals("true")) {
      // Updates the time zone
      String zone = request.getParameter("zone");
      timeZone = zone;
    } else {
      String process = request.getParameter("process");
      String json = "";

      if (process.equals("loadfiles")) {
        // Loads the files into the drop down menu

        if (request.getParameter("user").equals("false")) {
          // Does NOT update the current user

          boolean userFilesExist = true;

          Query queryUser = new Query("User").addSort("time", SortDirection.DESCENDING);
          Query queryFile;

          DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

          // Filters files to display based on current user
          if (user.equals("All")) {
            queryFile = new Query("File").addSort("time", SortDirection.DESCENDING);
          } else {
            // Filters files according to current user

            Filter propertyFilter = new FilterPredicate("user", FilterOperator.EQUAL, user);

            queryFile = 
                new Query("File")
                    .setFilter(propertyFilter)
                    .addSort("time", SortDirection.DESCENDING);

            if (((PreparedQuery) datastore.prepare(queryFile)).countEntities() == 0) {
              // Uses default "All" users option if current user has not uploaded files under their
              // name

              queryFile = new Query("File").addSort("time", SortDirection.DESCENDING);
              userFilesExist = false;
            }
          }        

          PreparedQuery fileResults = datastore.prepare(queryFile);
          PreparedQuery userResults = datastore.prepare(queryUser);

          // Assembles a collection of all known users
          
          ArrayList<User> users = new ArrayList<>();

          for (Entity entity : userResults.asIterable()) {
              users.add(
                  new User(
                      entity.getKey().getId(),
                      (String) entity.getProperty("user-name")
                  ));
          }

          ArrayList<LoadFile> files = new ArrayList<>();
          String dateTimeString;

          // Creates a collection of LoadFile objects with the proper information about their storage
          for (Entity fileEntity : fileResults.asIterable()) {
            dateTimeString = fileEntity.getProperty("date").toString();

            files.add(
                new LoadFile(
                    fileEntity.getKey().getId(),
                    (String) fileEntity.getProperty("name"),
                    dateTimeString,
                    timeZone,
                    users,
                    user,
                    userFilesExist));
          }

          json = new Gson().toJson(files);
        } else {
          // Updates the current user

          String name = request.getParameter("user-name");
          user = name;
        }
      } else if (process.equals("pre")) {
        // Executes the preprocessing of the simulation trace

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Entity retrievedSimulationTrace = null;

        // Retrieves the file based on its entity's key, throws an error if the key doesn't exist
        try {
          Key key = new Builder("File", Long.parseLong(request.getParameter("fileId"))).getKey();
          retrievedSimulationTrace = datastore.get(key);
        } catch (EntityNotFoundException e) {
          System.out.println("file not found.");
        }

        // Parses the simulation trace out of the respective entity's Blob in datastore
        MemaccessCheckerData simulationTrace = 
            MemaccessCheckerData.parseFrom(
                ((Blob) retrievedSimulationTrace.getProperty("simulation-trace")).getBytes());

        // validation = new Validation(simulationTrace);

        // PreProcessResults preProcessResults = validation.preProcess();
        PreProcessResults preProcessResults = 
            new PreProcessResults(false, "Processed successfully.", 10000);

        json = new Gson().toJson(preProcessResults);
      } else {
        // Executes the trace processing of the simulation trace
        long start = Long.parseLong(request.getParameter("start"));

        System.out.println("Start is " + start);

        // ProcessResults processResults = validation.process(start, start + 1000);
        // Placeholder
        ProcessResults processResults = new ProcessResults(null, new int[1][2], new int[2][3]);

        json = new Gson().toJson(processResults);
      }

      response.setContentType("application/json;");
      response.getWriter().println(json);
    }
  }
}