package Segmentation.Data;

import data.Event;

import java.util.*;

public class Node {
    private String eventType;
    private Map<String, String> context;
    private Integer frequency;

    public Node(){
        this.eventType = "ENTRY";
        this.context = new HashMap<>();
        this.frequency = 1;
    }

    public Node(Event event){
        this.eventType = event.getEventType();
        this.context = new HashMap<>(event.context);
        this.frequency = 1;
    }

    public Node(String eventType, Map<String, String> context, Integer frequency){
        this.eventType = eventType;
        this.context = new HashMap<>(context);
        this.frequency = frequency;
    }

    public Node(Node node){
        this.eventType = node.getEventType();
        this.context = new HashMap<>(node.getContext());
        this.frequency = node.getFrequency();
    }

    public String getEventType(){ return this.eventType; }

    public Integer getFrequency(){ return this.frequency; }

    public Map<String, String> getContext(){ return this.context; }

    void increaseFrequency() { this.frequency += 1; }

    @Override
    public String toString() {
        String context = "";
        List<String> ignoreAttributes = new ArrayList<>();
        ignoreAttributes.add("target.type");
        //ignoreAttributes.add("target.checked");
        if(this.context.containsKey("target.name") && this.context.containsKey("target.id"))
            ignoreAttributes.add("target.name");

        for(var attribute: this.context.keySet()){
            if(!ignoreAttributes.contains(attribute))
                context += this.context.get(attribute) + "+";
        }
        if(!context.equals(""))
            return eventType + "+" + context.substring(0, context.lastIndexOf("+"));
        else
            return eventType;
    }

    @Override
    public boolean equals(Object obj){
        if(obj != null && getClass() == obj.getClass()){
            Node node = (Node) obj;
            return this.eventType.equals(node.eventType) && this.context.equals(node.context);
        }
        return false;
    }

    @Override
    public int hashCode(){
        return Objects.hash(eventType, context);
    }
}
