package AutomatabilityAssessment.Data;

import lombok.Data;

@Data
public class TaneDependency {
    private Integer dependeeIdx;
    private Integer dependerIdx;

    public TaneDependency(Integer dependeeIdx, Integer dependerIdx) {
        this.dependeeIdx = dependeeIdx;
        this.dependerIdx = dependerIdx;
    }
}
