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
import java.util.*;

/**
 * This class is responsible for creating an TXT file from the parsed dialog structures of TLK files. The TXT file lists
 * group of strings that are either parents or children of each other.
 */
class GroupCreator {

    private SortedMap<Integer, DialogString> idsToDialogs;

    /**
     * Constructs the group creator. The groups are constructed out of the given map.
     *
     * @param idsToDialogs the map where the relations between string IDs and dialog texts are stored
     */
    GroupCreator(SortedMap<Integer, DialogString> idsToDialogs) {
        // Create a copy of idsToDialogs because it will be modified
        this.idsToDialogs = new TreeMap<>(idsToDialogs);
    }

    /**
     * Creates the groups and writes them into an TXT file in the given folder.
     *
     * @param folder the output folder
     * @throws IOException if an I/O error occurs
     */
    void create(Path folder) throws IOException {
        SortedSet<SortedSet<Integer>> groups = new TreeSet<>(Comparator.comparingInt(SortedSet::first));
        // Go on until idsToDialogs is empty
        while (!idsToDialogs.isEmpty()) {
            // Take the first key and construct its group
            int id = idsToDialogs.firstKey();
            groups.add(createGroup(id));

            System.out.println("Group " + id + " created");
        }
        // Write the groups into a file
        writeSetsToFile(groups, folder);
    }

    /**
     * Writes the given string ID groups into a TXT file to the given folder.
     *
     * @param groups the string ID groups
     * @param folder the output folder
     * @throws IOException if an I/O error occurs
     */
    private void writeSetsToFile(Set<SortedSet<Integer>> groups, Path folder) throws IOException {
        // Delete the old file and create a new one
        Files.deleteIfExists(folder.resolve("DialogSets.txt"));
        Path file = Files.createFile(folder.resolve("DialogSets.txt"));

        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(file)) {
            int count = 1;
            // Lopp through every group
            for (SortedSet<Integer> eachGroup : groups) {
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

            System.out.println(
                    "Groups written to '" + folder.resolve("DialogSets.txt").toAbsolutePath().toString() + "'");
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
    private SortedSet<Integer> createGroup(int id) {
        SortedSet<Integer> result = new TreeSet<>();
        // Add this string ID to the returned group
        result.add(id);
        // Remove the added ID from the map that it will not be analyzed again
        DialogString dialogString = idsToDialogs.remove(id);
        // If the id is somehow not existent in the map, return an empty set
        if (dialogString == null) {
            return Collections.emptySortedSet();
        }
        // Analyze all parents of the string ID
        for (int each : dialogString.getParents()) {
            result.addAll(createGroup(each));
        }
        // Analyze all children of the string ID
        for (int each : dialogString.getChildren()) {
            result.addAll(createGroup(each));
        }
        return result;
    }
}
