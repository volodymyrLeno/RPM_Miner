package Aggregation.Data;

import data.Pattern;
import lombok.Data;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
public class DataFlowGraph {

    List<String> elements;
    List<Pair<String, String>> links;
    HashMap<String, Set<Pair<String, String>>> dependencyChains;
    List<String> systemChanges;

    public DataFlowGraph(){
        elements = new ArrayList<>();
        links = new ArrayList<>();
        dependencyChains = new HashMap<>();
        systemChanges = new ArrayList<>();
    }

    public void constructDataFlowGraph(Pattern pattern){
        var transformations = pattern.getTransformations();
        transformations.values().removeIf(value -> value.isEmpty());
        var dependencies = pattern.getItemsDependencies();

        for(var patternItem: pattern.getItemsValues())
            if(patternItem.contains("clickButton") || patternItem.contains("clickLink"))
                systemChanges.add(patternItem);

        for(var key:transformations.keySet()){
            if(!transformations.get(key).equals("")){
                var source = key.getKey().getContextId();
                var target = key.getValue().getContextId();
                addElement(source);
                addElement(target);
                addLink(source, target);
            }
        }
        for(var dependency: dependencies){
            if(dependency.getDependee() != null){
                List<String> sources = dependency.getDependee().stream().map(el -> el.getContextId()).collect(Collectors.toList());
                var target = dependency.getDepender().getContextId();
                for(var source: sources){
                    addElement(source);
                    addElement(target);
                    addLink(source, target);
                }
            }
        }
        generateDotFile();
        constructDependencyChains();
    }

    private void constructDependencyChains(){
        dependencyChains = new HashMap<>();
        for(var element: elements)
            getDependencies(element, element, links.size() - 1);
    }

    private void getDependencies(String element, String currentElement, int currentPos){
        for(int i = currentPos; i >= 0; i--){
            if(links.get(i).getValue().equals(currentElement)){
                if(!dependencyChains.containsKey(element))
                    dependencyChains.put(element, Collections.singleton(links.get(i)));
                else
                    dependencyChains.put(element, Stream.concat(dependencyChains.get(element).stream(),
                            Stream.of(links.get(i))).collect(Collectors.toSet()));
                getDependencies(element, links.get(i).getKey(), i);
            }
        }
    }

    /*
    private List<Pair<String, String>> getDependencyChain(String element){
        List<List<String>> dependencyChain = new ArrayList<>();
        String currentElement = element;
        for(int i = links.size()-1; i >= 0; i--){
            if(links.get(i).getValue().equals(currentElement)){
                    currentElement = links.get(i).getKey();
                    dependencyChain.add(currentElement);
                }
            }
        return dependencyChain;
    }
     */

    private void addElement(String element){
        if(!elements.contains(element))
            elements.add(element);
    }

    private void addLink(String source, String target){
        Pair<String, String> link = new ImmutablePair<>(source, target);
        if(!links.contains(link))
            links.add(link);
    }

    private void generateDotFile(){
        String DOT = "digraph g {\n";
        long startTime = System.currentTimeMillis();
        for(Pair<String, String> link: this.links)
            DOT = DOT + "\t" + link.getKey().replaceAll("[^a-zA-Z0-9]+", "_") + " -> " +
                    link.getValue().replaceAll("[^a-zA-Z0-9]+", "_") + "\n";
        DOT = DOT + "}";
        try{
            PrintWriter writer = new PrintWriter("DataFlowGraph.dot");
            writer.print(DOT);
            writer.close();
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    @Override
    public boolean equals(Object obj){
        if(obj != null && getClass() == obj.getClass()){
            DataFlowGraph dataFlowGraph = (DataFlowGraph) obj;

            List<String> elements = new ArrayList<>(dataFlowGraph.getElements());
            List<String> thisElements = new ArrayList<>(this.elements);
            Collections.sort(elements);
            Collections.sort(thisElements);

            List<Pair<String, String>> links = new ArrayList<>(dataFlowGraph.getLinks());
            List<Pair<String, String>> thisLinks = new ArrayList<>(this.links);
            Collections.sort(links);
            Collections.sort(thisLinks);

            return thisElements.equals(elements) && thisLinks.equals(links);
        }
        return false;
    }

    @Override
    public int hashCode(){
        return Objects.hash(elements, links);
    }
}
