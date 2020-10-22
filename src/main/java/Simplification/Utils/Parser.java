package Simplification.Utils;

import data.Event;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;

public class Parser {
    public static String toMXML(HashMap<Integer, List<Event>> cases){
        String sr = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<!-- This file has been generated with the OpenXES library. It conforms -->\n" +
            "<!-- to the legacy MXML standard for log storage and management. -->\n" +
            "<!-- OpenXES library version: 1.0RC7 -->\n" +
            "<!-- OpenXES is available from http://www.xes-standard.org/ -->";

        sr += "<WorkflowLog>\n\t<Source program=\"XES MXML serialization\" openxes.version=\"1.0RC7\"/>\n";
        sr += "\t<Process id=\"process1\" description=\"process with id process1\">\n" +
            "\t\t<Data>\n\t\t\t<attribute name=\"concept:name\">process1</attribute>\n" +
            "\t\t\t<attribute name=\"time:timestamp\">1970-01-01T00:00:00</attribute>\n\t\t</Data>\n";

        for(var caseID: cases.keySet()){
            sr += "\t\t<ProcessInstance id=\"" + caseID + "\" description=\"instance with id " + caseID + "\">\n";
			sr += "\t\t\t<Data>\n\t\t\t\t<attribute name=\"concept:name\">" + caseID + "</attribute>\n\t\t\t</Data>\n";
            for(var event: cases.get(caseID)){
                sr += "\t\t\t<AuditTrailEntry>\n";
                sr += "\t\t\t\t<Data>\n";
                sr += "\t\t\t\t\t<attribute name=\"concept:name\">" + event.getEventType() + "</attribute>\n";
                sr += "\t\t\t\t\t<attribute name=\"time:timestamp\">" + event.getTimestamp() + "</attribute>\n";
                sr += "\t\t\t\t\t<attribute name=\"lifecycle:transition\">complete</attribute>\n";
                for(var attribute: event.payload.keySet())
                    sr += "\t\t\t\t\t<attribute name=\"" + attribute + "\">" + event.payload.get(attribute) + "</attribute>\n";
                sr += "\t\t\t\t</Data>\n";
                sr += "\t\t\t\t<WorkflowModelElement>" + event.getEventType() + "</WorkflowModelElement>\n";
				sr += "\t\t\t\t<EventType>complete</EventType>\n";
				sr += "\t\t\t\t<timestamp>" + event.getTimestamp() + "</timestamp>\n";
                sr += "\t\t\t</AuditTrailEntry>\n";
            }
            sr += "\t\t</ProcessInstance>\n";
        }
        return sr;
    }

    public static void saveIntoMXML(String fileName, HashMap<Integer, List<Event>> cases){
        var mxmlRepresentation = toMXML(cases);
        var mxmlLog = fileName.substring(0, fileName.lastIndexOf('.')) + ".txt";

        try{
            FileWriter myWriter = new FileWriter(mxmlLog);
            myWriter.write(mxmlRepresentation);
            myWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}
