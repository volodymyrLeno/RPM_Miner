package data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Event {
    public Map<String, String> payload;
    public Map<String, String> context;
    private int eid;
    private String caseID;
    private String eventType;
    private String timestamp;
    private List<String> attributes;
    private boolean start;
    private boolean end;

    public Event(List<String> attributes, String[] values, int eid) {
        this.start = false;
        this.end = false;
        this.eid = eid;
        String temp;
        this.attributes = new ArrayList<>(attributes);
        this.caseID = attributes.contains("caseID") ? values[attributes.indexOf("caseID")] : "";
        this.eventType = values[attributes.indexOf("eventType")];
        this.timestamp = values[attributes.indexOf("timeStamp")];
        payload = new HashMap<>();
        for (int i = 0; i < values.length; i++) {
            if (values[i].matches("\"+"))
                temp = "";
            else
                temp = values[i].replaceAll("\"{4}", "\"\"").replaceAll("\"([^;\"\\[\\]]+)\"", "$1").
                        replaceAll("^\"(.*)\"$", "$1");

            if (i != attributes.indexOf("eventType") && i != attributes.indexOf("timeStamp") && i != attributes.indexOf("caseID")) {
                if ((!temp.equals("\"\"") && !temp.equals("")) || (i == attributes.indexOf("target.value")
                        && (this.eventType.equals("clickTextField") ||
                        this.eventType.equals("editField") || this.eventType.equals("getCell") || this.eventType.equals("editRange"))))
                    payload.put(attributes.get(i), temp);
            }
        }

        /* Handling Excel events */

        if (payload.containsKey("targetApp") && payload.get("targetApp").contains("Excel")) {
            if (payload.containsKey("target.id")) {
                payload.put("target.row", payload.get("target.id").replaceAll("[A-Za-z]+", ""));
                payload.put("target.column", payload.get("target.id").replaceAll("\\d+", ""));
            }
        }
    }

    public Event(String activityName, String timestamp) {
        this.caseID = "";
        this.eventType = activityName;
        this.timestamp = timestamp;
        payload = new HashMap<>();
        this.start = false;
        this.end = false;
    }

    public Event(Event event) {
        this.caseID = event.caseID;
        this.eventType = event.eventType;
        this.timestamp = event.timestamp;
        this.payload = new HashMap<>(event.payload);
        this.start = false;
        this.end = false;
    }

    public Event(String timestamp, String eventType, HashMap<String, String> payload, int eid) {
        this.caseID = "";
        this.eventType = eventType;
        this.timestamp = timestamp;
        this.payload = new HashMap<>(payload);
        this.eid = eid;
        this.start = false;
        this.end = false;
        this.attributes = new ArrayList<>(payload.keySet());
    }

    public void removeAttribute(String attribute) {
        this.attributes.remove(attribute);
    }

    public String getApplication() {
        if (this.payload.containsKey("targetApp"))
            return this.payload.get("targetApp");
        else if (this.payload.containsKey("source"))
            return this.payload.get("source");
        else
            return "";
    }

    public String getEventType() {
        return this.eventType;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public String getCaseID() {
        return this.caseID;
    }

    public void setCaseID(String caseID) {
        this.caseID = caseID;
    }

    public List<String> getAttributes() {
        return this.attributes;
    }

    public int getID() {
        return eid;
    }

    public Map<String, String> getContext() {
        return this.context;
    }

    public Map<String, String> getPayload() {
        return this.payload;
    }

    public String getEventNameAndContext() {
        String output = this.eventType;
        if (getContext().size() > 0) {
            for (String attr : this.context.keySet())
                output += "+" + this.context.get(attr);
        } else {
            output += "+";
        }
        return output;
    }

    public boolean isStart() {
        return start;
    }

    public void setStart(boolean start) {
        this.start = start;
    }

    public boolean isEnd() {
        return end;
    }

    public void setEnd(boolean end) {
        this.end = end;
    }

    public String toString() {
        return "({" + this.caseID + "}, " + this.eid + ", " + this.eventType + ", " + this.timestamp + ", " + payload + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Event && obj != null) return this.eid == ((Event) obj).eid;
        return false;
    }
}
