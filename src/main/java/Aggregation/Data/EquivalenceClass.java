package Aggregation.Data;

import data.PatternItem;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

@Data
public class EquivalenceClass {
    private HashMap<Pair<PatternItem, PatternItem>, String> transformations;
    private HashMap<String, Set<Pair<String, String>>> dependencyChains;
    private List<String> systemChanges;

    public EquivalenceClass(Map<Pair<PatternItem, PatternItem>, String> transformations,
                            HashMap<String, Set<Pair<String, String>>> dependencyChains,
                            List<String> systemChanges){
        this.transformations = new HashMap<>(transformations);
        this.transformations.values().removeIf(value -> value.isEmpty());
        this.dependencyChains = new HashMap<>(dependencyChains);
        this.systemChanges = new ArrayList<>(systemChanges);
    }

    @Override
    public boolean equals(Object obj){
        if(obj != null && getClass() == obj.getClass()){
            EquivalenceClass equivalenceClass = (EquivalenceClass) obj;
            return this.transformations.equals(equivalenceClass.getTransformations()) &&
                    this.dependencyChains.equals(equivalenceClass.getDependencyChains()) &&
                            this.systemChanges.equals(equivalenceClass.getSystemChanges());
        }
        return false;
    }

    @Override
    public int hashCode(){
        return Objects.hash(transformations, dependencyChains);
    }
}