package com.google.sps.servlets;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.protobuf.TextFormat;
import com.google.sps.Validation;
import com.google.sps.proto.SimulationTraceProto.*;
import com.google.sps.results.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.servlet.ServletException;

@WebServlet("/report")
@MultipartConfig()
public class ReportServlet extends HttpServlet {
    private static SimulationTrace simulationTrace;
    private static Validation validation;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String process = request.getParameter("process").toString();
        // GsonBuilder gsonBuilder = new GsonBuilder();
        // Gson gson = gsonBuilder.registerTypeAdapter(CommentObject.class, new CommentAdapter()).create();

        // response.setContentType("application/json;");
        // response.getWriter().println(gson.toJson(comments));

        String json;

        // Or we can just make an object to call the gson on
        if (process.equals("pre")) {
            Query query = new Query("Files").addSort("timestamp", SortDirection.DESCENDING);

            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            PreparedQuery results = datastore.prepare(query);

            Entity retrievedSimulationTrace = results.asIterable().iterator().next();

            simulationTrace =
                SimulationTrace.parseFrom(((Blob) retrievedSimulationTrace.getProperty("simulation-trace")).getBytes());

            validation = new Validation(simulationTrace);

            PreProcessResults preProcessResults = validation.preProcess();

            json = new Gson().toJson(preProcessResults);

            // System.out.println("pre");
            // json += "\"message\": ";
            // json +=  "\"" + "test init" + "\"";
            // json += ", ";
            // json += "\"total\": " + 10000;
        } else {
            // System.out.println("post");
            long start = Long.parseLong(request.getParameter("start"));
            System.out.println("Start is " + start);

            ProcessResults processResults = validation.process(start, start + 1000);

            json = new Gson().toJson(processResults);

            // json += "\"traces\": ";
            // json += "\"" + start + " to " + (start + 1000) + "\"";
            // json += ", ";
            // json += "\"call\": " + start/1000;
        }

        // json += "}";

        response.setContentType("application/json;");
        response.getWriter().println(json);
    }

    // Currently not used
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {        
        String process = request.getParameter("process").toString();

        // if (process.equals("pre")) {
        //     Query query = new Query("Files").addSort("timestamp", SortDirection.DESCENDING);

        //     DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        //     PreparedQuery results = datastore.prepare(query);

        //     Entity retrievedSimulationTrace = results.asIterable().iterator().next();

        //     simulationTrace = 
        //         SimulationTrace.parseFrom(((Blob) retrievedSimulationTrace.getProperty("simulation-trace")).getBytes());

        //     validation = new Validation(simulationTrace);

        //     validation.preProcess();
        // } else {
        //     long start = Long.parseLong(request.getParameter("start"));
        //     validation.process(start, start + 1000);
        // }

        response.sendRedirect("/report.html");
    }
}