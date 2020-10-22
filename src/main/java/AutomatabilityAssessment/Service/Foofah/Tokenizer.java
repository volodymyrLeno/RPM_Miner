package AutomatabilityAssessment.Service.Foofah;

import AutomatabilityAssessment.Data.Transformation;

import java.util.*;
import java.util.stream.Collectors;

public class Tokenizer {
    public static Map<String, List<Transformation>> clusterByPattern(List<Transformation> transformations) {
        Map<String, List<Transformation>> clusters = new HashMap<>();
        transformations.forEach(transformation -> {
            String pattern = tokenize(transformation);

            if (clusters.containsKey(pattern)) {
                List<Transformation> collection = new ArrayList<>(clusters.get(pattern));
                collection.addAll(Collections.singletonList(transformation));
                clusters.put(pattern, collection);
            } else {
                clusters.put(pattern, Collections.singletonList(transformation));
            }
        });

        return clusters;
    }

    private static String tokenize(Transformation transformationExample) {
        List<String> patterns = transformationExample.getInput().stream()
                .map(example -> example.replaceAll("[a-zA-Z]+", "<a>+"))
                .map(pattern -> pattern.replaceAll("\\d+", "<d>+"))
                .collect(Collectors.toList());

        return patterns.size() == 1 ? patterns.get(0) : patterns.toString();
    }
}
