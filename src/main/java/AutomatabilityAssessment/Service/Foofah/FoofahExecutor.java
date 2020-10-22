package AutomatabilityAssessment.Service.Foofah;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class FoofahExecutor {
    private static String foofahPath = "foofah";

    public static String getFoofahPath() {
        return foofahPath;
    }

    public static String execPython() {
        String output = null;

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", "python " + foofahPath +
                "/foofah.py --input " + foofahPath + "/foofah.temp --timeout 120");

        try {
            StringBuilder sb = new StringBuilder();
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }

            int exitVal = process.waitFor();
            if (exitVal == 0 && sb.toString().length() != 0 && !sb.toString().contains("Not Found")) {
                output = sb.substring(sb.indexOf("#\n# Data Transformation\n#") + 26);
                output = output.equals("\n") ? "Equality" : output;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return output;
    }
}
