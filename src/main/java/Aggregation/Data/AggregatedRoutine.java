package Aggregation.Data;

import Utils.Utils;
import data.Pattern;
import data.PatternItem;

import java.util.HashMap;
import java.util.List;

public class AggregatedRoutine {
    List<Pattern> coveredRoutines;

    /*
    public Pattern findClosestRoutine(List<Pattern> routines){
        Pattern closestRoutine = routines.get(0);
        int minDistance = getMinDistance(closestRoutine);
        for(int i = 1; i < routines.size(); i++){
            int distance = getMinDistance(routines.get(i));
            if(minDistance > distance){
                closestRoutine = routines.get(i);
                minDistance = distance;
            }
        }
        return closestRoutine;
    }

    private int getMinDistance(Pattern p1){
        int minDistance = getDistance(p1, this.coveredRoutines.get(0));

        if(this.coveredRoutines.size() > 1)
            for(int i = 1; i < this.coveredRoutines.size(); i++){
                int distance = getDistance(p1, this.coveredRoutines.get(i));
                if(minDistance > distance)
                    minDistance = distance;
            }

        return minDistance;
    }
     */


    public void addRoutine(Pattern pattern){ this.coveredRoutines.add(pattern); }


}
