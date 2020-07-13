package com.google.sps.servlets;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory.Builder;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.gson.Gson;
import com.google.sps.Validation;
import com.google.sps.proto.SimulationTraceProto.*;
import java.io.IOException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/report")
public class ReportServlet extends HttpServlet {
    private static SimulationTrace simulationTrace;
    private static Validation validation;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) 
        throws IOException {

        String process = request.getParameter("process").toString();
        String json = "{";

        if (process.equals("loadfiles")) {
            Query query = new Query("File").addSort("time", SortDirection.DESCENDING);

            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            PreparedQuery results = datastore.prepare(query);

            // Uncomment to purge datastore of all entities
            // for (Entity entity : results.asIterable()) {
            //     datastore.delete(entity.getKey());
            // }

            ArrayList<LoadFile> files = new ArrayList<>();
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

            for (Entity entity : results.asIterable()) {
                files.add(
                    new LoadFile(
                        entity.getKey().getId(),
                        (String) entity.getProperty("name"),
                        SimulationTrace.parseFrom(
                            ((Blob) entity.getProperty("simulation-trace")).getBytes()), 
                        formatter.format((Date) entity.getProperty("time"))));
            }

            json = new Gson().toJson(files);
        } else if (process.equals("pre")) {
            
            // Substitute with return object's gson.json()

            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            Entity retrievedSimulationTrace = null;
            // PreparedQuery results = datastore.prepare(query);

            try {
                Key key = new Builder("File", Long.parseLong(request.getParameter("file"))).getKey();
                retrievedSimulationTrace = datastore.get(key);
            } catch (EntityNotFoundException e) {
                System.out.println("file not found.");
            }

            SimulationTrace simulationTrace = 
                SimulationTrace.parseFrom(
                    ((Blob) retrievedSimulationTrace.getProperty("simulation-trace")).getBytes());

            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            String date = formatter.format((Date) retrievedSimulationTrace.getProperty("time"));

            json += "\"message\": ";
            json +=  "\"" + date + "\"";
            json += ", ";
            json += "\"total\": " + 10000;
            json += "}";

            // validation = new Validation(simulationTrace);

            // validation.preProcess();
        } else {
            // Substitute with return object's gson.json()
            long start = Long.parseLong(request.getParameter("start"));

            json += "\"traces\": ";
            json += "\"" + start + " to " + (start + 1000) + ", time: " + System.currentTimeMillis() + "\"";
            json += ", ";
            json += "\"call\": " + start/1000;
            json += "}";

            // Actual validation code
            // long start = Long.parseLong(request.getParameter("start"));
            // validation.process(start, start + 1000);
        }

        Gson gson = new Gson();

        response.setContentType("application/json;");
        // response.getWriter().println(gson.toJson(json));
        response.getWriter().println(json);
    }
}