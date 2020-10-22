package AutomatabilityAssessment.Service.Tane;

import AutomatabilityAssessment.Data.TaneDependency;
import Utils.CaseService;
import data.Pattern;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TaneExecutor {
    private static String tanePath = "tane-1.0";
    private List<List<String>> instances;
    private Pattern pattern;
    private InstanceParser instanceParser;

    public TaneExecutor(Pattern pattern, CaseService caseService) {
        this.pattern = pattern;
        this.instanceParser = new InstanceParser(caseService);
    }

    public static void setTanePath(String tanePath) {
        TaneExecutor.tanePath = tanePath;
    }

    public List<List<String>> getInstances() {
        return instances;
    }

    public void createInstancesFile() {
        this.instances = instanceParser.getInstances(pattern);
        File file = new File(tanePath + "/original/data.orig");

        try {
            FileWriter writer = new FileWriter(file);
            writer.write(getFormattedInstances(instances));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createTaneDataFiles() {
        try {
            ProcessBuilder pb = new ProcessBuilder()
                    .directory(new File(tanePath + "/original"))
                    .command("bash", "-c", "../bin/select.perl ../descriptions/data.dsc");
            Process p = pb.start();
            p.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public List<TaneDependency> getFunctionalDependencies() {
        List<TaneDependency> functionalDependencies = new ArrayList<>();
        createFDFile();
        try (Stream<String> stream = Files.lines(Paths.get(tanePath + "/functional_dependencies.txt"))) {
            stream.filter(line -> line.contains("->") && !line.contains("key")).forEach(line -> {
                String[] dependency = line.split("->");
                if (dependency[0].split(" ").length > 1) {
                    for (String s : dependency[0].split(" ")) {
                        int dependee = s.isEmpty() ? -1 : Integer.parseInt(s.trim()) - 1;
                        int depender = dependency[1].isEmpty() ? -1 : Integer.parseInt(dependency[1].trim()) - 1;
                        functionalDependencies.add(new TaneDependency(dependee, depender));
                    }
                } else {
                    int dependee = dependency[0].isEmpty() ? -1 : Integer.parseInt(dependency[0].trim()) - 1;
                    int depender = dependency[1].isEmpty() ? -1 : Integer.parseInt(dependency[1].trim()) - 1;
                    functionalDependencies.add(new TaneDependency(dependee, depender));
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return functionalDependencies;
    }

    private void createFDFile() {
        try {
            ProcessBuilder pb = new ProcessBuilder()
                    .directory(new File(tanePath))
                    .command(Arrays.asList("bash", "-c", String.format("bin/tane 20 %d %d data/data.dat > functional_dependencies.txt",
                            instances.size(), pattern.getItems().size())));
            Process p = pb.start();
            p.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String getFormattedInstances(List<List<String>> instances) {
        StringBuilder sb = new StringBuilder();
        instances.forEach(instance -> {
            sb.append(instance.stream().collect(Collectors.joining(",", "", "")));
            sb.append("\n");
        });

        return sb.toString();
    }
}
