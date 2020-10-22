package AutomatabilityAssessment.Service.Foofah;

import data.Pattern;
import data.PatternItem;

import java.util.*;

public class PatternEventFlowExtractor {
    private List<String> readActions;
    private List<String> writeActions;

    public PatternEventFlowExtractor() {
        //writeActions = new ArrayList<>(Arrays.asList("editField", "editCell"));
        //readActions = new ArrayList<>(Arrays.asList("copyCell", "copy"));

        writeActions = new ArrayList<>(Arrays.asList("insertValue", "paste", "openFile"));
        readActions = new ArrayList<>(Arrays.asList("copy"));
    }

    public Map<PatternItem, List<PatternItem>> extractWriteEventsPerReadEvent(Pattern pattern) {
        LinkedHashMap<PatternItem, List<PatternItem>> writesPerRead = new LinkedHashMap<>();
        PatternItem readEvent = new PatternItem();

        for (PatternItem event : pattern.getItems()) {
            String eventType = event.getValue().split("\\+")[0];
            if (readActions.contains(eventType)) {
                readEvent = event;
                if(!writesPerRead.containsKey(readEvent))
                    writesPerRead.put(readEvent, new ArrayList<>());
            } else if (writeActions.contains(eventType)) {
                if (writesPerRead.containsKey(readEvent)) {
                    writesPerRead.get(readEvent).add(event);
                } else {
                    if (!writesPerRead.containsKey(null)) {
                        writesPerRead.put(null, new ArrayList<>());
                    }
                    writesPerRead.get(null).add(event);
                }
            }
        }

        return writesPerRead;
    }
}