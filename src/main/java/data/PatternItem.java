package data;

import Aggregation.Data.DataFlowGraph;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

@Data
@NoArgsConstructor
public class PatternItem {
    private int index;
    private String value;
    private boolean automatable = true;
    private String contextId;
    private Boolean deterministicLocation;
    private HashMap<String, String> context;

    public PatternItem(String value) {
        this.value = value;
    }

    public PatternItem(int index, String value) {
        this.index = index;
        this.value = value;
    }

    public String getValue(){return value; }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object obj){
        if(obj != null && getClass() == obj.getClass()){
            PatternItem patternItem = (PatternItem) obj;
            return value.equals(patternItem.getValue());
        }
        return false;
    }

    @Override
    public int hashCode(){
        return Objects.hash(value);
    }
}