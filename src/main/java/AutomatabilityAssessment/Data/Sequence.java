package AutomatabilityAssessment.Data;

import data.Pattern;
import data.PatternItem;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@ToString
public class Sequence {
    private int id;
    private List<String> items;

    public Sequence(List<String> items) {
        this.items = items;
    }

    public Sequence(int id, List<String> items) {
        this.id = id;
        this.items = items;
    }

    public boolean contains(Pattern pattern) {
        List<String> patternEvents = new ArrayList<>(pattern.getItemsValues());
        List<String> sequenceEvents = new ArrayList<>(items);

        for (String patternEvent : patternEvents) {
            if (sequenceEvents.contains(patternEvent)) {
                sequenceEvents.remove(patternEvent);
            } else {
                return false;
            }
        }

        return true;
    }

    /*
    public boolean containsPattern(Pattern pattern) {
        List<String> sequenceItems = new ArrayList<>(items);
        for (String patternItem : pattern.getItemsValues()) {
            if (sequenceItems.contains(patternItem)) {
                sequenceItems.remove(patternItem);
            } else {
                return false;
            }
        }

        return true;
    }
     */

    public boolean containsPattern(Pattern pattern){
        List<String> sequenceItems = new ArrayList<>(items);
        List<String> patternItems = pattern.getItemsValues();
        HashMap<String, List<Integer>> positions = new HashMap<>();
        for(var patternItem: patternItems){
            for(int i = 0; i < sequenceItems.size(); i++){
                if(sequenceItems.get(i).equals(patternItem))
                    if(positions.containsKey(patternItem))
                        positions.put(patternItem, Stream.concat(positions.get(patternItem).stream(),
                                Stream.of(i)).collect(Collectors.toList()));
                    else{
                        int finalI = i;
                        positions.put(patternItem, new ArrayList<>(){{add(finalI);}});
                    }
            }
        }
        if(positions.size() == patternItems.stream().distinct().collect(Collectors.toList()).size()){
            List<Integer> pos = new ArrayList<>();
            for(var patternItem: patternItems){
                boolean flag = false;
                if(pos.size() == 0 || pos.get(pos.size() - 1) < positions.get(patternItem).get(0))
                    pos.add(positions.get(patternItem).get(0));
                else{
                    for(var idx: positions.get(patternItem)){
                        if(idx > pos.get(pos.size() - 1)){
                            pos.add(idx);
                            flag = true;
                            break;
                        }
                    }
                    if(flag == false)
                        break;
                }
            }
            if(pos.size() == patternItems.size())
                return true;
        }
        return false;
    }

    public List<PatternItem> removePatternElements(Pattern pattern) {
        List<String> sequenceItems = new ArrayList<>(items);
        if (containsPattern(pattern)) {
            pattern.getItemsValues().forEach(sequenceItems::remove);
            setItems(sequenceItems);

            return pattern.getItems();
        }

        return new ArrayList<>();
    }
}
