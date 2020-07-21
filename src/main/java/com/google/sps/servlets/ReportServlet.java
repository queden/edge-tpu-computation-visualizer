package com.google.sps.servlets;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory.Builder;
import com.google.gson.Gson;
// import com.google.sps.Validation;
import com.google.sps.proto.SimulationTraceProto.*;
import com.google.sps.results.*;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/report")
public class ReportServlet extends HttpServlet {
  private static MemaccessCheckerData simulationTrace;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String json = "";
    
    if (request.getParameter("process").equals("pre")) {
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

        // System.out.println("Start is " + start);

        // ProcessResults processResults = validation.process(start, start + 1000);
        // Placeholder
        ProcessResults processResults = new ProcessResults(null, new int[1][2], new int[2][3]);

        json = new Gson().toJson(processResults);
      }

      response.setContentType("application/json;");
      response.getWriter().println(json);
    }
  }