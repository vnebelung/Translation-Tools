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
 * This class is responsible for parsing a dialog file and extracting all string IDs and so-called internal string IDs.
 * Latter are used inside a single dialog file to reference particular strings.
 */
public class DialogContentParser implements IParser {

    private final static Pattern BEGIN = Pattern.compile("~ THEN BEGIN (\\d+)");
    private final static Pattern END = Pattern.compile("[\\r\\n]END[\\r\\n]");
    private final static Pattern SAY = Pattern.compile("SAY #(\\d+) /\\* ~([^~]*)~");
    private final static Pattern REPLY = Pattern.compile("~ THEN REPLY #(\\d+) /\\* ~([^~]*)~");
    private final static Pattern ADDJOURNALENTRY = Pattern.compile("AddJournalEntry\\((\\d+)");
    private final static Pattern JOURNAL = Pattern.compile("JOURNAL #(\\d+) /\\* ~([^~]*)~");
    private final SortedMap<Integer, TranslationString> idsToDialogs;
    private final SortedMap<String, Integer> internalIdsToIds;
    private SortedMap<String, List<Integer>> filenamesToIds;

    /**
     * Constructs a new content parser that parses a dialog file. The extracted string IDs are stored in the given maps.
     *
     * @param idsToDialogs     the map where the relations between string IDs and dialog texts are stored
     * @param internalIdsToIds the map where the relations between internal IDs and string IDs are stored
     * @param filenamesToIds   the map where the relations between file names and string IDs are stored
     */
    public DialogContentParser(SortedMap<Integer, TranslationString> idsToDialogs,
                               SortedMap<String, Integer> internalIdsToIds,
                               SortedMap<String, List<Integer>> filenamesToIds) {
        this.idsToDialogs = idsToDialogs;
        this.internalIdsToIds = internalIdsToIds;
        this.filenamesToIds = filenamesToIds;
    }

    @Override
    public void parse(Path file) throws IOException {
        // Read the file
        String fileContent = Files.readString(file);
        // Read the internal file name and use it as part of the internal ID for every string in this file
        // Now search for string IDs
        parseBegin(file.getFileName().toString().substring(0, file.getFileName().toString().lastIndexOf('.')),
                fileContent);
    }

    @Override
    public Set<String> getAllowedExtensions() {
        return Collections.singleton("d");
    }

    @Override
    public String getType() {
        return "content";
    }

    /**
     * Searches for the beginning and the end of different dialog blocks. A dialog block is defined by a substring of
     * the given file content that begins with "BEGIN" and ends with the nearest "END". Every block of the given file is
     * then examined more deeply.
     *
     * @param fileName    the file name
     * @param fileContent the file's content
     */
    private void parseBegin(String fileName, String fileContent) {
        // Initialize a new ID list for the given filename
        filenamesToIds.put(fileName, new LinkedList<>());
        Matcher beginMatcher = BEGIN.matcher(fileContent);
        // Find all occurrences of "BEGIN"
        while (beginMatcher.find()) {
            // Construct an internal ID for the string ID of "BEGIN …"
            String internalId = fileName + ':' + beginMatcher.group(1);
            // Starting from the position of the current "BEGIN" substring find the first occurrence of "END" which
            // closes the dialog block
            Matcher endMatcher = END.matcher(fileContent);
            //noinspection ResultOfMethodCallIgnored
            endMatcher.find(beginMatcher.start());
            // Parse the content of the current dialog block bounded by "BEGIN" and "END"
            parseBlock(fileName, internalId, fileContent.substring(beginMatcher.end(), endMatcher.start()));
        }
    }

    /**
     * Search for string IDs and strings in the given string block by searching for "SAY", "REPLY", "ADDJOURNALENTRY",
     * and "JOURNAL". Each of these entries has an internal string ID that is parsed as well. The internal string ID is
     * extended by the given blockId so that internal string IDs are unique among all dialog files.
     *
     * @param filename the name of the file that contains the dialog block
     * @param blockId  the ID if the dialog block
     * @param content  the content of the dialog block
     */
    private void parseBlock(String filename, String blockId, String content) {

        // Search for the first occurrence of "SAY"
        Matcher sayMatcher = SAY.matcher(content);
        //noinspection ResultOfMethodCallIgnored
        sayMatcher.find();
        // Parse the corresponding string ID and the internal ID
        int id = Integer.parseInt(sayMatcher.group(1));
        String internalId = blockId;
        // Parse the corresponding string text
        TranslationString dialogString =
                TranslationString.create(sayMatcher.group(2), TranslationString.Type.DIALOG, filename);
        // Store the SAY string with its IDs
        idsToDialogs.put(id, dialogString);
        internalIdsToIds.put(internalId, id);
        filenamesToIds.get(filename).add(id);

        // Search for all occurrences of "REPLY"
        Matcher replyMatcher = REPLY.matcher(content);
        for (int i = 0; replyMatcher.find(); i++) {
            // Parse the corresponding string ID and the internal ID
            id = Integer.parseInt(replyMatcher.group(1));
            internalId = blockId + '.' + i;
            // Parse the corresponding string text
            dialogString = TranslationString.create(replyMatcher.group(2), TranslationString.Type.DIALOG, filename);
            // Store the REPLY string with its IDs
            idsToDialogs.put(id, dialogString);
            internalIdsToIds.put(internalId, id);
        }

        // The counter is used for both "ADDJOURNALENTRY" and "JOURNAL" as they are both journal entries
        int i = 0;

        // Search for all occurrences of "ADDJOURNALENTRY"
        Matcher addJournalEntryMatcher = ADDJOURNALENTRY.matcher(content);
        while (addJournalEntryMatcher.find()) {
            // Parse the corresponding string ID and the internal ID
            id = Integer.parseInt(addJournalEntryMatcher.group(1));
            internalId = blockId + ".Journal." + i;
            // Parse the corresponding string text
            dialogString = TranslationString
                    .create("** No text specified in file **", TranslationString.Type.JOURNAL, filename);
            i++;
            // Store the ADDJOURNALENTRY string with its IDs
            idsToDialogs.put(id, dialogString);
            internalIdsToIds.put(internalId, id);
        }

        // Search for all occurrences of "JOURNAL"
        Matcher journalMatcher = JOURNAL.matcher(content);
        while (journalMatcher.find()) {
            id = Integer.parseInt(journalMatcher.group(1));
            internalId = blockId + ".Journal." + i;
            // Parse the corresponding string text
            dialogString = TranslationString.create(journalMatcher.group(2), TranslationString.Type.JOURNAL, filename);
            i++;
            // Store the JOURNAL string with its IDs
            idsToDialogs.put(id, dialogString);
            internalIdsToIds.put(internalId, id);
        }
    }

}
