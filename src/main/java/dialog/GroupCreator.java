/*
 * This file is part of the Translation Tools, modified on 29.08.17 22:44.
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See the COPYING file for more details.
 */

package dialog;

import dialog.linearizer.CycleDialogLinearizer;
import dialog.linearizer.IDialogLinearizer;
import dialog.linearizer.NonCycleDialogLinearizer;
import dialog.linearizer.ScriptLinearizer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * This class is responsible for creating an TXT file from the parsed dialog structures of TLK files. The TXT file lists
 * group of strings that are either parents or children of each other.
 */
class GroupCreator {

    final static String OUTPUT_FILENAME = "DialogGroups.txt";
    private final int minInclusive;
    private final int maxInclusive;
    private Map<String, IDialogLinearizer> linearizers = new HashMap<>();
    private SortedMap<Integer, TranslationString> idsToDialogs;

    /**
     * Constructs the group creator. The groups are constructed out of the given map.
     *
     * @param idsToDialogs the map where the relations between string IDs and dialog texts are stored
     * @param minInclusive the minimum string ID, inclusive
     * @param maxInclusive the maximum string ID, inclusive
     */
    GroupCreator(SortedMap<Integer, TranslationString> idsToDialogs, int minInclusive, int maxInclusive) {
        // Create a copy of idsToDialogs because it will be modified
        this.idsToDialogs = idsToDialogs;
        this.minInclusive = minInclusive;
        this.maxInclusive = maxInclusive;

        linearizers.put("cycle", new CycleDialogLinearizer());
        linearizers.put("noncycle", new NonCycleDialogLinearizer());
        linearizers.put("script", new ScriptLinearizer());
    }

    /**
     * Creates the groups and writes them into an TXT file in the given folder.
     *
     * @param folder the output folder
     * @throws IOException if an I/O error occurs
     */
    void create(Path folder) throws IOException {
        Set<Set<Integer>> groups = new TreeSet<>(Comparator.comparingInt(Collections::min));
        // Create a group that contains all string IDs that are in the parameter-defined range but not in idsToDialogs
        Set<Integer> nonDialogGroup = createNonDialogGroup();
        // Go on until idsToDialogs is empty
        while (!idsToDialogs.isEmpty()) {
            // Take the first key and construct its group
            int id = idsToDialogs.firstKey();

            Set<Integer> linearizedIds;
            Map<Integer, TranslationString> group;
            switch (idsToDialogs.get(id).getType()) {
                case DIALOG:
                case JOURNAL:
                    group = createDialogGroup(id);
                    try {
                        linearizedIds = linearizers.get("noncycle").linearize(group);
                    } catch (IllegalArgumentException ignored) {
                        linearizedIds = linearizers.get("cycle").linearize(group);
                    }
                    break;
                case SCRIPT_HEAD:
                case SCRIPT_JOURNAL:
                    group = createScriptGroup(id);
                    linearizedIds = linearizers.get("script").linearize(group);
                    break;
                default:
                    linearizedIds = Collections.emptySet();
            }
            groups.add(linearizedIds);
        }
        // Write the groups into a file
        writeDialogSetsToFile(groups, folder);
        // Add as the last group the non dialog IDs
        writeNonDialogStringsToFile(nonDialogGroup, folder);
    }

    /**
     * Creates an unsorted script group where all strings are part of the same script file.
     *
     * @param id the string ID to start with
     * @return the string ID and all its neighbors in the same file
     */
    private Map<Integer, TranslationString> createScriptGroup(int id) {
        Map<Integer, TranslationString> result = new HashMap<>();
        TranslationString scriptString = idsToDialogs.remove(id);
        result.put(id, scriptString);
        for (int each : scriptString.getNeighbors()) {
            if (idsToDialogs.containsKey(each)) {
                result.putAll(createScriptGroup(each));
            }
        }
        return result;
    }

    /**
     * Creates an unsorted dialog group where all strings are somehow connected with each other.
     *
     * @param id the string ID to start with
     * @return the string ID and all its parents and children
     */
    private Map<Integer, TranslationString> createDialogGroup(int id) {
        Map<Integer, TranslationString> result = new HashMap<>();
        TranslationString dialogString = idsToDialogs.remove(id);
        result.put(id, dialogString);
        for (int each : dialogString.getParents()) {
            if (idsToDialogs.containsKey(each)) {
                result.putAll(createDialogGroup(each));
            }
        }
        for (int each : dialogString.getChildren()) {
            if (idsToDialogs.containsKey(each)) {
                result.putAll(createDialogGroup(each));
            }
        }
        return result;
    }

    /**
     * Writes the given group of string IDs into a TXT file to the given folder. All string IDs must not be used as part
     * of a dialog.
     *
     * @param group  the string ID group
     * @param folder the output folder
     * @throws IOException if an I/O error occurs
     */
    private void writeNonDialogStringsToFile(Set<Integer> group, Path folder) throws IOException {
        try (BufferedWriter bufferedWriter = new BufferedWriter(
                Files.newBufferedWriter(folder.resolve(OUTPUT_FILENAME), StandardOpenOption.APPEND))) {
            bufferedWriter.newLine();
            bufferedWriter.write("// Group: non dialog IDs, " + group.size() + " strings");
            bufferedWriter.newLine();
            bufferedWriter.newLine();
            // Print every child of the group
            for (int eachId : group) {
                bufferedWriter.write(String.valueOf(eachId));
                bufferedWriter.newLine();
            }
        }
    }

    /**
     * Writes the given groups of string IDs into a TXT file to the given folder. All string IDs must be used as part of
     * a dialog.
     *
     * @param groups the string ID groups
     * @param folder the output folder
     * @throws IOException if an I/O error occurs
     */
    private void writeDialogSetsToFile(Set<Set<Integer>> groups, Path folder) throws IOException {
        // Delete the old file and create a new one
        Files.deleteIfExists(folder.resolve(OUTPUT_FILENAME));
        Path file = Files.createFile(folder.resolve(OUTPUT_FILENAME));

        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(file)) {
            int count = 1;
            // Loop through every group
            for (Set<Integer> eachGroup : groups) {
                bufferedWriter.newLine();
                bufferedWriter.write("// Group " + count + ", " + eachGroup.size() + " strings");
                count++;
                bufferedWriter.newLine();
                bufferedWriter.newLine();
                // Print every child of the current group
                for (int eachId : eachGroup) {
                    bufferedWriter.write(String.valueOf(eachId));
                    bufferedWriter.newLine();
                }
            }
        }
    }

    /**
     * Creates a group of string IDs, that means a set of string IDs, which strings are not part of any dialog. For that
     * the string IDs of idsToDialogs are subtracted from the list of all string IDs given by the user-defined ID range.
     * The remaining IDs are not part of any dialog and their strings may be items or are unused.
     *
     * @return the group of strings containing all IDs that are not part of dialogs.
     */
    private Set<Integer> createNonDialogGroup() {
        Set<Integer> result = new TreeSet<>();
        for (int i = minInclusive; i <= maxInclusive; i++) {
            result.add(i);
        }
        result.removeAll(idsToDialogs.keySet());
        return result;
    }
}