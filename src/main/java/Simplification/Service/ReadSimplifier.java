package Simplification.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReadSimplifier {
    static String redundantFirstCopyRegex = "((\"([^\"]|\"\")*\",){3}\"copy.*\\n)" +
            "((((?!(\"([^\"]|\"\")*\",){3}\"paste).)*\",.*\\n)*" +
            "(\"([^\"]|\"\")*\",){3}\"copy.*\\n*)";

    static String singleCopyRegex = "((\"([^\"]|\"\")*\",){3}(\"copy[a-zA-Z]*\",)(\"([^\"]|\"\")*\",)(\"([^\"]|\"\")*\",).*\\n)" +
            "(((\"([^\"]|\"\")*\",){3}(?!(\"paste[a-zA-Z]*\"|\"copy[a-zA-Z]*\")).*\\n)*" +
            "((\"([^\"]|\"\")*\",){3}(\"paste[a-zA-Z]*\",(\"([^\"]|\"\")*\",)(?!\\7)).*\\n*))";

    static boolean containsRedundantCopy(String log) {
        Pattern p = Pattern.compile(redundantFirstCopyRegex);
        Matcher matcher = p.matcher(log);

        return matcher.find();
    }

    static boolean containsSingleCopy(String log) {
        Pattern p = Pattern.compile(singleCopyRegex);
        Matcher matcher = p.matcher(log);

        return matcher.find();
    }

    static String removeRedundantCopy(String log) {
        if (containsRedundantCopy(log)) {
            log = log.replaceAll(redundantFirstCopyRegex, "$4");
            return removeRedundantCopy(log);
        }

        return log;
    }

    static String removeSingleCopy(String log) {
        if (containsSingleCopy(log)) {
            log = log.replaceAll(singleCopyRegex, "$9");
            return removeSingleCopy(log);
        }

        return log;
    }
}
