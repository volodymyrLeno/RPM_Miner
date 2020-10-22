package Simplification.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WriteSimplifier {
    static String redundantEditCellRegex = ".*\"editCell\",(\"([^\"]|\"\")*\",){4}(\"([^\"]|\"\")*\"),.*\\n" +
            "(((\"([^\"]|\"\")*\",){3}((?!(\"getCell\"|\"copyCell\"),(\"([^\"]|\"\")*\",){4}\\3).)*\\n)*" +
            ".*\"editCell\",(\"([^\"]|\"\")*\",){4}\\3,.*\\n*)";

    static String redundantPasteIntoCellRegex = ".*\"pasteIntoCell\",(\"([^\"]|\"\")*\",){2}((\"([^\"]|\"\")*\",){3}).*\\n" +
            "(((\"([^\"]|\"\")*\",){3}((?!\"copyCell\",(\"([^\"]|\"\")*\",){2}\\3).)*\\n)*" +
            ".*\"pasteIntoCell\",(\"([^\"]|\"\")*\",){2}\\3.*\\n*)";

    static String redundantPasteIntoRangeRegex = ".*\"pasteIntoRange\",(\"([^\"]|\"\")*\",){2}((\"([^\"]|\"\")*\",){3}).*\\n" +
            "(((\"([^\"]|\"\")*\",){3}((?!\"copyRange\",(\"([^\"]|\"\")*\",){2}\\3).)*\\n)*" +
            ".*\"pasteIntoRange\",(\"([^\"]|\"\")*\",){2}\\3.*\\n*)";

    static String redundantDoublePasteRegex = ".*\"paste\",(\"([^\"]|\"\")*\",)(\"([^\"]|\"\")*\",)(\"([^\"]|\"\")*\",){6}(\"([^\"]|\"\")*\",)(\"([^\"]|\"\")*\",).*\\n" +
            "(((\"([^\"]|\"\")*\",){3}((?!\"copy\",(\"([^\"]|\"\")*\",){8}\\7).)*\\n)*" +
            ".*\"paste\",(\"([^\"]|\"\")*\",){2}(\"([^\"]|\"\")*\",){6}\\7\\9.*\\n*)";

    static String redundantDoubleEditFieldRegex = "(.*\"editField\",(\"([^\"]|\"\")*\",){8}(\"([^\"]|\"\")*\",)(\"([^\"]|\"\")*\",).*\\n)" +
            "(((\"([^\"]|\"\")*\",){3}((?!\"copy\",(((?!,).)*,){8}\\4).)*\\n)*" +
            ".*\"editField\",(\"([^\"]|\"\")*\",){8}\\4\\6.*\\n*)";

    static String pasteEditFieldWithoutCopyRegex = "((((?!\"paste\").)*\\n)*)" +
            "((.*\"paste\",(\"([^\"]|\"\")*\",){8}(\"([^\"]|\"\")*\"),.*\\n)*)" +
            "(((.*\\n)*)" +
            "(.*\"editField\",(\"([^\"]|\"\")*\",){8}(\"([^\"]|\"\")*\"),\"(([^\"]|\"\")*)\",.*\\n)" +
            "(" +
            "((\"([^\"]|\"\")*\",){3}((?!\"copy\",(\"([^\"]|\"\")*\",){8}\\16).)*\\n)*" +
            ".*\"editField\",(\"([^\"]|\"\")*\",){8}\\16,(((?!\\18).)*),.*\\n*))";

    static String pasteBetweenEditFieldsRegex = "(.*\"editField\",(\"([^\"]|\"\")*\",){8}(\"([^\"]|\"\")*\",)\"(([^\"]|\"\")*)\",.*\\n)" +
            "(" +
            "((\"([^\"]|\"\")*\",){3}\"paste\",(\"([^\"]|\"\")*\",)\"(([^\"]|\"\")*)\",(\"([^\"]|\"\")*\",){6}\\4.*\\n)*" +
            ".*\"editField\",(\"([^\"]|\"\")*\",){8}\\4\"(\\6\\14)\",.*\\n*)";


    static boolean containsRedundantEditCell(String log) {
        Pattern pattern = Pattern.compile(redundantEditCellRegex);
        Matcher matcher = pattern.matcher(log);

        return matcher.find();
    }

    static boolean containsRedundantPasteIntoCell(String log) {
        Pattern p = Pattern.compile(redundantPasteIntoCellRegex);
        Matcher matcher = p.matcher(log);

        return matcher.find();
    }

    static boolean containsRedundantPasteIntoRange(String log) {
        Pattern p = Pattern.compile(redundantPasteIntoRangeRegex);
        Matcher matcher = p.matcher(log);

        return matcher.find();
    }

    static boolean containsRedundantDoublePaste(String log) {
        Pattern p = Pattern.compile(redundantDoublePasteRegex);
        Matcher matcher = p.matcher(log);

        return matcher.find();
    }

    static boolean containsRedundantDoubleEditField(String log) {
        Pattern p = Pattern.compile(redundantDoubleEditFieldRegex);
        Matcher matcher = p.matcher(log);

        return matcher.find();
    }

    static boolean containsRedundantEditField(String log) {
        Pattern withoutCopyPattern = Pattern.compile(pasteEditFieldWithoutCopyRegex);
        Pattern pateBetweenEditFieldsPattern = Pattern.compile(pasteBetweenEditFieldsRegex);

        return withoutCopyPattern.matcher(log).find() &&
                !pateBetweenEditFieldsPattern.matcher(log).find();
    }


    static String removeRedundantEditCell(String log) {
        /*
            $5 is a parameter of WriteSimplifier#redundantEditCellRegex that
            represents every action after the first "editCell" action and
            the second "editCell" action in the pattern.
         */
        log = log.replaceAll(redundantEditCellRegex, "$5");

        if (containsRedundantEditCell(log)) {
            return removeRedundantEditCell(log);
        }

        return log;
    }

    static String removeRedundantPasteIntoCell(String log) {
        /*
            $6 is a parameter of WriteSimplifier#redundantPasteIntoCellRegex that
            represents every action after the first "pasteIntoCell" action
            and the second "pasteIntoCell" action in the pattern.
         */
        log = log.replaceAll(redundantPasteIntoCellRegex, "$6");

        if (containsRedundantPasteIntoCell(log)) {
            return removeRedundantPasteIntoCell(log);
        }

        return log;
    }

    static String removeRedundantPasteIntoRange(String log) {
        /*
            $6 is a parameter of WriteSimplifier#redundantPasteIntoRangeRegex that
            represents every action after the first "pasteIntoRange" action
            and the second "pasteIntoRange" action in the pattern.
         */
        log = log.replaceAll(redundantPasteIntoRangeRegex, "$6");

        if (containsRedundantPasteIntoRange(log)) {
            return removeRedundantPasteIntoRange(log);
        }

        return log;
    }

    static String removeRedundantDoublePaste(String log) {
        /*
            $11 is a parameter of WriteSimplifier#redundantPasteRegex that
            represents every action after the first "paste" action
            and the second "paste" action in the pattern.
         */
        log = log.replaceAll(redundantDoublePasteRegex, "$11");

        if (containsRedundantDoublePaste(log)) {
            return removeRedundantDoublePaste(log);
        }

        return log;
    }

    static String removeRedundantDoubleEditField(String log) {
        /*
            $8 is a parameter of WriteSimplifier#redundantDoubleEditFieldRegex that
            represents the second "editField" action.
         */
        log = log.replaceAll(redundantDoubleEditFieldRegex, "$8");

        if (containsRedundantDoubleEditField(log)) {
            return removeRedundantDoubleEditField(log);
        }

        return log;
    }

    static String removeRedundantEditField(String log) {
        Pattern withoutCopyPattern = Pattern.compile(pasteEditFieldWithoutCopyRegex);
        Matcher withoutCopyMatcher = withoutCopyPattern.matcher(log);

        /*
            group(8)    represents target name of the first "paste" action.
            group(16)   represents target name of the first "editField" action.
            group(1)    represents every action before the first "paste" action.
            group(10)   represents every action after the first "paste" action.
            group(11)   represents every action between the first "paste" action and
                        the first "editField" action.
            group(20)   represents every action after the first "editField" action.
         */
        if (withoutCopyMatcher.find()) {
            log = withoutCopyMatcher.replaceAll(mr -> {
                if (mr.group(8) != null &&
                        mr.group(16) != null &&
                        mr.group(8).equals(mr.group(16))) {
                    return mr.group(1) + mr.group(10);
                }
                return mr.group(1) + mr.group(4) + mr.group(11) + mr.group(20);
            });

            // $8 represents everything after the first "editField" action.
            log = log.replaceAll(pasteBetweenEditFieldsRegex, "$8");
            return removeRedundantEditField(log);
        }

        return log;
    }
}
