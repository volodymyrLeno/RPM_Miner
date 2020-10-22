package AutomatabilityAssessment.Data;

import data.PatternItem;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ItemsDependency {
    private PatternItem depender;
    private List<PatternItem> dependee;
    private Map<List<String>, String> dependerPerDependee;
}