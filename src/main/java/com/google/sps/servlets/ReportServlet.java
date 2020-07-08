package com.google.sps.servlets;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
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

@WebServlet("/report")
@MultipartConfig()
public class ReportServlet extends HttpServlet {
    private static final SimulationTrace simulationTrace;
    private static final Validation validation;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // GsonBuilder gsonBuilder = new GsonBuilder();
        // Gson gson = gsonBuilder.registerTypeAdapter(CommentObject.class, new CommentAdapter()).create();

        // response.setContentType("application/json;");
        // response.getWriter().println(gson.toJson(comments));

        Gson gson = new Gson();

        String json = gson.toJson(validation.getNarrowArray()) + gson.toJson(validation.getWideArray()) + gson.toJson(validation.getErrors());
        response.setContentType("application/json;");
        response.getWriter().println(json);
        // response.sendRedirect("/report.html");
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {        
        String process = request.getParameter("process").toString();

        if (process.equals("pre")) {
            Query query = new Query("Files").addSort("timestamp", SortDirection.DESCENDING);

            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            PreparedQuery results = datastore.prepare(query);

            Entity retrievedSimulationTrace = results.asIterable().iterator().next();

            simulationTrace = 
                SimulationTrace.parseFrom(((Blob) retrievedSimulationTrace.getProperty("simulation-trace")).getBytes());

            validation = new Validation(simulationTrace);

            validation.preProcess();
        } else {
            validation.postProcess(Integer.parseInt(request.getParameter("start")));
        }

        response.sendRedirect("/report.html");
    }
}