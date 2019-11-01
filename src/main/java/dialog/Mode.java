/*
 * This file is part of the Translation Tools, modified on 29.08.17 23:43.
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See the COPYING file for more details.
 */

package dialog;

import dialog.parser.*;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * This class parses TLK files and creates an HTML file that contains dialog structures.
 */
public class Mode {

    private SortedMap<Integer, TranslationString> idsToDialogs = new TreeMap<>();
    private SortedMap<String, Integer> internalIdsToIds = new TreeMap<>();
    private SortedMap<String, List<Integer>> fileNamesToIds = new TreeMap<>();

    /**
     * Creates a TXT document that lists groups of strings. A group of strings contains strings that are parents or
     * children of each other.
     *
     * @param minInclusive the minimum string ID, inclusive
     * @param maxInclusive the maximum string ID, inclusive
     * @param folder       the output folder that the TXT document will be written to
     * @throws IOException if an I/O error occurs
     */
    private void createGroups(Path folder, int minInclusive, int maxInclusive) throws IOException {
        GroupCreator groupFinder = new GroupCreator(idsToDialogs, minInclusive, maxInclusive);
        groupFinder.create(folder);
    }

    /**
     * Creates the an HTML document.
     *
     * @param folder the output folder that the HTML will be written to
     * @throws ParserConfigurationException if an HTML DocumentBuilder cannot be created which satisfies the
     *                                      configuration requested.
     * @throws TransformerException         if an unrecoverable error occurs during the course of the HTML
     *                                      transformation.
     */
    private void createHtml(Path folder) throws ParserConfigurationException, TransformerException {
        HtmlCreator htmlCreator = new HtmlCreator(fileNamesToIds, idsToDialogs);
        htmlCreator.create(folder);
    }

    /**
     * Removes all string IDs that are not in the given ID range. If a string is to be removed and has children that it
     * points to or has parents that are pointing to it the references are removed as well.
     *
     * @param minInclusive the minimum string ID, inclusive
     * @param maxInclusive the maximum string ID, inclusive
     */
    private void chopMappingsToRange(int minInclusive, int maxInclusive) {
        for (Iterator<Map.Entry<Integer, TranslationString>> iterator = idsToDialogs.entrySet().iterator();
             iterator.hasNext(); ) {
            Map.Entry<Integer, TranslationString> entry = iterator.next();
            // Check every id if it is not in the given range
            if (entry.getKey() < minInclusive || entry.getKey() > maxInclusive) {
                // Remove all links between itself and its children
                for (int each : entry.getValue().getChildren()) {
                    idsToDialogs.get(each).removeParent(entry.getKey());
                }
                // Remove all links between itself and its parents
                for (int each : entry.getValue().getParents()) {
                    idsToDialogs.get(each).removeChild(entry.getKey());
                }
                // Remove all links between itself and its neighbors
                for (int each : entry.getValue().getNeighbors()) {
                    idsToDialogs.get(each).removeNeighbor(entry.getKey());
                }
                // Remove itself
                iterator.remove();
            }
        }
        for (Iterator<Map.Entry<String, List<Integer>>> iterator = fileNamesToIds.entrySet().iterator(); iterator.hasNext(); ) {
            // Remove all entries from fileNamesToIds with IDs that are removed from idsToDialogs
            Map.Entry<String, List<Integer>> entry = iterator.next();
            entry.getValue().removeIf(i -> !idsToDialogs.containsKey(i));
            if (entry.getValue().isEmpty()) {
                iterator.remove();
            }
        }

    }

    /**
     * Parses all files in the given folder that the given parser can handle.
     *
     * @param folder the input folder
     * @throws IOException if an I/O error occurs
     */
    private void parseFiles(IParser parser, Path folder) throws IOException {
        DirectoryStream<Path> files = Files.newDirectoryStream(folder, path -> Files.isRegularFile(path) &&
                parser.getAllowedExtensions()
                        .contains(path.getFileName().toString().substring(path.getFileName().toString().lastIndexOf('.') + 1)));
        int totalNumFiles = countFiles(folder, parser.getAllowedExtensions());
        int i = 1;
        for (Path file : files) {
            System.out.printf("Parse %s %d/%d: %s%n", parser.getType(), i, totalNumFiles, file.getFileName());
            parser.parse(file);
            i++;
        }
    }

    /**
     * Creates a default entry in the id/dialog map used for erroneous structures during parsing
     */
    private void prepareMappings() {
        idsToDialogs.put(-1, TranslationString.create("INVALID REFERENCE", TranslationString.Type.ERROR, ""));
    }

    /**
     * Counts the files with the given file extension in the given folder
     *
     * @param folder         the input folder
     * @param fileExtensions the file extensions
     * @return the number of files with the given file extensions
     * @throws IOException if an I/O error occurs
     */
    private int countFiles(Path folder, Set<String> fileExtensions) throws IOException {
        DirectoryStream<Path> files = Files.newDirectoryStream(folder, path -> Files.isRegularFile(path) && fileExtensions
                .contains(path.getFileName().toString().substring(path.getFileName().toString().lastIndexOf('.') + 1)));
        int result = 0;
        for (Path ignored : files) {
            result++;
        }
        return result;
    }

    /**
     * Generates an HTML file that contains the dialog structure and a TXT file with grouped dialog string IDs of BAF
     * and D files of the game in focus.
     *
     * @param from      the minimum string ID (inclusive)
     * @param to        the maximum string ID (inclusive)
     * @param outTxt    the file path to the output TXT file containing the dialog groups
     * @param bafFolder the folder path containing the BAF files
     * @param dFolder   the folder path containing the D files
     * @param outHtml   the file path to the output HTML file containing the dialog structure
     * @throws IOException                  if an IO exception has occurred
     * @throws ParserConfigurationException if an HTML DocumentBuilder cannot be created which satisfies the *
     *                                      configuration requested.
     * @throws TransformerException         if an unrecoverable error occurs during the course of the HTML *
     *                                      transformation.
     */
    public void invoke(int from, int to, String bafFolder, String dFolder, String outHtml, String outTxt) throws IOException,
            TransformerException, ParserConfigurationException {

        // Prepare the ID mappings
        prepareMappings();

        // Find all string IDs in d files
        parseFiles(new DialogContentParser(idsToDialogs, internalIdsToIds, fileNamesToIds), Paths.get(dFolder));
        // Find all string relations in d files
        parseFiles(new DialogStructureParser(idsToDialogs, internalIdsToIds), Paths.get(dFolder));

        // Find all string IDs in baf files
        parseFiles(new ScriptContentParser(idsToDialogs, fileNamesToIds), Paths.get(bafFolder));
        // Find all string relations in baf files
        parseFiles(new ScriptStructureParser(idsToDialogs, fileNamesToIds), Paths.get(bafFolder));
        // Find all string IDs in d files
        parseFiles(new ScriptContentParser(idsToDialogs, fileNamesToIds), Paths.get(dFolder));
        // Find all string relations in d files
        parseFiles(new ScriptStructureParser(idsToDialogs, fileNamesToIds), Paths.get(dFolder));

        // Chop string IDs to user defined ID range
        chopMappingsToRange(from, to);
        // Create the HTML dialog file
        createHtml(Paths.get(outHtml));
        // Create a string group file
        createGroups(Paths.get(outTxt), from, to);

        System.out.printf("Groups written to '%s'%n", Paths.get(outTxt).toAbsolutePath().toString());
        System.out.printf("HTML written to '%s'%n", Paths.get(outHtml).toAbsolutePath().toString());
    }
}
