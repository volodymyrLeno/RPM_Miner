package Utils;

import Segmentation.Data.Node;
import data.Event;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static Utils.Utils.*;

public class Parser {
    public static List<String> toSequence(List<Event> events, List<String> contextAttributes){
        List<String> sequence = new ArrayList<>();
        HashMap<String, List<Event>> groupedEvents = groupByEventType(events);
        for(var group: groupedEvents.keySet())
            setContextAttributes(groupedEvents.get(group), contextAttributes);
        for(var event: events)
            sequence.add(new Node(event.getEventType(), event.context, 1).toString());
        return sequence;
    }

    public static List<String> toSequence(List<Event> events){
        List<String> sequence = new ArrayList<>();
        for(var event: events)
            sequence.add(new Node(event.getEventType(), event.context, 1).toString());
        return sequence;
    }

    public static List<List<String>> toSequences(HashMap<Integer, List<Event>> cases, List<String> contextAttributes){
        List<List<String>> sequences = new ArrayList<>();

        List<Event> events = new ArrayList<>();
        cases.values().forEach(events::addAll);

        HashMap<String, List<Event>> groupedEvents = groupEvents(events);
        for(var group: groupedEvents.keySet())
            setContextAttributes(groupedEvents.get(group), contextAttributes);
        for(var caseID: cases.keySet()){
            List<String> sequence = new ArrayList<>();
            for(var event: cases.get(caseID))
                sequence.add(new Node(event.getEventType(), event.context, 1).toString());
            sequences.add(sequence);
        }
        return sequences;
    }

    public static List<String> extractContextAttributes(String filePath){
        List<String> contextAttributes = new ArrayList<>();
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader(filePath));
            JSONObject jsonObject = (JSONObject) obj;
            JSONArray context = (JSONArray) jsonObject.get("context");

            if (context != null) {
                for (int i = 0; i < context.size(); i++){
                    contextAttributes.add(context.get(i).toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return contextAttributes;
    }

    public static void setContextAttributes(List<Event> events, List<String> contextAttributes){
        for(var event: events){
            HashMap<String, String> context = new HashMap<>();
            for(var attribute: event.payload.keySet())
                if(contextAttributes.contains(attribute)) {
                    if (attribute.equals("target.id") && event.getApplication().equals("Excel")) {
                        var uniqueColumns = events.stream().map(el -> el.payload.get("target.column")).distinct().collect(Collectors.toList());
                        var uniqueRows = events.stream().map(el -> el.payload.get("target.row")).distinct().collect(Collectors.toList());
                        if (uniqueColumns.size() < uniqueRows.size())
                            attribute = "target.column";
                        else
                            attribute = "target.row";
                    }
                    context.put(attribute, event.payload.get(attribute));
                }
            event.context = new HashMap<>(context);
        }
    }
}
