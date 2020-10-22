package Utils;

import Segmentation.Data.Node;
import data.Event;
import data.Pattern;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utils {
    public static HashMap<String, List<Event>> groupByEventType(List<Event> events){
        HashMap<String, List<Event>> groupedEvents = new HashMap<>();
        for(var event: events){
            if(!groupedEvents.containsKey(event.getEventType()))
                groupedEvents.put(event.getEventType(), Collections.singletonList(event));
            else
                groupedEvents.put(event.getEventType(), Stream.concat(groupedEvents.get(event.getEventType()).stream(),
                        Stream.of(event)).collect(Collectors.toList()));
        }
        return groupedEvents;
    }

    public static HashMap<String, List<Event>> groupEvents(List<Event> events){
        HashMap<String, List<Event>> groupedEvents = new HashMap<>();
        for(var event: events){
            var key = event.getEventType() + "_" + event.getApplication();
            if(!groupedEvents.containsKey(key))
                groupedEvents.put(key, Collections.singletonList(event));
            else
                groupedEvents.put(key, Stream.concat(groupedEvents.get(key).stream(),
                        Stream.of(event)).collect(Collectors.toList()));
        }

        return groupedEvents;
    }

    public static void getTransposableActions(String[][] reachabilityMatrix){
        for(int i = 1; i < reachabilityMatrix.length; i++)
            for(int j = i+1; j < reachabilityMatrix.length; j++)
                if(reachabilityMatrix[i][j].equals("1") && reachabilityMatrix[j][i].equals("1"))
                    System.out.println("Transposable actions - " + reachabilityMatrix[i][0] + " and " + reachabilityMatrix[0][j]);
    }

    public static String[][] buildReachabilityMatrix(List<String> traces){
        List<Character> actions = new ArrayList<>();
        for(var trace: traces){
            for(int i = 0; i < trace.length(); i++)
                actions.add(trace.charAt(i));
        }
        actions = actions.stream().distinct().collect(Collectors.toList());
        String[][] reachabilityMatrix = new String[actions.size() + 1][actions.size() + 1];
        for(int i = 0; i < reachabilityMatrix.length; i++)
            for(int j = 0; j < reachabilityMatrix[i].length; j++)
                reachabilityMatrix[i][j] = "0";
        for(int i = 1; i < reachabilityMatrix.length; i++){
            reachabilityMatrix[i][0] = actions.get(i-1).toString();
            reachabilityMatrix[0][i] = actions.get(i-1).toString();
        }
        boolean precedence;
        boolean response;
        for(int i = 0; i < actions.size(); i++)
            for(int j = i+1; j < actions.size(); j++){
                precedence = false;
                response = false;
                for(var trace: traces){
                    if(precedence && response)
                        break;
                    else{
                        if(trace.indexOf(actions.get(i)) != -1 && trace.indexOf(actions.get(j)) != -1){
                            if(trace.indexOf(actions.get(i)) < trace.indexOf(actions.get(j))){
                                reachabilityMatrix[i+1][j+1] = "1";
                                precedence = true;
                            }
                            else{
                                reachabilityMatrix[j+1][i+1] = "1";
                                response = true;
                            }
                        }
                    }
                }
            }
        return reachabilityMatrix;
    }

    public static void printMatrix(int[][] matrix, String s1, String s2){
        System.out.print("- | ");
        for (int j = 0; j < s1.length(); j++) {
            System.out.printf("%2s | ", s1.charAt(j));
            //System.out.print(s1.charAt(j) + "  | ");
        }
        System.out.println();
        for (int i = 1; i < s2.length() + 1; i++) {
            System.out.print(s2.charAt(i - 1) + " | ");
            for (int j = 1; j < s1.length() + 1; j++)
                if(j != 1)
                    System.out.printf("%3s |",matrix[i][j]);
                else
                    System.out.printf("%2s |",matrix[i][j]);
            System.out.println();
        }
    }

    public static void printMatrix(int[][] matrix){
        for(int i = 0; i < matrix.length; i++){
            for(int j = 0; j < matrix[i].length; j++)
                System.out.print(matrix[i][j] + " ");
            System.out.println();
        }
    }

    public static void setContextAttributes(List<Event> events, List<String> contextAttr){
        var contextAttributes = new ArrayList<>(contextAttr);

        if(contextAttributes.contains("Row/Column")) {
            var uniqueColumns = events.stream().map(el -> el.payload.get("Column")).distinct().collect(Collectors.toList());
            var uniqueRows = events.stream().map(el -> el.payload.get("Row")).distinct().collect(Collectors.toList());
            if (uniqueColumns.size() < uniqueRows.size())
                contextAttributes.add("Column");
                //contextAttributes.put("Column", event.payload.get("Column"));
            else if (uniqueRows.size() < uniqueColumns.size())
                contextAttributes.add("Row");
                //context.put("Row", event.payload.get("Row"));
            else {
                contextAttributes.add("Row");
                contextAttributes.add("Column");
                //context.put("Row", event.payload.get("Row"));
                //context.put("Column", event.payload.get("Column"));
            }
        }

        for(var event: events){
            HashMap<String, String> context = new HashMap<>();
            for(var attribute: event.payload.keySet())
                if(contextAttributes.contains(attribute)) {
                    if (attribute.equals("target.id") && (event.getApplication().contains("Excel"))) {
                        var uniqueColumns = events.stream().map(el -> el.payload.get("target.column")).distinct().collect(Collectors.toList());
                        var uniqueRows = events.stream().map(el -> el.payload.get("target.row")).distinct().collect(Collectors.toList());
                        if (uniqueColumns.size() < uniqueRows.size())
                            attribute = "target.column";
                        else
                            attribute = "target.row";
                    }
                    context.put(attribute, event.payload.get(attribute));
                }
            event.context = new HashMap<>(context);
        }
    }

    public static void getSummary(List<Pattern> patterns, List<List<String>> groundTruth, List<Event> events) {
        int i = 1;
        for (var pattern : patterns) {
            System.out.println("\nRoutine " + i + "\nPattern:  " + pattern);
            if (groundTruth.size() > 0) {
                pattern.assignClosestMatch(groundTruth);
                pattern.computeConfusionMatrix(events);
                System.out.println("The closest match:  " + pattern.getClosestMatch());
            }
            System.out.println("Length = " + pattern.getLength());
            System.out.printf("Sup = %.2f\n", pattern.getRelativeSupport());
            System.out.printf("Coverage = %.2f\n", pattern.getCoverage());

            if (groundTruth.size() > 0) {
                System.out.printf("Precision = %.3f\n", pattern.calculatePrecision());
                System.out.printf("Recall = %.3f\n", pattern.calculateRecall());
                System.out.printf("Accuracy = %.3f\n", pattern.calculateAccuracy());
                System.out.printf("F-score = %.3f\n", pattern.calculateFScore());
                System.out.printf("Jaccard = %.3f\n", pattern.calculateJaccard(groundTruth, events));
            }
            i++;
        }
        System.out.println("\nOverall results:\n");
        System.out.printf("Average length = %.2f\n", patterns.stream().mapToInt(Pattern::getLength).average().orElse(0.0));
        System.out.printf("Average support = %.2f\n", patterns.stream().mapToDouble(Pattern::getRelativeSupport).average().orElse(0.0));
        System.out.printf("Total coverage = %.2f\n", patterns.stream().mapToDouble(Pattern::getCoverage).sum());
        System.out.printf("Average coverage = %.2f\n", patterns.stream().mapToDouble(Pattern::getCoverage).average().orElse(0.0));

        if (groundTruth.size() > 0) {
            System.out.printf("Average precision = %.3f\n", patterns.stream().mapToDouble(Pattern::getPrecision).average().orElse(0.0));
            System.out.printf("Average recall = %.3f\n", patterns.stream().mapToDouble(Pattern::getRecall).average().orElse(0.0));
            System.out.printf("Average accuracy = %.3f\n", patterns.stream().mapToDouble(Pattern::getAccuracy).average().orElse(0.0));
            System.out.printf("Average f-score = %.3f\n", patterns.stream().mapToDouble(Pattern::getFscore).average().orElse(0.0));
            System.out.printf("Average Jaccard = %.3f\n", patterns.stream().mapToDouble(Pattern::getJaccard).average().orElse(0.0));
        }
    }

    public static List<List<String>> toSequences(HashMap<Integer, List<Event>> cases, List<String> contextAttributes){
        List<List<String>> sequences = new ArrayList<>();

        List<Event> events = new ArrayList<>();
        cases.values().forEach(events::addAll);

        HashMap<String, List<Event>> groupedEvents = groupEvents(events);
        for(var group: groupedEvents.keySet())
            Utils.setContextAttributes(groupedEvents.get(group), contextAttributes);
        for(var caseID: cases.keySet()){
            List<String> sequence = new ArrayList<>();
            for(var event: cases.get(caseID))
                sequence.add(new Node(event.getEventType(), event.context, 1).toString());
            sequences.add(sequence);
        }
        return sequences;
    }

    public static int convertColumnLetters(String columnLetter) {
        int result = 0;
        char[] charArray = columnLetter.toCharArray();
        for (int count = 0; count < charArray.length; count++) {
            int columnValue = 26 << charArray.length - count - 2;
            int unitValue = convertColumnLetter(columnLetter.charAt(count));
            columnValue = (columnValue == 0) ? 1 : columnValue; // account for ones column
            result += (unitValue * columnValue) ;
        }
        return result;
    }

    public static int convertColumnLetter(char c) {
        return ((int) c) - 64;
    }

    public static boolean findProgression(List<Integer> numbers){
        if(numbers.get(1) - numbers.get(0) == numbers.get(2) - numbers.get(1) &&
                numbers.get(2) - numbers.get(1) == numbers.get(3) - numbers.get(2))
            return true;
        else if(numbers.get(1)/numbers.get(0) == numbers.get(2)/numbers.get(1) &&
                numbers.get(2)/numbers.get(1) == numbers.get(3)/numbers.get(2))
            return true;
        else
            return false;
    }
}
