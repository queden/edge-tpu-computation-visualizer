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
// import com.google.sps.Validation;
import com.google.sps.proto.SimulationTraceProto.*;
import com.google.sps.results.*;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.ArrayList;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/report")
public class ReportServlet extends HttpServlet {
    private static SimulationTrace simulationTrace;
    // private static Validation validation;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) 
        throws IOException {

        String process = request.getParameter("process").toString();
        String json = "";

        if (process.equals("loadfiles")) {
            Query queryZone = new Query("Zone").addSort("time", SortDirection.DESCENDING);
            Query queryFile = new Query("File").addSort("time", SortDirection.DESCENDING);

            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            PreparedQuery fileResults = datastore.prepare(queryFile);

            Entity zoneEntity = ((PreparedQuery) datastore.prepare(queryZone)).asIterator().next();
            String zone = zoneEntity.getProperty("time-zone").toString();

            // Uncomment to purge datastore of all entities
            // for (Entity entity : results.asIterable()) {
            //     datastore.delete(entity.getKey());
            // }

            ArrayList<LoadFile> files = new ArrayList<>();
            String time = "";
            DateTimeFormatter formatter;
            String dateTimeString;
            ZonedDateTime dateTime;

            for (Entity fileEntity : fileResults.asIterable()) {
                dateTimeString = fileEntity.getProperty("date").toString();
                formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;
                dateTime = ZonedDateTime.parse(dateTimeString, formatter);
                formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");

                if (zone.equals("-04:00")) {
                    dateTime = dateTime.withZoneSameInstant(ZoneId.of("-05:00")).minusHours(1); 
                    time += dateTime.format(formatter);
                    time += " EDT";
                    zone = "Eastern Daylight Time";
                } else if (zone.equals("-09:00")) {
                    dateTime = dateTime.withZoneSameInstant(ZoneId.of("-10:00")).minusHours(1);
                    time += dateTime.format(formatter);
                    time += " HDT";
                    zone = "Hawaiian Daylight Time";
                } else if (zone.equals("-06:00")) {
                    dateTime = dateTime.withZoneSameInstant(ZoneId.of("-07:00")).minusHours(1);
                    time += dateTime.format(formatter);
                    time += " MDT";
                    zone = "Mountain Daylight Time";
                } else {          
                    dateTime = dateTime.withZoneSameInstant(ZoneId.of(zone));
                    time += dateTime.format(formatter);

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

                files.add(
                    new LoadFile(
                        fileEntity.getKey().getId(),
                        (String) fileEntity.getProperty("name"), 
                        time,
                        zone));
            }

            json = new Gson().toJson(files);
        } else if (process.equals("pre")) {
            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            Entity retrievedSimulationTrace = null;

            try {
                Key key = new Builder("File", Long.parseLong(request.getParameter("file"))).getKey();
                retrievedSimulationTrace = datastore.get(key);
            } catch (EntityNotFoundException e) {
                System.out.println("file not found.");
            }

            SimulationTrace simulationTrace = 
                SimulationTrace.parseFrom(
                    ((Blob) retrievedSimulationTrace.getProperty("simulation-trace")).getBytes());

            // validation = new Validation(simulationTrace);

            // PreProcessResults preProcessResults = validation.preProcess();
            PreProcessResults preProcessResults = new PreProcessResults(false, "Processed successfully.", 10000);

            json = new Gson().toJson(preProcessResults);
        } else {
            long start = Long.parseLong(request.getParameter("start"));

            System.out.println("Start is " + start);

            // ProcessResults processResults = validation.process(start, start + 1000);
            ProcessResults processResults = new ProcessResults(null, new int[1][2], new int[2][3]);

            json = new Gson().toJson(processResults);
        }

        response.setContentType("application/json;");
        response.getWriter().println(json);
    }
}