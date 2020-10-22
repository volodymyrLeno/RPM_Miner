package Simplification.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Preprocessing {
    static String sortLog(String log) {
        System.out.print("\tSorting the log");
        long startTime = System.currentTimeMillis();
        List<String> actions = Arrays.asList(log.split("\n"));
        Collections.sort(actions);
        long stopTime = System.currentTimeMillis();
        System.out.println(" (" + (stopTime - startTime) / 1000.0 + " sec)");
        return actions.stream().map(el -> el + "\n").collect(Collectors.joining());
    }

    static String identifyPasteAction(String log) {
        log = transformCopyCellEditToPaste(log);
        log = transformCopyRangeEditToPaste(log);
        log = transformChromeCopyEditToPaste(log);

        return log;
    }

    static String transformCopyCellEditToPaste(String log) {
        String cellRegex = "(.*\"copyCell\",(\"([^\"]|\"\")*\",)(\"([^\"]|\"\")*\"),.*\\n)" +
                "((.*\\n)*)" +
                "((.*)\"editCell\",(\"([^\"]|\"\")*\",)(\"([^\"]|\"\")*\",)((\"([^\"]|\"\")*\",){7}\\4.*)\\n*)";

        if (log.contains("editCell") && Pattern.compile(cellRegex).matcher(log).find()) {
            log = log.replaceAll(cellRegex, "$1$6$9\"pasteIntoCell\",$10$4,$14\n");

            return transformCopyCellEditToPaste(log);
        }

        return log;
    }

    static String transformCopyRangeEditToPaste(String log) {
        String rangeRegex = "(.*\"copyRange\",(\"([^\"]|\"\")*\",)(\"([^\"]|\"\")*\",)(\"([^\"]|\"\")*\",){7}(\"([^\"]|\"\")*\",).*\\n)" +
                "((.*\\n)*)" +
                "((.*)\"editRange\",(\"([^\"]|\"\")*\",)(\"([^\"]|\"\")*\",)((\"([^\"]|\"\")*\",){7}\\8.*)\\n*)";

        if (log.contains("editRange") && Pattern.compile(rangeRegex).matcher(log).find()) {
            log = log.replaceAll(rangeRegex, "$1$10$13\"pasteIntoRange\",$14$4$18\n");

            return transformCopyRangeEditToPaste(log);
        }

        return log;
    }

    static String transformChromeCopyEditToPaste(String log) {
        String chromeRegex = "(.*\"Chrome\",\"copy\",(\"([^\"]|\"\")*\",)(\"([^\"]|\"\")*\",).*\\n)" +
                "((.*\\n)*)" +
                "((.*)\"editCell\",(\"([^\"]|\"\")*\",)(\"([^\"]|\"\")*\",)((\"([^\"]|\"\")*\",){7}\\4.*\\n*))";

        if (log.contains("editCell") && Pattern.compile(chromeRegex).matcher(log).find()) {
            log = log.replaceAll(chromeRegex, "$1$6$9\"pasteIntoCell\",$10$4$14\n");

            return transformChromeCopyEditToPaste(log);
        }

        return log;
    }

    static String mergeNavigationCellCopy(String log) {
        log = mergeGetCellCopy(log);
        log = mergeGetRangeCopy(log);
        log = mergeEditCellCopy(log);

        log = log.replaceAll("((\"([^\"]|\"\")*\",){3}\"getCell\",.*\\n*)|" +
                "((\"([^\"]|\"\")*\",){3}\"getRange\",.*\\n*)", "");

        return log;
    }

    static String mergeGetCellCopy(String log) {
        String getCellRegex = "((\"([^\"]|\"\")*\",)((\"([^\"]|\"\")*\",){2})\"getCell\",(\"([^\"]|\"\")*\",){2}(.*)\\n" +
                "(((?!(\"([^\"]|\"\")*\",){3}(\"editCell\"|\"getRange\"|\"getCell\"),(\"([^\"]|\"\")*\",){9}).)*\\n)*)" +
                "(\"([^\"]|\"\")*\",)(\"([^\"]|\"\")*\",)\"OS-Clipboard\",\"copy\",((\"([^\"]|\"\")*\",){2}).*\\n*";

        if (log.contains("getCell") && Pattern.compile(getCellRegex).matcher(log).find()) {
            log = log.replaceAll(getCellRegex, "$1$17$4\"copyCell\",$21$9\n");

            return mergeGetCellCopy(log);
        }

        return log;
    }

    static String mergeGetRangeCopy(String log) {
        String getRangeRegex = "((\"([^\"]|\"\")*\",)((\"([^\"]|\"\")*\",){2})\"getRange\",(\"([^\"]|\"\")*\",){2}(.*)\\n" +
                "(((?!(\"([^\"]|\"\")*\",){3}(\"editCell\"|\"getRange\"|\"getCell\"),(\"([^\"]|\"\")*\",){9}).)*\\n)*)" +
                "(\"([^\"]|\"\")*\",)(\"([^\"]|\"\")*\",)\"OS-Clipboard\",\"copy\",((((?!,).)*,){2}).*\\n*";

        if (log.contains("getRange") && Pattern.compile(getRangeRegex).matcher(log).find()) {
            log = log.replaceAll(getRangeRegex, "$1$17$4\"copyRange\",$21$9\n");

            return mergeGetRangeCopy(log);
        }

        return log;
    }

    static String mergeEditCellCopy(String log) {
        String editCellRegex = "((\"([^\"]|\"\")*\",)((\"([^\"]|\"\")*\",){2})\"editCell\",(\"([^\"]|\"\")*\",){2}(.*)\\n" +
                "(((?!(\"([^\"]|\"\")*\",){3}(\"editCell\"|\"getRange\"|\"getCell\"),(\"([^\"]|\"\")*\",){9}).)*\\n)*)" +
                "(\"([^\"]|\"\")*\",)(\"([^\"]|\"\")*\",)\"OS-Clipboard\",\"copy\",((\"([^\"]|\"\")*\",){2}).*\\n*";

        if (log.contains("editCell") && Pattern.compile(editCellRegex).matcher(log).find()) {
            log = log.replaceAll(editCellRegex, "$1$17$4\"copyCell\",$21$9\n");

            return mergeEditCellCopy(log);
        }

        return log;
    }

    static String deleteChromeClipboardCopy(String log) {
        String regex = "((\"([^\"]|\"\")*\",){2}\"Chrome\",\"copy\",.*\\n)" +
                "((\"([^\"]|\"\")*\",){2}\"OS-Clipboard\",\"copy\",.*\\n*)";

        Pattern p = Pattern.compile(regex);
        Matcher matcher = p.matcher(log);

        if (matcher.find()) {
            log = log.replaceAll(regex, "$1");
            return deleteChromeClipboardCopy(log);
        }
        return log;
    }
}
