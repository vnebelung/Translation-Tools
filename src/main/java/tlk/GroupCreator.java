/*
 * This file is part of the Translation Tools, modified on 29.08.17 22:44.
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See the COPYING file for more details.
 */

package tlk;

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

    private final static String DIALOG_SETS_FILENAME = "DialogSets.txt";
    private final int minInclusive;
    private final int maxInclusive;
    private SortedMap<Integer, DialogString> idsToDialogs;

    /**
     * Constructs the group creator. The groups are constructed out of the given map.
     *
     * @param idsToDialogs the map where the relations between string IDs and dialog texts are stored
     * @param minInclusive the minimum string ID, inclusive
     * @param maxInclusive the maximum string ID, inclusive
     */
    GroupCreator(SortedMap<Integer, DialogString> idsToDialogs, int minInclusive, int maxInclusive) {
        // Create a copy of idsToDialogs because it will be modified
        this.idsToDialogs = new TreeMap<>(idsToDialogs);
        this.minInclusive = minInclusive;
        this.maxInclusive = maxInclusive;
    }

    /**
     * Creates the groups and writes them into an TXT file in the given folder.
     *
     * @param folder the output folder
     * @throws IOException if an I/O error occurs
     */
    void create(Path folder) throws IOException {
        SortedSet<Set<Integer>> groups = new TreeSet<>(Comparator.comparingInt(Collections::min));
        // Create a group that contains all string IDs that are in the parameter-defined range but not in idsToDialogs
        Set<Integer> nonDialogGroup = createNonDialogGroup();
        // Go on until idsToDialogs is empty
        while (!idsToDialogs.isEmpty()) {
            // Take the first key and construct its group
            int id = idsToDialogs.firstKey();
            groups.add(createDialogGroup(id));

            System.out.println("Group " + id + " created");
        }
        // Write the groups into a file
        writeDialogSetsToFile(groups, folder);
        // Add as the last group the non dialog IDs
        writeNonDialogStringsToFile(nonDialogGroup, folder);

        System.out.println(
                "Groups written to '" + folder.resolve(DIALOG_SETS_FILENAME).toAbsolutePath().toString() + "'");
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
                Files.newBufferedWriter(folder.resolve(DIALOG_SETS_FILENAME), StandardOpenOption.APPEND))) {
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
    private void writeDialogSetsToFile(SortedSet<Set<Integer>> groups, Path folder) throws IOException {
        // Delete the old file and create a new one
        Files.deleteIfExists(folder.resolve(DIALOG_SETS_FILENAME));
        Path file = Files.createFile(folder.resolve(DIALOG_SETS_FILENAME));

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
     * Creates a group of string IDs, that means a set of string IDs, for a given string ID. For the given ID all its
     * parents and children are added to this group. The parents and children are then searched for their parents and
     * children as well. By this recursive approach a closed string group is created where all string are somehow
     * connected with each other.
     *
     * @param id the string ID which's parents and children will be
     * @return the group of strings containing the given ID and all the children and parents IDs.
     */
    private Set<Integer> createDialogGroup(int id) {
        Set<Integer> result = new LinkedHashSet<>();
        // Remove the added ID from the map that it will not be analyzed again
        DialogString dialogString = idsToDialogs.remove(id);
        // If the id is somehow not existent in the map, return an empty set
        if (dialogString == null) {
            return Collections.emptySortedSet();
        }
        // Analyze all parents of the string ID
        for (int each : dialogString.getParents()) {
            result.addAll(createDialogGroup(each));
        }
        // Add this string ID to the returned group
        result.add(id);
        // Add all children of this ID to the returned group. This leads to a more breadth-first search like approach
        result.addAll(dialogString.getChildren());
        // Analyze all children of the string ID
        for (int each : dialogString.getChildren()) {
            result.addAll(createDialogGroup(each));
        }
        return result;
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
