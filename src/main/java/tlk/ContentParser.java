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

package tlk;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is responsible for parsing a TLK file and extracting all string IDs and so-called internal string IDs.
 * Latter are used inside a single TLK file to reference particular strings.
 */
class ContentParser {

    private final static Pattern FILENAME = Pattern.compile("// argument : (.*)\\.DLG");
    private final static Pattern BEGIN = Pattern.compile("~ THEN BEGIN (\\d+)");
    private final static Pattern END = Pattern.compile("END[\\r\\n]");
    private final static Pattern SAY = Pattern.compile("SAY #(\\d+) /\\* ~([^~]*)~");
    private final static Pattern REPLY = Pattern.compile("~ THEN REPLY #(\\d+) /\\* ~([^~]*)~");
    private final static Pattern ADDJOURNALENTRY = Pattern.compile("AddJournalEntry\\((\\d+)");
    private final static Pattern JOURNAL = Pattern.compile("JOURNAL #(\\d+) /\\* ~([^~]*)~");
    private final SortedMap<Integer, DialogString> idsToDialogs;
    private final SortedMap<String, Integer> internalIdsToIds;
    private SortedMap<String, List<Integer>> filenamesToIds;

    /**
     * Constructs a new content parser that parses a TLK file. The extracted string IDs are stored in the given maps.
     *
     * @param idsToDialogs     the map where the relations between string IDs and dialog texts are stored
     * @param internalIdsToIds the map where the relations between internal IDs and string IDs are stored
     * @param filenamesToIds   the map where the relations between filenames and string IDs are stored
     */
    ContentParser(SortedMap<Integer, DialogString> idsToDialogs, SortedMap<String, Integer> internalIdsToIds,
                  SortedMap<String, List<Integer>> filenamesToIds) {
        this.idsToDialogs = idsToDialogs;
        this.internalIdsToIds = internalIdsToIds;
        this.filenamesToIds = filenamesToIds;
    }

    /**
     * Parses a TLK file and extracts all string IDs and internal string IDs.
     *
     * @param file the input file containing TLK dialogs
     * @throws IOException if an exception of some sort has occurred
     */
    void parse(Path file) throws IOException {
        // Read the file
        String fileContent = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
        // Read the internal file name and use it as part of the internal ID for every string in this file
        Matcher matcher = FILENAME.matcher(fileContent);
        //noinspection ResultOfMethodCallIgnored
        matcher.find();
        String filename = matcher.group(1);
        // Now search for string IDs
        parseBegin(filename, fileContent);
    }

    /**
     * Searches for the beginning and the end of different dialog blocks. A dialog block is defined by a substring of
     * the given file content that begins with "BEGIN" and ends with the nearest "END". Every block of the given file is
     * then examined more deeply.
     *
     * @param filename    the filename
     * @param fileContent the file's content
     */
    private void parseBegin(String filename, String fileContent) {
        // Initialize a new ID list for the given filename
        filenamesToIds.put(filename, new LinkedList<>());
        Matcher beginMatcher = BEGIN.matcher(fileContent);
        // Find all occurrences of "BEGIN"
        while (beginMatcher.find()) {
            // Construct an internal ID for the string ID of "BEGIN …"
            String internalId = filename + ':' + beginMatcher.group(1);
            // Starting from the position of the current "BEGIN" substring find the first occurrence of "END" which
            // closes the dialog block
            Matcher endMatcher = END.matcher(fileContent);
            //noinspection ResultOfMethodCallIgnored
            endMatcher.find(beginMatcher.start());
            // Parse the content of the current dialog block bounded by "BEGIN" and "END"
            parseBlock(filename, internalId, fileContent.substring(beginMatcher.end(), endMatcher.start()));
        }
    }

    /**
     * Search for string IDs and strings in the given string block by searching for "SAY", "REPLY", "ADDJOURNALENTRY",
     * and "JOURNAL". Each of these entries has an internal string ID that is parsed as well. The internal string ID is
     * extended by the given blockId so that internal string IDs are unique among all TLK files.
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
        int id = Integer.valueOf(sayMatcher.group(1));
        String internalId = blockId;
        // Parse the corresponding string text
        DialogString dialogString = DialogString.create(sayMatcher.group(2), DialogString.Type.DIALOG, filename);
        // Store the SAY string with its IDs
        idsToDialogs.put(id, dialogString);
        internalIdsToIds.put(internalId, id);
        filenamesToIds.get(filename).add(id);

        // Search for all occurrences of "REPLY"
        Matcher replyMatcher = REPLY.matcher(content);
        for (int i = 0; replyMatcher.find(); i++) {
            // Parse the corresponding string ID and the internal ID
            id = Integer.valueOf(replyMatcher.group(1));
            internalId = blockId + '.' + i;
            // Parse the corresponding string text
            dialogString = DialogString.create(replyMatcher.group(2), DialogString.Type.DIALOG, filename);
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
            id = Integer.valueOf(addJournalEntryMatcher.group(1));
            internalId = blockId + ".Journal." + i;
            // Parse the corresponding string text
            dialogString =
                    DialogString.create("** No text specified in TLK file **", DialogString.Type.JOURNAL, filename);
            i++;
            // Store the ADDJOURNALENTRY string with its IDs
            idsToDialogs.put(id, dialogString);
            internalIdsToIds.put(internalId, id);
        }

        // Search for all occurrences of "JOURNAL"
        Matcher journalMatcher = JOURNAL.matcher(content);
        while (journalMatcher.find()) {
            id = Integer.valueOf(journalMatcher.group(1));
            internalId = blockId + ".Journal." + i;
            // Parse the corresponding string text
            dialogString = DialogString.create(journalMatcher.group(2), DialogString.Type.JOURNAL, filename);
            i++;
            // Store the JOURNAL string with its IDs
            idsToDialogs.put(id, dialogString);
            internalIdsToIds.put(internalId, id);
        }
    }

}
