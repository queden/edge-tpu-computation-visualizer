package com.google.sps.servlets;

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
        // GsonBuilder gsonBuilder = new GsonBuilder();
        // Gson gson = gsonBuilder.registerTypeAdapter(CommentObject.class, new CommentAdapter()).create();

        // response.setContentType("application/json;");
        // response.getWriter().println(gson.toJson(comments));
        response.sendRedirect("/index.html");
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {        
        Part filePart = request.getPart("file-input");
        InputStream fileInputStream = filePart.getInputStream();

        File saveFile = new File("uploaded-file/" + filePart.getSubmittedFileName());
        // Files.copy(fileInputStream, saveFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        // String fileURL = "http://localhost:8080/uploaded-file/" + filePart.getSubmittedFileName();

        InputStreamReader reader = new InputStreamReader(fileInputStream, "ASCII");
        SimulationTrace.Builder builder = SimulationTrace.newBuilder();
        TextFormat.merge(reader, builder);

        SimulationTrace simulationTrace = builder.build();
    
        Validation validation = new Validation(simulationTrace);
        System.out.println(simulationTrace.getNumTiles());

        response.sendRedirect("/index.html");
    }
}
