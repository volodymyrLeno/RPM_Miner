package Aggregation.Service;

import Aggregation.Data.EquivalenceClass;
import data.Pattern;
import data.PatternItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class AggregationService {
    List<Pattern> patterns;
    int[][] patternsDistances;

    public AggregationService(){
        patterns = new ArrayList<>();
        patternsDistances = new int[patterns.size()][patterns.size()];
    }

    public AggregationService(List<Pattern> patterns){
        this.patterns = new ArrayList<>(patterns);
        patternsDistances = new int[patterns.size()][patterns.size()];
        for(int i = 0; i < patternsDistances.length; i++)
            for(int j = 0; j < patternsDistances[i].length; j++)
                patternsDistances[i][j] = 0;
    }

    public void computePatternsDistances(){
        int distance = 0;
        for(int i = 0; i < patterns.size(); i++){
            for(int j = i + 1; j < patterns.size(); j++) {
                distance = computeDistance(this.patterns.get(i), this.patterns.get(j));
                patternsDistances[i][j] = distance;
                patternsDistances[j][i] = distance;
            }
        }
    }

    private int computeDistance(Pattern pattern1, Pattern pattern2){
        List<PatternItem> p1 = pattern1.getItems();
        List<PatternItem> p2 = pattern2.getItems();

        if (p1 == null) {
            throw new NullPointerException("s1 must not be null");
        }

        if (p2 == null) {
            throw new NullPointerException("s2 must not be null");
        }

        if (p1.equals(p2)) {
            return 0;
        }

        // INFinite distance is the max possible distance
        int inf = p1.size() + p2.size();

        // Create and initialize the character array indices
        HashMap<String, Integer> da = new HashMap<>();

        for (int d = 0; d < p1.size(); d++) {
            da.put(p1.get(d).getValue(), 0);
        }

        for (int d = 0; d < p2.size(); d++) {
            da.put(p2.get(d).getValue(), 0);
        }

        // Create the distance matrix H[0 .. s1.length+1][0 .. s2.length+1]
        int[][] h = new int[p2.size() + 2][p1.size() + 2];

        // initialize the left and top edges of H
        for (int i = 0; i <= p2.size(); i++) {
            h[i + 1][0] = inf;
            h[i + 1][1] = i;
        }

        for (int j = 0; j <= p1.size(); j++) {
            h[0][j + 1] = inf;
            h[1][j + 1] = j;

        }

        // fill in the distance matrix H
        // look at each character in s1
        for (int i = 1; i <= p2.size(); i++) {
            int db = 0;

            // look at each character in b
            for (int j = 1; j <= p1.size(); j++) {
                int i1 = da.get(p1.get(j - 1).getValue());
                int j1 = db;

                int cost = 1;
                if (p2.get(i - 1).getValue().equals(p1.get(j - 1).getValue())) {
                    cost = 0;
                    db = j;
                }

                h[i + 1][j + 1] = min(
                        h[i][j] + cost, // substitution
                        h[i + 1][j] + 1, // insertion
                        h[i][j + 1] + 1, // deletion
                        h[i1][j1] + (i - i1 - 1) + 1 + (j - j1 - 1));
            }

            da.put(p2.get(i - 1).getValue(), i);
        }

        return h[p2.size()][p1.size()];
    }

    private int min(int a, int b, int c){
        return Math.min(a, Math.min(b, c));
    }

    private int min(int a, int b, int c, int d) {
        return Math.min(min(a, b, c), d);
    }

    public HashMap<EquivalenceClass, Pattern> getRepresentatives(HashMap<EquivalenceClass, List<Pattern>> clusters){
        HashMap<EquivalenceClass, Pattern> routines = new HashMap<>();
        for(var key: clusters.keySet()){
            List<Pattern> patterns = new ArrayList<>(clusters.get(key));
            patterns.sort(Comparator.comparing(Pattern::getAbsoluteSupport).reversed());
            Pattern bestRepresentative = patterns.get(0);
            Pattern finalBestRepresentative = bestRepresentative;
            patterns = patterns.stream().filter(pattern -> pattern.getAbsoluteSupport() >= finalBestRepresentative.getAbsoluteSupport()).collect(Collectors.toList());
            if(patterns.size() > 1){
                patterns.sort(Comparator.comparing(Pattern::getLength));
                bestRepresentative = patterns.get(0);
            }
            routines.put(key, bestRepresentative);
        }
        return routines;
    }
}
