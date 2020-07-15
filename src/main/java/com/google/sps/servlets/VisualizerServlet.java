package com.google.sps.servlets;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.protobuf.TextFormat;
import com.google.sps.proto.SimulationTraceProto.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
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
    private static String name = null;

    @Override 
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String json = "{";

        if (request.getParameter("time").equals("false")) {
            Query queryZone = new Query("Zone").addSort("time", SortDirection.DESCENDING);
            Query queryFile = new Query("File").addSort("time", SortDirection.DESCENDING);

            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

            // Uncomment to purge datastore of all entities
            // Query queryZone = new Query("Zone");
            // Query queryFile = new Query("File");
            // for (Entity entity : ((PreparedQuery) datastore.prepare(queryFile)).asIterable()) {
            //     datastore.delete(entity.getKey());
            // }
            // for (Entity entity : ((PreparedQuery) datastore.prepare(queryZone)).asIterable()) {
            //     datastore.delete(entity.getKey());
            // }

            // System.out.println(((PreparedQuery) datastore.prepare(queryZone)).countEntities());
            // System.out.println(((PreparedQuery) datastore.prepare(queryFile)).countEntities());

            Entity zoneEntity = ((PreparedQuery) datastore.prepare(queryZone)).asIterator().next();
            Entity fileEntity = ((PreparedQuery) datastore.prepare(queryFile)).asIterator().next();

            String zone = zoneEntity.getProperty("time-zone").toString();
            String dateTimeString = fileEntity.getProperty("date").toString();

            String time = "";
            DateTimeFormatter formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;
            ZonedDateTime dateTime = ZonedDateTime.parse(dateTimeString, formatter);
            formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");
            dateTime = dateTime.withZoneSameInstant(ZoneId.of(zone));
            time += dateTime.format(formatter);

            if (zone.equals("-04:00")) {
                time += " EDT";
                zone = "Eastern Daylight Time";
            } else if (zone.equals("-09:00")) {
                time += " HDT";
                zone = "Hawaiian Daylight Time";
            } else if (zone.equals("-06:00")) {
                time += " MDT";
                zone = "Mountain Daylight Time";
            } else {          
                if (zone.equals("-05:00")) {
                    time += " EST";
                    zone = " Eastern Standard Time";
                } else if (zone.equals("-10:00")) {
                    time += " HST";
                    zone = " Hawaiian Standard Time";
                } else if (zone.equals("-07:00")) {
                    time += " MST";
                    zone = " Mountain Standard Time";
                } else {
                    formatter = DateTimeFormatter.ofPattern("z");
                    time += " " + dateTime.format(formatter);
                }         
            }

            json += "\"name\": ";
            json += "\"" + name + "\"";
            json += ", ";
            json += "\"time\": ";
            json += "\"" + time + "\"";
            json += ", ";
            json += "\"zone\": ";
            json += "\"" + zone + "\"";
            json += "}";

            name = null;
        } else {
            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

            String zone = request.getParameter("zone");
            Entity timeEntity = new Entity("Zone");
            timeEntity.setProperty("time-zone", zone);
            timeEntity.setProperty("time", new Date());
            datastore.put(timeEntity);
        }

        response.setContentType("application/json;");
        response.getWriter().println(json);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) 
        throws IOException, ServletException {        
        // Retrieve the uploaded file
        Part filePart = request.getPart("file-input");
        
        if (filePart.getSubmittedFileName().length() > 0) {
            InputStream fileInputStream = filePart.getInputStream();
            
            // Create a simulation trace out of the uploaded file
            InputStreamReader reader = new InputStreamReader(fileInputStream, "ASCII");
            SimulationTrace.Builder builder = SimulationTrace.newBuilder();
            TextFormat.merge(reader, builder);

            SimulationTrace simulationTrace = builder.build();

            // Put the simulation trace proto into datastore
            ZonedDateTime dateTime = ZonedDateTime.now(ZoneId.of(ZoneOffset.UTC.getId()));
            DateTimeFormatter formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;
            DateTimeFormatter testformatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss z");
            // System.out.println(dateTime.format(testformatter));

            Entity simulationTraceUpload = new Entity("File");
            simulationTraceUpload.setProperty("date", dateTime.format(formatter));
            simulationTraceUpload.setProperty("time", new Date());
            simulationTraceUpload.setProperty("name", simulationTrace.getName());
            simulationTraceUpload.setProperty("simulation-trace", new Blob(simulationTrace.toByteArray()));

            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            datastore.put(simulationTraceUpload);

            name = simulationTrace.getName();
            // System.out.println(dateTime.format(formatter));
        }

        response.sendRedirect("/index.html");
    }
}
