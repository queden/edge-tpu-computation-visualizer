package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory.Builder;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsInputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.protobuf.TextFormat;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.sps.data.*;
import com.google.sps.Validation;
import com.google.sps.proto.MemaccessCheckerDataProto.*;
import com.google.sps.results.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.Channels;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/report")
public class ReportServlet extends HttpServlet {
  private static MemaccessCheckerData memaccessChecker;
  private static Validation validation;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String json = "";
    Gson gson = new Gson();
    
    if (request.getParameter("process").equals("pre")) {
        // Executes the preprocessing of the simulation trace.

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Entity retrievedMemaccessChecker = null;

        // Retrieves the file based on its entity's key, throws an error if the key doesn't exist.
        try {
          Key key = new Builder("File", Long.parseLong(request.getParameter("fileId"))).getKey();
          retrievedMemaccessChecker = datastore.get(key);
        } catch (EntityNotFoundException e) {
          System.out.println("file not found.");
        }

        // Retrive and read file from Cloud Storage.
        GcsService gcsService = GcsServiceFactory.createGcsService();
        GcsFilename fileName = 
            new GcsFilename(
                "trace_info_files", 
                retrievedMemaccessChecker.getProperty("memaccess-checker").toString());

        GcsInputChannel readChannel = gcsService.openReadChannel(fileName, 0);
        InputStream fileInputStream = Channels.newInputStream(readChannel);

        // Parses the retrieved file into a proto message.
        byte[] byteArray = ByteStreams.toByteArray(fileInputStream);
        MemaccessCheckerData memaccessChecker = MemaccessCheckerData.parseFrom(byteArray);

        validation = new Validation(memaccessChecker);

        PreProcessResults preProcessResults = validation.preProcess();

        json = gson.toJson(preProcessResults);
    } else {
        // Executes the trace processing of the memaccess checker.

        long start = Long.parseLong(request.getParameter("start"));
        long step = Long.parseLong(request.getParameter("step-size"));

        ProcessResults processResults = validation.process(start, start + step);
        
        json = gson.toJson(processResults);
    }

    response.setContentType("application/json;");
    response.getWriter().println(json);
  }
}
