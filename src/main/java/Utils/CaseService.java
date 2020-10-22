package Utils;

import AutomatabilityAssessment.Data.Sequence;
import Segmentation.Data.Node;
import Segmentation.Service.SegmentsDiscoverer;
import data.Event;
import data.Pattern;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class CaseService {
    private Map<Integer, List<Event>> cases;

    public void extractCases(List<Event> events, boolean isSegmented, List<String> context) {
        if (isSegmented) {
            cases = extractSegmentedCases(events);
        } else {
            //events = SimplifierService.applyPreprocessing(events, context);
            SegmentsDiscoverer disco = new SegmentsDiscoverer();
            cases = disco.extractSegments(events);
        }
        //cases = SimplifierService.applyPreprocessing(cases, context);
    }

    private Map<Integer, List<Event>> extractSegmentedCases(List<Event> events) {
        Map<Integer, List<Event>> cases = new HashMap<>();

        for (Event event : events) {
            int caseId = Integer.parseInt(event.getCaseID());
            if (!cases.containsKey(caseId))
                cases.put(caseId, Collections.singletonList(event));
            else
                cases.put(caseId, Stream.concat(cases.get(caseId).stream(),
                        Stream.of(event)).collect(Collectors.toList()));
        }

        return cases;
    }

    public List<Event> getFirstOccurrence(Pattern pattern) {
        List<Event> occurrence = new ArrayList<>();

        List<Event> firstContainingCase = getFirstContainingCase(pattern);
        for (int i = 0; i < pattern.getItems().size(); i++) {
            if (pattern.getItemsValues().get(i).contains(firstContainingCase.get(i).getEventType())) {
                occurrence.add(firstContainingCase.get(i));
            }
        }

        return occurrence;
    }

    public List<Event> getFirstContainingCase(Pattern pattern) {
        return IntStream.range(1, cases.size())
                .mapToObj(i -> new Sequence(i, caseToString(cases.get(i))))
                .filter(s -> s.containsPattern(pattern))
                .map(s -> cases.get(s.getId()))
                .findFirst().orElse(Collections.emptyList());
    }

    public List<List<Event>> getAllOccurences(Pattern pattern){
        List<List<Event>> occurences = new ArrayList<>();
        for(var key: cases.keySet()){
            Sequence seq = new Sequence(caseToString(cases.get(key)));
            if(seq.containsPattern(pattern))
                occurences.add(cases.get(key));
        }
        return occurences;
    }

    public Map<Integer, List<Event>> getCases() {
        return cases;
    }

    private List<List<String>> casesToSequences(Map<Integer, List<Event>> cases) {
        return cases.keySet().stream()
                .map(key -> cases.get(key).stream()
                        .map(el -> new Node(el).toString())
                        .collect(Collectors.toList()))
                .map(ArrayList::new)
                .collect(Collectors.toList());
    }

    private List<String> caseToString(List<Event> events) {
        return events.stream().map(el -> new Node(el).toString()).collect(Collectors.toList());
    }
}
