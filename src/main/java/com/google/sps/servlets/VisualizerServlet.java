package com.google.sps.servlets;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.protobuf.TextFormat;
import com.google.sps.Validation;
import com.google.sps.proto.SimulationTraceProto.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) 
        throws IOException, ServletException {        
        // Retrieve the uploaded file
        Part filePart = request.getPart("file-input");
        InputStream fileInputStream = filePart.getInputStream();
            
        // Create a simulation trace out of the uploaded file
        InputStreamReader reader = new InputStreamReader(fileInputStream, "ASCII");
        SimulationTrace.Builder builder = SimulationTrace.newBuilder();
        TextFormat.merge(reader, builder);

        SimulationTrace simulationTrace = builder.build();

        // Put the simulation trace proto into datastore
        Date date = new Date();

        Entity simulationTraceUpload = new Entity("File");
        simulationTraceUpload.setProperty("time", date);
        simulationTraceUpload.setProperty("name", filePart.getSubmittedFileName().toString());
        simulationTraceUpload.setProperty("simulation-trace", new Blob(simulationTrace.toByteArray()));

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(simulationTraceUpload);

        response.sendRedirect("/index.html");
    }
}
