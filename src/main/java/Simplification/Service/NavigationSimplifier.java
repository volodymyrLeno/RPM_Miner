package Simplification.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NavigationSimplifier {
    private static String redundantClickTextFieldRegex = "((\"([^\"]|\"\")*\",){3}\"clickTextField\",.*\\n*)";

    private static String redundantExcelNavigation = "((\"([^\"]|\"\")*\",){3}\"getCell\",.*\\n*)|" +
            "((\"([^\"]|\"\")*\",){3}\"getRange\",.*\\n*)";

    static boolean containsRedundantClickTextField(String log) {
        Pattern pattern = Pattern.compile(redundantClickTextFieldRegex);
        Matcher matcher = pattern.matcher(log);

        return matcher.find();
    }

    static String removeRedundantClickTextField(String log) {
        if (containsRedundantClickTextField(log)) {
            log = log.replaceAll(redundantClickTextFieldRegex, "");
            return removeRedundantClickTextField(log);
        }

        return log;
    }

    static boolean containsRedundantExcelNavigation(String log){
        Pattern pattern = Pattern.compile(redundantExcelNavigation);
        Matcher matcher = pattern.matcher(log);

        return matcher.find();
    }

    static String removeExcelNavigation(String log) {
        if(containsRedundantExcelNavigation(log)){
            log = log.replaceAll(redundantExcelNavigation, "");
            return removeExcelNavigation(log);
        }

        return log;
    }
}
