/*
 * This file is part of the Translation Tools, modified on 27.08.17 22:49.
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See the COPYING file for more details.
 */

/*
 * This file is part of the Translation Tools, modified on 27.08.17 22:49.
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See the COPYING file for more details.
 */

package dialog.parser;

import dialog.TranslationString;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is responsible for parsing a script file and extracting all string IDs.
 */
public class ScriptContentParser implements IParser {

    private final static Pattern ADDJOURNALENTRY = Pattern.compile("AddJournalEntry\\((\\d+),[^)]+\\)( {2}// ([^\\n]+))?");
    private final static Pattern DISPLAYSTRINGHEAD =
            Pattern.compile("DisplayStringHead\\(\"([^\"]+)\",(\\d+)\\)( {2}// ([^\\n]+))?");
    private final static Pattern DISPLAYSTRINGWAIT =
            Pattern.compile("DisplayStringWait\\(\"([^\"]+)\",(\\d+)\\)( {2}// ([^\\n]+))?");

    private final SortedMap<Integer, TranslationString> idsToDialogs;
    private SortedMap<String, List<Integer>> fileNamesToIds;

    /**
     * Constructs a new content parser that parses a TLK file. The extracted string IDs are stored in the given maps.
     *
     * @param idsToDialogs   the map where the relations between string IDs and dialog texts are stored
     * @param fileNamesToIds the map where the relations between file names and string IDs are stored
     */
    public ScriptContentParser(SortedMap<Integer, TranslationString> idsToDialogs, SortedMap<String, List<Integer>> fileNamesToIds) {
        this.idsToDialogs = idsToDialogs;
        this.fileNamesToIds = fileNamesToIds;
    }

    @Override
    public void parse(Path file) throws IOException {
        // Read the file
        String content = Files.readString(file);
        String filename = file.getFileName().toString();

        // Initialize a new ID list for the given filename
        List<Integer> ids = new LinkedList<>();

        // Search for all occurrences of "ADDJOURNALENTRY"
        Matcher addJournalEntryMatcher = ADDJOURNALENTRY.matcher(content);
        while (addJournalEntryMatcher.find()) {
            // Parse the corresponding string ID
            int id = Integer.parseInt(addJournalEntryMatcher.group(1));
            // If the string ID was already parsed in a dialog file, move on
            if (idsToDialogs.containsKey(id)) {
                continue;
            }
            // Parse the corresponding string text
            TranslationString dialogString = TranslationString
                    .create(addJournalEntryMatcher.group(addJournalEntryMatcher.groupCount()),
                            TranslationString.Type.SCRIPT_JOURNAL, filename);
            // Store the ADDJOURNALENTRY string with its IDs
            idsToDialogs.put(id, dialogString);
            ids.add(id);
        }

        // Search for all occurrences of "DISPLAYSTRINGHEAD"
        Matcher displayStringHeadMatcher = DISPLAYSTRINGHEAD.matcher(content);
        while (displayStringHeadMatcher.find()) {
            // Parse the corresponding string ID
            int id = Integer.parseInt(displayStringHeadMatcher.group(2));
            // If the string ID was already parsed in a dialog file, move on
            if (idsToDialogs.containsKey(id)) {
                continue;
            }
            // Parse the corresponding string text
            TranslationString dialogString = TranslationString.create("(" + displayStringHeadMatcher.group(1) + ") " +
                            displayStringHeadMatcher.group(displayStringHeadMatcher.groupCount()), TranslationString.Type.SCRIPT_HEAD,
                    filename);
            // Store the DISPLAYSTRINGHEAD string with its IDs
            idsToDialogs.put(id, dialogString);
            ids.add(id);
        }

        // Search for all occurrences of "DISPLAYSTRINGWAIT"
        Matcher displayStringWaitMatcher = DISPLAYSTRINGWAIT.matcher(content);
        while (displayStringWaitMatcher.find()) {
            // Parse the corresponding string ID
            int id = Integer.parseInt(displayStringWaitMatcher.group(2));
            // If the string ID was already parsed in a dialog file, move on
            if (idsToDialogs.containsKey(id)) {
                continue;
            }
            // Parse the corresponding string text
            TranslationString dialogString = TranslationString.create("(" + displayStringWaitMatcher.group(1) + ") " +
                            displayStringWaitMatcher.group(displayStringHeadMatcher.groupCount()), TranslationString.Type.SCRIPT_HEAD,
                    filename);
            // Store the DISPLAYSTRINGWAIT string with its IDs
            idsToDialogs.put(id, dialogString);
            ids.add(id);
        }

        Collections.sort(ids);

        // If there were no new strings in the file, the filename is removed from the list
        if (!ids.isEmpty()) {
            fileNamesToIds.put(filename, ids);
        }
    }

    @Override
    public Set<String> getAllowedExtensions() {
        return Set.of("BAF", "d");
    }

    @Override
    public String getType() {
        return "content";
    }

}
