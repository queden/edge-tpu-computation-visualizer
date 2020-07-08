package com.google.sps.servlets;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.protobuf.TextFormat;
import com.google.sps.Validation;
import com.google.sps.proto.SimulationTraceProto.*;
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

@WebServlet("/visualizer")
@MultipartConfig()
public class VisualizerServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("query");
        Query query = new Query("Files").addSort("timestamp", SortDirection.DESCENDING);

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        PreparedQuery results = datastore.prepare(query);

        Entity retrievedSimulationTrace = results.asIterable().iterator().next();

        SimulationTrace simulationTrace = 
            SimulationTrace.parseFrom(((Blob) retrievedSimulationTrace.getProperty("simulation-trace")).getBytes());

        // Test if the simulation trace was correctly retrieved
        Validation validation = new Validation(simulationTrace);
        System.out.println(simulationTrace.getNumTiles());

        // GsonBuilder gsonBuilder = new GsonBuilder();
        // Gson gson = gsonBuilder.registerTypeAdapter(CommentObject.class, new CommentAdapter()).create();

        // response.setContentType("application/json;");
        // response.getWriter().println(gson.toJson(comments));

        // TODO: Correct redirect?
        response.sendRedirect("/index.html");
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {        
        // Retrieve the uploaded file
        Part filePart = request.getPart("file-input");
        InputStream fileInputStream = filePart.getInputStream();
            
        // Create a simulation trace out of the uploaded file
        InputStreamReader reader = new InputStreamReader(fileInputStream, "ASCII");
        SimulationTrace.Builder builder = SimulationTrace.newBuilder();
        TextFormat.merge(reader, builder);

        SimulationTrace simulationTrace = builder.build();

        // Put the trace into datastore
        Entity simulationTraceUpload = new Entity("Files");
        simulationTraceUpload.setProperty("timestamp", System.currentTimeMillis());
        simulationTraceUpload.setProperty("simulation-trace", new Blob(simulationTrace.toByteArray()));

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(simulationTraceUpload);

        // Test if the entity was made correctly
        System.out.println(simulationTraceUpload.getProperty("timestamp"));

        response.sendRedirect("/index.html");
    }
}
