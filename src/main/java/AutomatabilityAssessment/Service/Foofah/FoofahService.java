package AutomatabilityAssessment.Service.Foofah;

import AutomatabilityAssessment.Data.Transformation;
import data.Pattern;
import data.PatternItem;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class FoofahService {
    private final TransformationExtractor extractor;

    public FoofahService(TransformationExtractor extractor) {
        this.extractor = extractor;
    }

    public Map<Pair<PatternItem, PatternItem>, String> findTransformations(Pattern pattern) {
        LinkedHashMap<Pair<PatternItem, PatternItem>, String> patternTransformations = new LinkedHashMap<>();
        Map<Pair<PatternItem, PatternItem>, List<Transformation>> transformationsPerReadWrite = extractor.getPatternTransformations(pattern);

        transformationsPerReadWrite.forEach((readWritePair, transformations) -> {
            Map<String, List<Transformation>> cluster = Tokenizer.clusterByPattern(transformations);
            String transformation = getFoofahPatternsTransformation(cluster);
            System.out.println(readWritePair);
            System.out.println(transformation);
            patternTransformations.put(readWritePair, transformation);
        });

        return patternTransformations;
    }

    public String getFoofahPatternsTransformation(Map<String, List<Transformation>> tokenizedTransformations) {
        Map<String, List<String>> groupedPatterns = new HashMap<>();

        for (String pattern : tokenizedTransformations.keySet()) {
            String transformation = getFoofahTransformation(getSeed(1.0 / tokenizedTransformations.get(pattern).size(),
                    tokenizedTransformations.get(pattern)));

            if (transformation == null) return "";

            groupedPatterns.put(transformation, groupedPatterns.containsKey(transformation) ?
                    Stream.concat(groupedPatterns.get(transformation).stream(), Stream.of(pattern)).collect(Collectors.toList()) :
                    Collections.singletonList(pattern));
        }

        if (groupedPatterns.size() == 1) {
            return String.valueOf(groupedPatterns.keySet().toArray()[0]);
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < groupedPatterns.keySet().size(); i++) {
                String transformation = String.valueOf(groupedPatterns.keySet().toArray()[i]);
                if (i == (groupedPatterns.size() - 1) && groupedPatterns.get(transformation).size() > 1) {
                    sb.append("Default: \n").append(transformation).append("\n");
                } else {
                    if (groupedPatterns.get(transformation).size() == 1) {
                        sb.append(groupedPatterns.get(transformation).toArray()[0]).append("\n").append(transformation);
                    } else {
                        groupedPatterns.get(transformation).forEach(t -> sb.append(t).append("\n"));
                        sb.append("\n").append(transformation);
                    }
                }
            }

            return sb.toString();
        }
    }

    private List<Transformation> getSeed(Double frac, List<Transformation> transformations) {
        List<Transformation> seed = new ArrayList<>();

        int num = (int) Math.ceil(frac * transformations.size());
        List<Integer> indexes = new ArrayList<>();
        Random random = new Random();
        while (seed.size() < num) {
            int next = random.nextInt(transformations.size());
            if (!indexes.contains(next)) {
                indexes.add(next);
                seed.add(transformations.get(next));
            }
        }
        return seed;
    }

    private String getFoofahTransformation(List<Transformation> transformations) {
        String output = null;
        try {
            File foofahFile = new File(FoofahExecutor.getFoofahPath() + "/foofah.temp");

            FileWriter fileWriter = new FileWriter(foofahFile);
            for (Transformation t : transformations) {
                StringBuilder sb = valuesToFoofahJSON(t);
                fileWriter.write(sb.toString());
                fileWriter.flush();
                output = FoofahExecutor.execPython();
            }

            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return output;
    }

    private StringBuilder valuesToFoofahJSON(Transformation transformation) {
        StringBuilder sb = new StringBuilder();
        String input = getFormattedList(transformation.getInput());
        String output = getFormattedList(transformation.getOutput());

        sb.append("{").append("\"InputTable\":").append(" [").append((input)).append("]").append(", ")
                .append("\"OutputTable\":").append(" [").append(output).append("]").append("}");

        return sb;
    }

    private String getFormattedList(List<String> list) {
        String sb = list.stream().map(el -> "\"".concat(el).concat("\""))
                .collect(Collectors.joining(", ", "[", "]"));

        return sb.trim();
    }
}
