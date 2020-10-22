package Utils;

import com.opencsv.CSVWriter;
import data.Event;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class LogWriter {
    public static void writeSegments(String filePath, Map<Integer, List<Event>> segments){
        System.out.print("\nSaving segmented log... ");
        long startTime = System.currentTimeMillis();
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(filePath),
                    CSVWriter.DEFAULT_SEPARATOR,
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.NO_ESCAPE_CHARACTER,
                    CSVWriter.RFC4180_LINE_END);

            List<Event> events = new ArrayList<>();
            segments.values().forEach(events::addAll);

            String[] headers = Stream.concat(Stream.of("\"caseID\""),
                    extractAttributes(events).stream().map(el -> "\"" + el + "\"")).toArray(String[]::new);
            writer.writeNext(headers);

            StringBuilder row = new StringBuilder();
            System.out.println(row.toString());
            for(var caseID: segments.keySet())
                for(var event: segments.get(caseID)){
                    for (String header : headers) {
                        switch (header) {
                            case "\"caseID\"":
                                row.append("\"").append(caseID).append("\",");
                                break;
                            case "\"timeStamp\"":
                                row.append("\"").append(event.getTimestamp()).append("\",");
                                break;
                            case "\"eventType\"":
                                row.append("\"").append(event.getEventType()).append("\",");
                                break;
                            default:
                                String attribute = header.replaceAll("^\"(.*)\"$", "$1");
                                row.append(event.payload.containsKey(attribute) ? "\"" + event.payload.get(attribute) + "\"," : ",");
                                break;
                        }
                    }
                    row = new StringBuilder(row.substring(0, row.lastIndexOf(",")) + "\n");
                }
            writeActionsValues(writer, row.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        long stopTime = System.currentTimeMillis();
        System.out.println(" (" + (stopTime - startTime) / 1000.0 + " sec)");
    }

    static void writeActionsValues(CSVWriter writer, String data){
        String[] actions = data.split("\n");

        for (String action : actions) {
            String[] actionValues = action.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
            actionValues = Arrays.stream(actionValues)
                    .map(e -> e.replaceAll("\"{2}(([^\"]|\"\")*)\"{2}", "\"\"\"$1\"\"\""))
                    .toArray(String[]::new);
            writer.writeNext(actionValues);
        }
    }

    public static List<String>  extractAttributes(List<Event> events){
        List<String> attributes = new ArrayList<>();
        for(int i = 0; i < events.size(); i++){
            for(String attr: events.get(i).getAttributes())
                if(!attributes.contains(attr))
                    attributes.add(attr);
        }
        return attributes;
    }
}
