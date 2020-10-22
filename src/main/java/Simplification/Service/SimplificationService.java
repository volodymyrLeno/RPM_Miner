package Simplification.Service;

import Simplification.Utils.SimplificationUtils;
import data.Event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimplificationService {
    public static List<Event> applyPreprocessing(List<Event> ev, List<String> context){
        /*
        String[] header = {"timeStamp", "userID", "targetApp", "eventType", "url", "content", "target.workbookName",
                "target.sheetName", "target.id", "target.class", "target.tagName", "target.type", "target.name",
                "target.value", "target.innerText", "target.checked", "target.href", "target.option", "target.title", "target.innerHTML"};
        */
        String events = SimplificationUtils.eventListToString(ev);

        List<String> attributes = ev.get(0).getAttributes();

        events = Preprocessing.sortLog(events);
        events = Preprocessing.deleteChromeClipboardCopy(events);
        events = Preprocessing.mergeNavigationCellCopy(events);
        events = Preprocessing.identifyPasteAction(events);

        events = NavigationSimplifier.removeRedundantClickTextField(events);
        //events = NavigationSimplifier.removeExcelNavigation(events);

        while (ReadSimplifier.containsRedundantCopy(events) ||
                ReadSimplifier.containsSingleCopy(events)) {
            events = ReadSimplifier.removeRedundantCopy(events);
            events = ReadSimplifier.removeSingleCopy(events);
        }

        return SimplificationUtils.toEvents(events, 0, attributes, -1, context);
    }

    public static Map<Integer, List<Event>> applyPreprocessing(Map<Integer, List<Event>> cases, List<String> context){
        Map<Integer, List<Event>> preprocessedCases = new HashMap<>();

        /*
        String[] header = {"caseID", "timeStamp", "userID", "targetApp", "eventType", "url", "content", "target.workbookName",
                "target.sheetName", "target.id", "target.class", "target.tagName", "target.type", "target.name",
                "target.value", "target.innerText", "target.checked", "target.href", "target.option", "target.title", "target.innerHTML"};
        */

        int offset = 0;

        for(var key: cases.keySet()){
            List<String> attributes = cases.get(key).get(0).getAttributes();

            String events = SimplificationUtils.eventListToString(cases.get(key));
            events = Preprocessing.sortLog(events);
            events = Preprocessing.deleteChromeClipboardCopy(events);
            events = Preprocessing.mergeNavigationCellCopy(events);
            events = Preprocessing.identifyPasteAction(events);

            events = NavigationSimplifier.removeRedundantClickTextField(events);
            //events = NavigationSimplifier.removeExcelNavigation(events);

            while (ReadSimplifier.containsRedundantCopy(events) ||
                    ReadSimplifier.containsSingleCopy(events) ||
                    WriteSimplifier.containsRedundantDoublePaste(events) ||
                    WriteSimplifier.containsRedundantEditCell(events) ||
                    WriteSimplifier.containsRedundantEditField(events) ||
                    WriteSimplifier.containsRedundantPasteIntoCell(events) ||
                    WriteSimplifier.containsRedundantPasteIntoRange(events) ||
                    WriteSimplifier.containsRedundantDoubleEditField(events)){

                events = ReadSimplifier.removeRedundantCopy(events);
                events = ReadSimplifier.removeSingleCopy(events);
                events = WriteSimplifier.removeRedundantDoublePaste(events);
                events = WriteSimplifier.removeRedundantEditCell(events);
                events = WriteSimplifier.removeRedundantEditField(events);
                events = WriteSimplifier.removeRedundantPasteIntoCell(events);
                events = WriteSimplifier.removeRedundantPasteIntoRange(events);
                events = WriteSimplifier.removeRedundantDoubleEditField(events);
            }

            preprocessedCases.put(key, SimplificationUtils.toEvents(events, offset, attributes, key, context));
            offset += cases.get(key).size();
        }

        return preprocessedCases;
    }
}
