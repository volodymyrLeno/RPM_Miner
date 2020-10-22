package Segmentation.Service;

import Segmentation.Data.DirectlyFollowsGraph;
import Segmentation.Data.Edge;
import Segmentation.Data.Node;
import Utils.LogReader;
import Utils.LogWriter;
import Utils.Parser;
import Utils.Utils;
import data.Event;
import ee.ut.comptech.DominatorTree;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.toMap;

public class SegmentsDiscoverer {
    public SegmentsDiscoverer() {}

    DominatorTree domTree;
    Map<Node, Integer> nodeIDs;
    Map<Integer, Node> idToNode;
    int NID;

    private void generateDominatorsTree(DirectlyFollowsGraph dfg){
        NID = 0;
        nodeIDs = new HashMap<>();
        idToNode = new HashMap<>();
        Map<Integer, List<Integer>> reachableNodes = new HashMap<>();
        for(Node node: dfg.getNodes()){
            NID++;
            reachableNodes.put(NID, new ArrayList<>());
            nodeIDs.put(node, NID);
            idToNode.put(NID, node);
        }
        for(Node node: dfg.getNodes())
            if(dfg.getOutgoingEdges().containsKey(node))
                for (var successor : dfg.getOutgoingEdges().get(node))
                    reachableNodes.get(nodeIDs.get(node)).add(nodeIDs.get(successor.getTarget()));

        domTree = new DominatorTree(reachableNodes);
        domTree.analyse(nodeIDs.get(dfg.getNodes().get(0)));
    }

    private Node getDominator(Node node){
        int domID = domTree.getInfo(nodeIDs.get(node)).getDom().getNode();
        Node dominator = idToNode.get(domID);
        return dominator;
    }

    public void segmentLog(String log, String config){
        List<Event> events = LogReader.readCSV(log);
        List<String> contextAttributes = Parser.extractContextAttributes(config);
        var groupedEvents = Utils.groupEvents(events);
        for(var key: groupedEvents.keySet())
            Parser.setContextAttributes(groupedEvents.get(key), contextAttributes);
        var dfg = new DirectlyFollowsGraph(events);
        dfg.buildGraph();

        var cases = extractSegmentsFromDFG(dfg);
        LogWriter.writeSegments(log.substring(0, log.lastIndexOf(".")) + "_segmented.csv", cases);
    }

    public HashMap<Integer, List<Event>> extractSegmentsFromDFG(DirectlyFollowsGraph dfg){
        generateDominatorsTree(dfg);
        List<Edge> loops = new ArrayList<>();
        HashMap<Edge, List<Node>> container = new HashMap<>();
        discoverBackEdges(dfg, loops, container, 0);
        return discoverSegments(dfg, loops, container);
    }

    public HashMap<Integer, List<Event>> extractSegments(List<Event> events){
        DirectlyFollowsGraph dfg = new DirectlyFollowsGraph(events);
        dfg.buildGraph();
        generateDominatorsTree(dfg);
        List<Edge> loops = new ArrayList<>();
        HashMap<Edge, List<Node>> container = new HashMap<>();
        discoverBackEdges(dfg, loops, container, 0);
        return discoverSegments(dfg, loops, container);
    }

    private HashMap<Integer, List<Event>> discoverSegments(DirectlyFollowsGraph dfg, List<Edge> loops, HashMap<Edge, List<Node>> container){
        HashMap<Integer, List<Event>> segments = new HashMap<>();
        List<Event> uiLog = dfg.getEvents();

        int eCounts = uiLog.size();
        Event next = null;

        uiLog.get(eCounts-1).setEnd(true);

        /*
        var rank1 = rankByFrequency(loops);
        var rank2 = rankByLogLength(loops);
        var rank3 = rankByGraphDistance(loops, dfg);

        List<List<Edge>> rankings = new ArrayList<>(){{ add(rank1); add(rank2); add(rank3); }};
        var overallRank = getAggregatedRanking(rankings);
        */

        int lCount = 0;

        HashMap<Event, List<Event>> startMatches = new HashMap<>();

        for(Edge loop: loops){
            for(Event start: uiLog){
                if(start.getEventType().equals(loop.getTarget().getEventType()) && start.context.equals(loop.getTarget().getContext())){
                    uiLog.get(start.getID()).setStart(true);
                    if(!startMatches.containsKey(start))
                        startMatches.put(start, new ArrayList<>(loop.getSourceEvents().stream().filter(event ->
                                event.getTimestamp().compareTo(start.getTimestamp()) > 0).collect(Collectors.toList())));
                    else
                        startMatches.put(start, new ArrayList<>(Stream.concat(startMatches.get(start).stream(),
                                loop.getSourceEvents().stream().filter(event ->
                                        event.getTimestamp().compareTo(start.getTimestamp()) > 0)).collect(Collectors.toList())));
                }
            }
            for(Event end: uiLog){
                if(end.getEventType().equals(loop.getSource().getEventType()) && end.context.equals(loop.getSource().getContext()))
                    uiLog.get(end.getID()).setEnd(true);
            }
        }

        int i = 0;
        int caseID = 0;
        boolean within = false;
        List<Event> segment = null;
        Event start = null;
        int totalLength = 0;
        do {
            next = uiLog.get(i);
            i++;

            if(within) {
                if (isCovered(new Node(start), new Node(next), container, loops)) {
                    segment.add(next);
                    if (next.isEnd() && (startMatches.get(start).contains(next) || i == eCounts)) {
                        segments.put(caseID, segment);
                        caseID++;
                        within = false;
                        totalLength += segment.size();
                        //System.out.println("DEBUG - discovered segment of length: " + segment.size());
                    }
                } else {
                    segments.put(caseID, segment);
                    caseID++;
                    within = false;
                    totalLength += segment.size();
                    i--;
                }
            } else if(next.isStart()) {
                segment = new ArrayList<>();
                segment.add(next);
                within = true;
                start = next;
            }
        } while(i!=eCounts);

        //System.out.println("\n\tDEBUG - total segments discovered: " + caseID);
        //System.out.println("\tDEBUG - total events ("+ i +") into segments: " + totalLength);
        //System.out.println("Average length: " + totalLength/caseID);
        //System.out.println("Vertices: " + dfg.getNodes().size() + ", Edges: " + dfg.getEdges().size());
        return segments;
    }

    private boolean isCovered(Node start, Node current, HashMap<Edge, List<Node>> container, List<Edge> backEdges){
        List<Edge> edges = backEdges.stream().filter(edge -> edge.getTarget().equals(start)).collect(Collectors.toList());
        for(var edge: edges){
            if(container.get(edge).contains(current))
                return true;
        }
        return false;
    }

    private List<Edge> discoverBackEdges(DirectlyFollowsGraph dfg, List<Edge> loops, HashMap<Edge, List<Node>> container, int i){
        var k = i+1;
        List<DirectlyFollowsGraph> sccs = dfg.getSCComponents(dfg.getAdjacencyMatrix());
        for(var scc: sccs){
            //System.out.println("SCC (nodes = " + scc.getNodes().size() + ", edges = " + scc.getEdges().size() + ")");
            if(scc.getNodes().size() > 1){
                var backEdges = getBackEdges(scc);

                if(backEdges == null){
                    List<Edge> loopCandidates = rankByGraphDistance(scc.identifyLoops(scc.getAdjacencyMatrix(), 0), scc);
                    scc.removeEdges(Collections.singletonList(loopCandidates.get(0)));
                    discoverBackEdges(scc, loops, container, i);
                }
                else{
                    //System.out.println("DEBUG - " + backEdges + " (level = " + k + ")");
                    loops.addAll(new ArrayList<>(backEdges));

                    for(var be: backEdges){
                        if(!container.containsKey(be))
                            container.put(be, new ArrayList<>(scc.getNodes()));
                    }

                    scc.removeEdges(backEdges);
                    discoverBackEdges(scc, loops, container, k);
                }
            }
        }
        return loops;
    }

    private List<Edge> getBackEdges(DirectlyFollowsGraph scc){
        List<Edge> backEdges = new ArrayList<>();

        var header = getHeader(scc);

        if(header == null){
            return null;
        }
        else{
            for(var edge: scc.getEdges())
                if(edge.getTarget().equals(header))
                    backEdges.add(edge);
            return backEdges;
        }
    }

    private Node getHeader(DirectlyFollowsGraph scc){
        Node header = null;
        HashMap<Node, Node> loop = new HashMap<>();

        for(var node: scc.getNodes())
            loop.put(node, getDominator(node));

        for(var node: loop.keySet()){
            if(!scc.getNodes().contains(loop.get(node)))
                if(header == null)
                    header = new Node(node);
                else
                    return null;
        }

        return header;
    }

    private List<Edge> rankByFrequency(List<Edge> edges){
        List<Edge> rankedEdges = new ArrayList<>(edges);
        rankedEdges.sort(comparing(Edge::getFrequency).reversed());
        return rankedEdges;
    }

    private List<Edge> rankByLogLength(List<Edge> edges){
        List<Edge> rankedEdges = new ArrayList<>(edges);
        rankedEdges.sort(comparing(Edge::getAvgLogLength).reversed());
        return rankedEdges;
    }

    private List<Edge> rankByGraphDistance(List<Edge> edges, DirectlyFollowsGraph dfg){
        HashMap<Edge, Integer> longestDistance = new HashMap<>();

        for(var edge: edges)
            longestDistance.put(edge, dfg.getLongestPath(edge.getTarget(), edge.getSource()));

        Map<Edge, Integer> sorted = longestDistance.entrySet().stream().sorted(Collections.reverseOrder(comparingByValue()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));

        return new ArrayList<>(sorted.keySet());
    }

    private List<Edge> getAggregatedRanking(List<List<Edge>> rankings){
        List<Edge> edges = new ArrayList<>(rankings.get(0));
        HashMap<Edge, Double> scores = new HashMap<>();
        for(var edge: edges){
            double score = 0.0;
            for(var ranking: rankings)
                score += ranking.indexOf(edge);
            scores.put(edge, score/rankings.size());
        }
        Map<Edge, Double> sorted = scores.entrySet().stream().sorted(comparingByValue())
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
        return new ArrayList<>(sorted.keySet());
    }
}
