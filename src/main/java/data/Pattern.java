package data;

import AutomatabilityAssessment.Data.ItemsDependency;

import java.util.*;
import java.util.stream.Collectors;

import Segmentation.Data.DirectlyFollowsGraph;
import Segmentation.Data.Node;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Data
public class Pattern implements Comparable<Pattern> {
    private UUID id = UUID.randomUUID();
    private List<PatternItem> items;
    private int absSupport;
    private double relSupport;
    private int length;
    private int cohesionScore;
    private boolean automatable = false;
    private Map<Pair<data.PatternItem, PatternItem>, String> transformations;
    private List<ItemsDependency> itemsDependencies;
    private double coverage;
    private double RAI;

    private List<String> closestMatch;
    private int tp = 0;
    private int tn = 0;
    private int fp = 0;
    private int fn = 0;
    private double jaccard;
    private double precision;
    private double recall;
    private double accuracy;
    private double fscore;

    public Pattern() {
        items = new ArrayList<>();
        transformations = new HashMap<>();
        itemsDependencies = new ArrayList<>();
    }

    public Pattern(List<PatternItem> items) {
        this.items = items;
        this.transformations = new HashMap<>();
        this.itemsDependencies = new ArrayList<>();
        this.length = items.size();
    }

    public Pattern(List<PatternItem> items, int support) {
        this.items = items;
        this.absSupport = support;
        this.relSupport = 0.0;
        this.length = items.size();
        this.transformations = new HashMap<>();
        this.itemsDependencies = new ArrayList<>();
    }

    public Pattern(List<PatternItem> items, Double relSup, int absSup) {
        this.items = items;
        this.absSupport = absSup;
        this.relSupport = relSup;
        this.length = items.size();
        this.transformations = new HashMap<>();
        this.itemsDependencies = new ArrayList<>();
    }

    public Pattern(Pattern pattern){
        this.items = pattern.getItems();
        this.absSupport = pattern.getAbsSupport();
        this.relSupport = pattern.getRelSupport();
        this.length = pattern.getLength();
        this.transformations = new HashMap<>(pattern.getTransformations());
        this.itemsDependencies = new ArrayList<>(pattern.getItemsDependencies());
    }

    public List<String> getItemsValues() {
        return items.stream().map(PatternItem::getValue).collect(Collectors.toList());
    }

    public List<Pair<PatternItem, PatternItem>> getEmptyTransformationPairs() {
        return transformations.entrySet().stream()
                .filter(transformation -> transformation.getValue().isEmpty())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public void setAutomatability() {
        items.forEach(item -> setItemAutomatability(item));
        if (items.stream().allMatch(PatternItem::isAutomatable)) {
            automatable = true;
        }
    }

    public void setRAI() {
        double automatableItemsCount = (double) items.stream().filter(PatternItem::isAutomatable).count();
        double totalItemsCount = items.size();
        RAI = automatableItemsCount / totalItemsCount;
    }

    private void setItemAutomatability(PatternItem item) {
        if(!item.getDeterministicLocation())
            item.setAutomatable(false);
        else{
            transformations.entrySet().stream()
                    .filter(entry -> entry.getKey().getRight().equals(item) && entry.getValue().isEmpty())
                    .forEach(t -> itemsDependencies.stream()
                            .filter(d -> d.getDepender().equals(item) && d.getDependerPerDependee().containsValue(null))
                            .forEach(d -> item.setAutomatable(false)));
        }
    }

    /*
    public void analyseLocations(List<List<Event>> instances){
        for(int i = 0; i < items.size(); i++){
            List<String> locations = new ArrayList<>();
            for(var instance: instances){
                var item = instance.get(i);
                String location = "";
                if(item.getApplication().equals("Chrome"))
                    location = item.getContext().containsKey("target.id") ? item.payload.get("target.id") : item.payload.get("target.name");
                else if(item.getApplication().equals("Excel")){
                    if(item.getContext().containsKey("target.column"))
                        location += item.payload.get("target.column");
                    if(item.getContext().containsKey("target.row"))
                        location += item.payload.get("target.row");
                }
                locations.add(location);
            }
            if(analyseLocation(locations))
                items.get(i).setDeterministicLocation(true);
            else
                items.get(i).setDeterministicLocation(false);
        }
    }
     */

    public void analyseLocations(List<List<Event>> instances){
        for(int i = 0; i < items.size(); i++){
            List<String> locations = new ArrayList<>();
            for(var instance: instances){
                var item = instance.get(i);
                String location = "";
                if(item.getApplication().equals("Web"))
                    location = item.getContext().containsKey("Label") ? item.payload.get("Label") : item.payload.get("FileName");
                else if(item.getApplication().equals("Excel")){
                    if(item.getContext().containsKey("Column"))
                        location += item.payload.get("Row");
                    if(item.getContext().containsKey("Row"))
                        location += item.payload.get("Column");
                }
                locations.add(location);
            }
            if(analyseLocation(locations))
                items.get(i).setDeterministicLocation(true);
            else
                items.get(i).setDeterministicLocation(false);
        }
    }

    public boolean analyseLocation(List<String> locations){
        var uniqueLocations = locations.stream().distinct().collect(Collectors.toList());
        if(uniqueLocations.size() == 1)
            return true;
        else{
            List<Integer> rows = new ArrayList<>();
            List<Integer> columns = new ArrayList<>();

            for(var location: locations){
                String row = location.replaceAll("[A-Za-z]+", "");
                String column = location.replaceAll("\\d+", "");
                rows.add(Integer.parseInt(row));
                columns.add(Utils.Utils.convertColumnLetters(column));
            }

            if(Utils.Utils.findProgression(rows))
                return true;
            else
                return false;
        }
    }

    public void assignClosestMatch(List<List<String>> groundTruth){
        List<String> closestMatch = new ArrayList<>(groundTruth.get(0));
        Integer bestMatchScore = LevenshteinDistance(getItemsValues(), groundTruth.get(0));
        for(int i = 1; i < groundTruth.size(); i++) {
            Integer matchScore = LevenshteinDistance(getItemsValues(), groundTruth.get(i));
            if (bestMatchScore >= matchScore) {
                closestMatch = new ArrayList<>(groundTruth.get(i));
                bestMatchScore = matchScore;
            }
        }
        this.closestMatch = new ArrayList<>(closestMatch);
    }

    public Integer LevenshteinDistance(List<String> pattern, List<String> groundTruth){
        int rows = groundTruth.size() + 1;
        int cols = pattern.size() + 1;
        int editMatrix[][] = new int[rows][cols];
        for(int i = 0; i < rows; i++)
            editMatrix[i][0] = i;
        for(int j = 1; j < cols; j++)
            editMatrix[0][j] = j;
        for(int i = 1; i < rows; i++){
            for(int j = 1; j < cols; j++){
                editMatrix[i][j] = Math.min(
                        editMatrix[i-1][j] + 1,
                        Math.min(
                                editMatrix[i][j-1] + 1,
                                editMatrix[i-1][j-1] + (groundTruth.get(i-1).equals(pattern.get(j-1)) ? 0 : 1)
                        )
                );
            }
        }
        var editDistance = editMatrix[rows - 1][cols - 1];
        return editDistance;
    }

    public void computeConfusionMatrix(List<Event> events){
        var pattern = getItemsValues();
        tp = fn = fp = tn = 0;
        List<String> elements = new ArrayList<>(closestMatch);
        for(var element: pattern){
            if(elements.contains(element)){
                tp++;
                elements.remove(element);
            }
            else
                fp++;
        }
        for(var element: closestMatch){
            if(!pattern.contains(element))
                fn++;
        }
        elements = events.stream().map(event -> new Node(event).toString()).distinct().collect(Collectors.toList());
        for(var element: elements)
            if(!pattern.contains(element) && !closestMatch.contains(element))
                tn++;
    }

    public double calculateJaccard(){
        jaccard = (double)tp/(tp + fp + fn);
        return jaccard;
    }

    public double calculateJaccard(List<List<String>> groundTruths, List<Event> events){
        List<String> bestMatch = new ArrayList<>();
        Double bestJaccard = 0.0;
        HashMap<List<String>, Double> jaccards = new HashMap<>();

        for(var gt: groundTruths){
            closestMatch = new ArrayList<>(gt);
            computeConfusionMatrix(events);
            var jaccard = calculateJaccard();
            jaccards.put(gt, jaccard);
            if(jaccard > bestJaccard){
                bestJaccard = jaccard;
                bestMatch = new ArrayList<>(gt);
            }
        }

        closestMatch = new ArrayList<>(bestMatch);
        jaccard = bestJaccard;
        return jaccard;
    }

    public double calculatePrecision(){
        precision = (double)tp/(tp+fp);
        return precision;
    }

    public double calculateRecall(){
        recall = (double)tp/(tp+fn);
        return recall;
    }

    public double calculateAccuracy(){
        accuracy = (double)(tp+tn)/(tp+tn+fp+fn);
        return accuracy;
    }

    public double calculateFScore(){
        double precision = calculatePrecision();
        double recall = calculateRecall();
        fscore = precision == 0 && recall == 0 ? 0.0 : (2 * precision * recall)/(precision + recall);
        return fscore;
    }

    public double getPrecision(){ return precision; }

    public double getRecall(){ return recall; }

    public double getAccuracy(){ return accuracy; }

    public double getFscore(){ return fscore; }

    public double getJaccard(){ return jaccard; }

    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();

        ArrayNode items = mapper.createArrayNode();
        this.items.forEach(i -> items.add(i.getValue()));

        ArrayNode transformations = mapper.createArrayNode();
        this.transformations.forEach((k, v) -> {
            ObjectNode tn = mapper.createObjectNode();
            ArrayNode ts = mapper.createArrayNode();
            Arrays.asList(v.split("\n")).forEach(t -> {
                if (!t.isBlank()) ts.add(t.trim());
            });
            tn.set(k.getLeft() + " -> " + k.getRight(), ts);
            transformations.add(tn);
        });

        ArrayNode dependencies = mapper.createArrayNode();
        itemsDependencies.forEach(d -> {
            ObjectNode itemDep = mapper.createObjectNode();

            if (d.getDependee() != null) {
                ArrayNode dependee = mapper.createArrayNode();
                d.getDependee().forEach(dep -> dependee.add(dep.getValue()));
                itemDep.set("dependee", dependee);
            }

            if (d.getDepender() != null) {
                itemDep.put("depender", d.getDepender().getValue());
            }

            if (d.getDependerPerDependee() != null) {
                ArrayNode values = mapper.createArrayNode();
                d.getDependerPerDependee().forEach((k, v) -> {
                    ObjectNode value = mapper.createObjectNode();

                    if (d.getDependee() != null) {
                        ArrayNode dependeeValues = mapper.createArrayNode();
                        for (int i = 0; i < k.size(); i++) {
                            ObjectNode dependeeValue = mapper.createObjectNode();
                            dependeeValue.put(d.getDependee().get(i).getValue(), k.get(i));
                            dependeeValues.add(dependeeValue);
                        }
                        value.set("dependee", dependeeValues);
                    }

                    if (d.getDepender() != null) {
                        value.put("depender", v);
                    }

                    values.add(value);
                });
                itemDep.set("values", values);
            }
            dependencies.add(itemDep);
        });

        ObjectNode pattern = mapper.createObjectNode();
        pattern.set("items", items);
        pattern.put("length", length);
        pattern.put("absolute support", absSupport);
        pattern.put("relative support", relSupport);
        pattern.put("cohesion score", cohesionScore);
        pattern.set("transformations", transformations);
        pattern.set("functional dependencies", dependencies);
        pattern.put("automatable", automatable);
        pattern.put("coverage", coverage);
        pattern.put("RAI", RAI);

        ObjectNode root = mapper.createObjectNode();
        root.set("pattern@" + hashCode(), pattern);

        return root.toPrettyString();
    }

    public Double getRelativeSupport() {
        return this.relSupport;
    }

    public void setRelativeSupport(double support) {
        this.relSupport = support;
    }

    public Integer getAbsoluteSupport() {
        return this.absSupport;
    }

    public void setAbsoluteSupport(Integer support) {
        this.absSupport = support;
    }

    public int getLength() {
        return this.length;
    }

    public double getCoverage() {
        return this.coverage;
    }

    public void setCoverage(double coverage) {
        this.coverage = coverage;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && getClass() == obj.getClass()) {
            Pattern pattern = (Pattern) obj;
            return this.getItemsValues().equals(pattern.getItemsValues());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getItemsValues());
    }

    @Override
    public int compareTo(Pattern e) {
        if (e.getLength() == this.length)
            return e.getAbsoluteSupport() - this.getAbsoluteSupport();
        else
            return e.getLength() - this.getLength();
    }
}
