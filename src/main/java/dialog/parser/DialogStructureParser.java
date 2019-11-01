/*
 * This file is part of the Translation Tools, modified on 28.08.17 00:10.
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See the COPYING file for more details.
 */

package dialog.parser;

import dialog.TranslationString;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;
import java.util.SortedMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is responsible for parsing a dialog file and extracting the dialog structure of dialog strings. For every
 * string its children and parents are searched and linked to each other so that dialog trees can be build.
 */
public class DialogStructureParser implements IParser {

    private final static Pattern BEGIN = Pattern.compile("~ THEN BEGIN (\\d+)");
    private final static Pattern END = Pattern.compile("END[\\r\\n]");
    private final static Pattern SAY = Pattern.compile("SAY #(\\d+)");
    private final static Pattern REPLY = Pattern.compile("REPLY #(\\d+)");
    private final static Pattern GOTO = Pattern.compile("GOTO (\\d+)");
    private final static Pattern JOURNAL = Pattern.compile("JOURNAL #(\\d+)");
    private final static Pattern ADDJOURNALENTRY = Pattern.compile("AddJournalEntry\\((\\d+)");
    private final static Pattern EXTERN = Pattern.compile("EXTERN ~([^~]*)~ (\\d+)");
    private final SortedMap<Integer, TranslationString> idsToDialogs;
    private final SortedMap<String, Integer> internalIdsToIds;

    /**
     * Constructs a new structure parser that parses a dialog file. The extracted structures are stored in the dialog
     * strings of the given maps.
     *
     * @param idsToDialogs     the map where the relations between string IDs and dialog texts are stored
     * @param internalIdsToIds the map where the relations between internal IDs and string IDs are stored
     */
    public DialogStructureParser(SortedMap<Integer, TranslationString> idsToDialogs,
                                 SortedMap<String, Integer> internalIdsToIds) {
        this.idsToDialogs = idsToDialogs;
        this.internalIdsToIds = internalIdsToIds;
    }

    @Override
    public void parse(Path file) throws IOException {
        // Read the file
        String fileContent = Files.readString(file);
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
        return "structure";
    }

    /**
     * Search for the beginning and the end of different dialog blocks. A dialog block is defined by a substring of the
     * given file content that begins with "BEGIN" and ends with the nearest "END". Every block of the given file is
     * then examined more deeply.
     *
     * @param fileName    the fileName
     * @param fileContent the file's content
     */
    private void parseBegin(String fileName, String fileContent) {
        Matcher beginMatcher = BEGIN.matcher(fileContent);
        // Find all occurrences of "BEGIN"
        while (beginMatcher.find()) {
            // Starting from the position of the current "BEGIN" substring find the first occurrence of "END" which
            // closes the dialog block
            Matcher endMatcher = END.matcher(fileContent);
            //noinspection ResultOfMethodCallIgnored
            endMatcher.find(beginMatcher.start());
            // Parse the content of the current dialog block bounded by "BEGIN" and "END"
            parseBlock(fileName, fileContent.substring(beginMatcher.end(), endMatcher.start()));
        }
    }

    /**
     * Searches for string IDs in the given string block by searching for "SAY". Then each preceding dialog line in this
     * block is parsed as a potential child of the parent SAY string.
     *
     * @param filename the name of the file that contains the dialog block
     * @param content  the content of the dialog block
     */
    private void parseBlock(String filename, String content) {

        // Search for the first occurrence of "SAY"
        Matcher sayMatcher = SAY.matcher(content);
        //noinspection ResultOfMethodCallIgnored
        sayMatcher.find();
        // Parse the corresponding string ID
        int id = Integer.parseInt(sayMatcher.group(1));
        // Split the dialog block into dialog lines
        String[] entries = content.split("[\\r\\n] {2}");
        for (String each : entries) {
            // Parse every dialog line
            parseLine(filename, id, each);
        }
    }

    /**
     * Searches for string IDs in the given dialog line by searching for "REPLY", "GOTO", "ADDJOURNALENTRY", "JOURNAL",
     * and "EXTERN". Each of these entries is a child of the string represented by the given parent id. One dialog line
     * can contain zero or more children.
     *
     * @param filename the name of the file that contains the line
     * @param parentId the parent's string ID
     * @param line     the line that contains potential children
     */
    private void parseLine(String filename, int parentId, String line) {
        // Search for all occurrences of "REPLY"
        Matcher replyMatcher = REPLY.matcher(line);
        if (replyMatcher.find()) {
            // Parse the corresponding string ID
            int id = Integer.parseInt(replyMatcher.group(1));
            // Store the occurrence as a child
            idsToDialogs.get(parentId).addChild(id);
            idsToDialogs.get(id).addParent(parentId);
            // If the line contains a "REPLY", all following matches are treated as children of the REPLY string
            parentId = id;
        }

        // Search for all occurrences of "GOTO"
        Matcher gotoMatcher = GOTO.matcher(line);
        if (gotoMatcher.find()) {
            // Parse the corresponding internal string ID
            String internalId = filename + ':' + Integer.valueOf(gotoMatcher.group(1));
            try {
                // Store the occurrence as a child
                idsToDialogs.get(parentId).addChild(internalIdsToIds.get(internalId));
                idsToDialogs.get(internalIdsToIds.get(internalId)).addParent(parentId);
            } catch (NullPointerException ignored) {
                // Store the occurrence as an erroneous child
                idsToDialogs.get(parentId).addChild(-1);
                idsToDialogs.get(-1).addParent(parentId);
            }
        }

        // Search for all occurrences of "JOURNAL"
        Matcher journalMatcher = JOURNAL.matcher(line);
        if (journalMatcher.find()) {
            // Parse the corresponding string ID
            int id = Integer.parseInt(journalMatcher.group(1));
            // Store the occurrence as a child
            idsToDialogs.get(parentId).addChild(id);
            idsToDialogs.get(id).addParent(parentId);
        }

        // Search for all occurrences of "ADDJOURNALENTRY"
        Matcher addJournalEntryMatcher = ADDJOURNALENTRY.matcher(line);
        if (addJournalEntryMatcher.find()) {
            // Parse the corresponding string ID
            int id = Integer.parseInt(addJournalEntryMatcher.group(1));
            // Store the occurrence as a child
            idsToDialogs.get(parentId).addChild(id);
            idsToDialogs.get(id).addParent(parentId);
        }

        // Search for all occurrences of "EXTERN"
        Matcher externMatcher = EXTERN.matcher(line);
        if (externMatcher.find()) {
            // Parse the corresponding internal string ID
            String internalId = externMatcher.group(1) + ':' + externMatcher.group(2);
            try {
                // Store the occurrence as a child
                idsToDialogs.get(parentId).addChild(internalIdsToIds.get(internalId));
                idsToDialogs.get(internalIdsToIds.get(internalId)).addParent(parentId);
            } catch (NullPointerException ignored) {
                // Store the occurrence as an erroneous child
                idsToDialogs.get(parentId).addChild(-1);
                idsToDialogs.get(-1).addParent(parentId);
            }
        }
    }
}
