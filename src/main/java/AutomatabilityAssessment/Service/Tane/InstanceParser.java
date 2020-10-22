package AutomatabilityAssessment.Service.Tane;

import AutomatabilityAssessment.Data.Sequence;
import Segmentation.Data.Node;
import Utils.CaseService;
import data.Event;
import data.Pattern;
import data.PatternItem;

import java.util.*;
import java.util.stream.Collectors;

public class InstanceParser {
    //private final List<String> payloadAttributes = Arrays.asList("target.value", "content", "target.id", "url");
    private final List<String> payloadAttributes = Arrays.asList("Value", "FileName", "Url");

    private List<List<String>> instances;

    private Pattern pattern;
    private CaseService caseService;

    public InstanceParser(CaseService caseService) {
        this.caseService = caseService;
    }

    public List<List<String>> getInstances(Pattern pattern) {
        this.pattern = pattern;
        // Fill instances with empty lists
        initInstances();
        // Find instances
        Map<String, Integer> patternItemsCounts = new HashMap<>();
        pattern.getItems().forEach(patternItem -> extractData(patternItemsCounts, patternItem));

        return instances;
    }

    private void initInstances() {
        instances = new ArrayList<>();
        caseService.getCases().entrySet().stream()
                .filter(entry -> {
                    Sequence s = getSequence(entry.getValue());
                    return s.containsPattern(pattern);
                })
                .forEach(entry -> instances.add(new ArrayList<>()));
    }

    private void extractData(Map<String, Integer> patternItemsCounts, PatternItem patternItem){
        int sequenceIndex = 0;
        countPatternItems(patternItemsCounts, patternItem.getValue());
        var occurences = caseService.getAllOccurences(pattern);
        for(var caseEvents: occurences){
            List<Event> matchCaseEvents = getEvents(patternItem.getValue(), caseEvents);
            for (String payloadAttribute : payloadAttributes) {
                int patternItemIndex = patternItemsCounts.get(patternItem.getValue());
                Map<String, String> payload = matchCaseEvents.get(patternItemIndex).getPayload();
                if (payload.containsKey(payloadAttribute)) {
                    String attributeValue = payload.get(payloadAttribute);
                    // Fill attributes map and instances list
                    instances.get(sequenceIndex).add(attributeValue);
                    break;
                }
            }
            if (instances.get(sequenceIndex).isEmpty() || instances.get(sequenceIndex).size() < patternItem.getIndex() + 1)
                instances.get(sequenceIndex).add("");
            sequenceIndex++;
        }
    }

    /*
    private void extractData(Map<String, Integer> patternItemsCounts, PatternItem patternItem) {
        countPatternItems(patternItemsCounts, patternItem.getValue());
        int sequenceIndex = -1;
        var occurences = caseService.getAllOccurences(pattern);
        for (var caseEvents : occurences) {
            Sequence sequence = getSequence(caseEvents);
            if (sequence.contains(pattern)) {
                sequenceIndex++;
                // Find events in the sequence that matches the current pattern item
                List<Event> matchCaseEvents = getEvents(patternItem.getValue(), caseEvents);
                // Among defined payload attributes find that are present and add them to the valuesPerAttribute map
                for (String payloadAttribute : payloadAttributes) {
                    int patternItemIndex = patternItemsCounts.get(patternItem.getValue());
                    Map<String, String> payload = matchCaseEvents.get(patternItemIndex).getPayload();

                    // Check if event payload contains predefined payload attribute
                    if (payload.containsKey(payloadAttribute)) {
                        String attributeValue = payload.get(payloadAttribute);
                        // Fill attributes map and instances list
                        instances.get(sequenceIndex).add(attributeValue);
                        break;
                    }
                }
                if (instances.get(sequenceIndex).isEmpty() || instances.get(sequenceIndex).size() < patternItem.getIndex() + 1)
                    instances.get(sequenceIndex).add("");
            }
        }
    }
     */


    private Sequence getSequence(List<Event> caseEvents) {
        return new Sequence(caseEvents.stream()
                .map(event -> new Node(event).toString())
                .collect(Collectors.toList()));
    }

    private void countPatternItems(Map<String, Integer> patternItemsCounts, String patternItem) {
        patternItemsCounts.computeIfPresent(patternItem, (key, val) -> val + 1);
        patternItemsCounts.putIfAbsent(patternItem, 0);
    }

    private List<Event> getEvents(String patternItem, List<Event> caseEvents) {
        return caseEvents.stream()
                .filter(caseEvent -> patternItem.equals(new Node(caseEvent).toString()))
                .collect(Collectors.toList());
    }
}
