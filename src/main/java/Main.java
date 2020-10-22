import Aggregation.Data.DataFlowGraph;
import Aggregation.Data.EquivalenceClass;
import Aggregation.Service.AggregationService;
import AutomatabilityAssessment.Service.Foofah.FoofahService;
import AutomatabilityAssessment.Service.Foofah.TransformationExtractor;
import AutomatabilityAssessment.Service.Tane.ItemsDependencyService;
import AutomatabilityAssessment.Service.Tane.TaneService;
import RoutineIdentification.PatternsMiner;
import Simplification.Service.SimplificationService;
import Simplification.Utils.Parser;
import Simplification.Utils.SimplificationUtils;
import Utils.Utils;
import Utils.LogReader;
import Utils.CaseService;
import Aggregation.Data.Alignment;
import data.Event;
import data.Pattern;
import org.apache.commons.collections.functors.EqualPredicate;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    static Boolean preprocessing = null;
    static PatternsMiner.SPMFAlgorithmName algorithm = null;
    static String metric = null;
    static List<String> contextAttributes = null;
    static Double minSupport = 0.0;
    static Double minCoverage = 0.0;
    static Boolean segmented = null;

    public static void main(String[] args) {
        String log = args[0];
        String config = args[1];

        readConfiguration(config);
        List<Event> events = new ArrayList<>();
        String fileType = log.substring(log.lastIndexOf('.') + 1);
        HashMap<Integer, List<Event>> originalCases = new HashMap<>();
        List<List<String>> sequences = new ArrayList<>();

        if (fileType.equals("xes")) {
            originalCases = LogReader.readXES(log);
            originalCases.values().forEach(events::addAll);
            events.forEach(event -> event.setCaseID(""));
            events.forEach(event -> event.removeAttribute("caseID"));

            sequences = Utils.toSequences(originalCases, contextAttributes).stream().distinct().collect(Collectors.toList());
        } else if (fileType.equals("csv")) {
            events = LogReader.readCSV(log);
            if(preprocessing == true)
                events = SimplificationService.applyPreprocessing(events, contextAttributes);
        } else {
            System.out.println("The tool only supports XES and CSV formats!");
            return;
        }

        var groupedEvents = Utils.groupEvents(events);

        for(var key: groupedEvents.keySet())
            Utils.setContextAttributes(groupedEvents.get(key), contextAttributes);

        CaseService caseService = new CaseService();
        caseService.extractCases(events, segmented, contextAttributes);
        HashMap<Integer, List<Event>> cases = new HashMap<>(caseService.getCases());

        long t1 = System.currentTimeMillis();

        PatternsMiner patternsMiner = new PatternsMiner(caseService);
        Collection<Pattern> patterns = patternsMiner.discoverPatterns(algorithm, minSupport, minCoverage, metric);

        TransformationExtractor transformationExtractor = new TransformationExtractor(caseService);
        FoofahService foofahService = new FoofahService(transformationExtractor);
        TaneService taneService = new TaneService(caseService);
        ItemsDependencyService itemsDependencyService = new ItemsDependencyService(taneService);

        if (patterns != null) {
            patterns.forEach(pattern -> pattern.setTransformations(foofahService.findTransformations(pattern)));
            patterns.forEach(pattern -> pattern.setItemsDependencies(itemsDependencyService.findDependencies(pattern)));
            patterns.forEach(pattern -> pattern.analyseLocations(caseService.getAllOccurences(pattern)));
            patterns.forEach(pattern -> pattern.setAutomatability());
            patterns.forEach(pattern -> pattern.setRAI());
        }

        //patterns = patterns.stream().filter(pattern -> pattern.isAutomatable()).collect(Collectors.toList());
        //long t2 = System.currentTimeMillis();
        //System.out.println("Execution time - " + (t2 - t1) / 1000.0 + " sec");


        if(patterns.size() > 1){
            HashMap<EquivalenceClass, List<Pattern>> clusters = new HashMap<>();
            for(var pattern: patterns){
                DataFlowGraph dataFlowGraph = new DataFlowGraph();
                dataFlowGraph.constructDataFlowGraph(pattern);
                EquivalenceClass equivalenceClass = new EquivalenceClass(pattern.getTransformations(),
                        dataFlowGraph.getDependencyChains(), dataFlowGraph.getSystemChanges());
                if(!clusters.containsKey(equivalenceClass))
                    clusters.put(equivalenceClass, Collections.singletonList(pattern));
                else
                    clusters.put(equivalenceClass, Stream.concat(clusters.get(equivalenceClass).stream(),
                            Stream.of(pattern)).collect(Collectors.toList()));
            }

            AggregationService aggregationService = new AggregationService();
            patterns = aggregationService.getRepresentatives(clusters).values();
        }

        long t2 = System.currentTimeMillis();
        System.out.println("Execution time - " + (t2 - t1) / 1000.0 + " sec");

        List<List<String>> groundTruth = new ArrayList<>(sequences);

        Utils.getSummary(new ArrayList<>(patterns), groundTruth, events);

        //for(var pattern: patterns)
        //    System.out.println(pattern);

        System.out.println("\n\nRoutines discovered: " + patterns.size());
        System.out.println("Total coverage: " + patterns.stream().mapToDouble(Pattern::getCoverage).sum());
        System.out.println("Average length: " + patterns.stream().mapToInt(Pattern::getLength).average().orElse(0.0));
        System.out.println("Max length: " + patterns.stream().mapToInt(Pattern::getLength).max());
        System.out.println("Average RAI: " + patterns.stream().mapToDouble(Pattern::getRAI).average().orElse(0.0));

        /*
        Alignment alignment = new Alignment("GCATGCU", "GATTACA");
        System.out.println("\n" + alignment + "\n");

        Alignment alignment1 = new Alignment("A CAT", "A ABCT");
        System.out.println("\n" + alignment1 + "\n");

        System.out.println(getDistance("ABCDFGH", "ACEBDGI"));
        System.out.println(getDistance("ABCDFGH", "AECBDGI"));
        System.out.println(getDistance("A CAT", "A ABCT"));
        System.out.println(getDistance("ABCDEFG", "ADEBCFG"));

        List<String> traces = new ArrayList<>();
        traces.add("ABCDFGH");
        traces.add("ACEBGDI");
        var matrix = Utils.buildReachabilityMatrix(traces);
        for(int i = 0; i < matrix.length; i++){
            for(int j = 0; j < matrix[i].length; j++)
                System.out.print(matrix[i][j] + " ");
            System.out.println();
        }
        Utils.getTransposableActions(matrix);
        */
    }


    public static int getDistance(String s1, String s2){
        System.out.println(s1);
        System.out.println(s2);

        if (s1 == null) {
            throw new NullPointerException("s1 must not be null");
        }

        if (s2 == null) {
            throw new NullPointerException("s2 must not be null");
        }

        if (s1.equals(s2)) {
            return 0;
        }

        // INFinite distance is the max possible distance
        int inf = s1.length() + s2.length();

        // Create and initialize the character array indices
        HashMap<Character, Integer> da = new HashMap<>();

        for (int d = 0; d < s1.length(); d++) {
            da.put(s1.charAt(d), 0);
        }

        for (int d = 0; d < s2.length(); d++) {
            da.put(s2.charAt(d), 0);
        }

        // Create the distance matrix H[0 .. s1.length+1][0 .. s2.length+1]
        int[][] h = new int[s2.length() + 2][s1.length() + 2];

        // initialize the left and top edges of H
        for (int i = 0; i <= s2.length(); i++) {
            h[i + 1][0] = inf;
            h[i + 1][1] = i;
        }

        for (int j = 0; j <= s1.length(); j++) {
            h[0][j + 1] = inf;
            h[1][j + 1] = j;

        }

        // fill in the distance matrix H
        // look at each character in s1
        for (int i = 1; i <= s2.length(); i++) {
            int db = 0;

            // look at each character in b
            for (int j = 1; j <= s1.length(); j++) {
                int i1 = da.get(s1.charAt(j - 1));
                int j1 = db;

                int cost = 1;
                if (s2.charAt(i - 1) == s1.charAt(j - 1)) {
                    cost = 0;
                    db = j;
                }

                h[i + 1][j + 1] = min(
                        h[i][j] + cost, // substitution
                        h[i + 1][j] + 1, // insertion
                        h[i][j + 1] + 1, // deletion
                        h[i1][j1] + (i - i1 - 1) + 1 + (j - j1 - 1));
            }

            da.put(s2.charAt(i - 1), i);
        }

        h = reduceMatrix(h);

        //printMatrix(h);
        System.out.println("\n");
        Utils.printMatrix(h, s1, s2);
        findTranspositions(h, s1, s2);

        System.out.println("Final cost = " + h[s2.length()][s1.length()]);
        return h[s2.length()][s1.length()];
    }

    private static int min(int a, int b, int c){
        return Math.min(a, Math.min(b, c));
    }

    private static int min(int a, int b, int c, int d) {
        return Math.min(min(a, b, c), d);
    }

    private static int[][] reduceMatrix(int[][] matrix){
        int[][] reducedMatrix = new int[matrix.length - 1][matrix[0].length - 1];

        for(int i = 0; i < reducedMatrix.length; i++)
            for(int j = 0; j < reducedMatrix[i].length; j++)
                reducedMatrix[i][j] = matrix[i+1][j+1];

            return reducedMatrix;
    }

    private static void findTranspositions(int[][] matrix, String s1, String s2){
        int i = s2.length() - 1;
        int j = s1.length() - 1;
        while(i != 0 && j != 0){
            int bestScore = min(matrix[i-1][j-1], matrix[i][j-1], matrix[i-1][j]);
            if(matrix[i][j] == matrix[i-1][j-1] && s2.charAt(i-1) != s1.charAt(j-1) && matrix[i-1][j-1] == bestScore){
                System.out.println("Transposition found - " + s2.charAt(i-1) + " and " + s1.charAt(j-1));
                i--;
                j--;
            }
            else if(matrix[i][j] == matrix[i][j-1] + 1)
                j--;
            else if(matrix[i][j] == matrix[i - 1][j] + 1)
                i--;
            else{
                i--;
                j--;
            }
        }
    }

    static void readConfiguration(String config){
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader(config));
            JSONObject jsonObject = (JSONObject) obj;

            preprocessing = (Boolean) jsonObject.get("preprocessing");
            algorithm = PatternsMiner.SPMFAlgorithmName.valueOf(jsonObject.get("algorithm").toString());
            minSupport = (Double) jsonObject.get("minSupport");
            minCoverage = (Double) jsonObject.get("minCoverage");
            metric = jsonObject.get("metric").toString();
            segmented = (Boolean) jsonObject.get("segmented");

            JSONArray context = (JSONArray) jsonObject.get("context");
            List<String> temp = new ArrayList<>();
            if (context != null) {
                for (int i = 0; i < context.size(); i++){
                    temp.add(context.get(i).toString());
                }
                contextAttributes = new ArrayList<>(temp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}