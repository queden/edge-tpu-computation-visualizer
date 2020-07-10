package com.google.sps.servlets;

import com.google.gson.Gson;
import com.google.sps.Validation;
import com.google.sps.proto.SimulationTraceProto.*;
import java.io.IOException;
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
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String process = request.getParameter("process").toString();
        
        String json = "{";

        if (process.equals("pre")) {
            // Substitute with return object's gson.json()
            json += "\"message\": ";
            json +=  "\"" + "test init" + "\"";
            json += ", ";
            json += "\"total\": " + 10000;

            // Actual validation code
            // Query query = new Query("Files").addSort("timestamp", SortDirection.DESCENDING);

            // DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            // PreparedQuery results = datastore.prepare(query);

            // Entity retrievedSimulationTrace = results.asIterable().iterator().next();

            // simulationTrace = 
            //     SimulationTrace.parseFrom(
            //         ((Blob) retrievedSimulationTrace.getProperty("simulation-trace")).getBytes());

            // validation = new Validation(simulationTrace);

            // validation.preProcess();
        } else {
            // Substitute with return object's gson.json()
            long start = Long.parseLong(request.getParameter("start"));

            json += "\"traces\": ";
            json += "\"" + start + " to " + (start + 1000) + ", time: " + System.currentTimeMillis() + "\"";
            json += ", ";
            json += "\"call\": " + start/1000;

            // Actual validation code
            // long start = Long.parseLong(request.getParameter("start"));
            // validation.process(start, start + 1000);
        }

        json += "}";

        Gson gson = new Gson();

        response.setContentType("application/json;");
        // response.getWriter().println(gson.toJson(json));
        response.getWriter().println(json);
    }
}