/*
 * This file is part of the Translation Tools, modified on 29.08.17 23:43.
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See the COPYING file for more details.
 */

package tlk;

import main.IMode;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class parses TLK files and creates an HTML file that contains dialog structures.
 */
public class DialogStructureMode implements IMode {

    private SortedMap<Integer, DialogString> idsToDialogs = new TreeMap<>();
    private SortedMap<String, Integer> internalIdsToIds = new TreeMap<>();
    private SortedMap<String, List<Integer>> filenamesToIds = new TreeMap<>();

    /**
     * Creates a TXT document that lists groups of strings. A group of strings contains strings that are parents or
     * children of each other.
     * @param folder the output folder that the TXT document will be written to
     * @throws IOException if an I/O error occurs
     */
    private void createGroups(Path folder) throws IOException {
        GroupCreator groupFinder = new GroupCreator(idsToDialogs);
        groupFinder.create(folder);
    }

    /**
     * Creates the an HTML document.
     *
     * @param folder the output folder that the HTML will be written to
     * @throws ParserConfigurationException if a HTML DocumentBuilder cannot be created which satisfies the
     *                                      configuration requested.
     * @throws TransformerException         if an unrecoverable error occurs during the course of the HTML
     *                                      transformation.
     */
    private void createHtml(Path folder) throws ParserConfigurationException, TransformerException {
        HtmlCreator htmlCreator = new HtmlCreator(filenamesToIds, idsToDialogs);
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
        for (Iterator<Map.Entry<Integer, DialogString>> iterator = idsToDialogs.entrySet().iterator();
             iterator.hasNext(); ) {
            Map.Entry<Integer, DialogString> entry = iterator.next();
            // Check every id if it is not in the given range
            if (entry.getKey() < minInclusive || entry.getKey() > maxInclusive) {
                // Remove all links between itself and its children
                for (Integer each : entry.getValue().getChildren()) {
                    idsToDialogs.get(each).removeParent(entry.getKey());
                }
                // Remove all links between itself and its parents
                for (Integer each : entry.getValue().getParents()) {
                    idsToDialogs.get(each).removeChild(entry.getKey());
                }
                // Remove itself
                iterator.remove();
            }
        }
        for (Iterator<Map.Entry<String, List<Integer>>> iterator = filenamesToIds.entrySet().iterator();
             iterator.hasNext(); ) {
            // Remove all entries from filenamesToIds with IDs that are removed from idsToDialogs
            Map.Entry<String, List<Integer>> entry = iterator.next();
            entry.getValue().removeIf(i -> !idsToDialogs.containsKey(i));
            if (entry.getValue().isEmpty()) {
                iterator.remove();
            }
        }

    }

    /**
     * Parses the dialog structure of all d files in the given folder. The d files are searched for their relation to
     * each other, so that dialog trees can be extracted.
     *
     * @param folder the input folder
     * @param count  the total number of d files
     * @throws IOException if an I/O error occurs
     */
    private void parseStructure(Path folder, int count) throws IOException {
        StructureParser structureParser = new StructureParser(idsToDialogs, internalIdsToIds);
        DirectoryStream<Path> files = Files.newDirectoryStream(folder, "*.d");
        int i = 1;
        for (Path each : files) {
            System.out.println("Round 2/2, File " + i + "/" + count + " " + each.getFileName());
            structureParser.parse(each);
            i++;
        }
    }

    /**
     * Parses the content of all d files in the given folder. The d files are searched for string IDs, internal TLK IDs
     * and the corresponding string itself.
     *
     * @param folder the input folder
     * @param count  the total number of d files
     * @throws IOException if an I/O error occurs
     */
    private void parseContent(Path folder, int count) throws IOException {
        ContentParser contentParser = new ContentParser(idsToDialogs, internalIdsToIds, filenamesToIds);
        DirectoryStream<Path> files = Files.newDirectoryStream(folder, "*.d");
        int i = 1;
        for (Path each : files) {
            System.out.println("Round 1/2, File " + i + "/" + count + " " + each.getFileName());
            contentParser.parse(each);
            i++;
        }
    }

    /**
     * Creates a default entry in the id/dialog map used for erroneous structures during parsing
     */
    private void prepareMappings() {
        idsToDialogs.put(-1, DialogString.create("INVALID REFERENCE", DialogString.Type.ERROR, ""));
    }

    /**
     * Counts the .d files in the given folder
     *
     * @param folder the input folder
     * @return the number of .d files
     * @throws IOException if an I/O error occurs
     */
    private int countDFiles(Path folder) throws IOException {
        DirectoryStream<Path> files = Files.newDirectoryStream(folder, "*.d");
        int result = 0;
        for (Path ignored : files) {
            result++;
        }
        return result;
    }

    /**
     * Check whether all needed command line parameters are present and valid.
     *
     * @param parameters the parsed command line parameters
     * @return the parsed parameters or null in case of an error
     */
    private Map<String, String> checkParameters(String... parameters) {

        // Read all given parameters as key => value pairs
        Map<String, String> result = new HashMap<>((parameters.length + 1) / 2);
        for (int i = 0; i < parameters.length; i += 2) {
            // Read key without the - char
            String key = parameters[i].substring(1);
            // If value is not existent (an odd number of parameters), take an empty string
            String value = i + 1 < parameters.length ? parameters[i + 1] : "";
            result.put(key, value);
        }

        // Check whether -folder is present
        if (!result.containsKey("folder")) {
            return null;
        }
        // Check whether -folder <arg> is not empty
        if (result.get("folder").isEmpty()) {
            return null;
        }
        // Check whether folder argument can be parsed
        try {
            Path path = Paths.get(result.get("folder"));
            if (!Files.exists(path)) {
                return null;
            }
        } catch (InvalidPathException ignored) {
            return null;
        }

        // Check whether -range is present
        if (!result.containsKey("range")) {
            return null;
        }
        // Check whether -range <arg> is not empty
        if (result.get("range").isEmpty()) {
            return null;
        }
        // Check whether range argument can be parsed
        String[] numbers = result.get("range").split("-");
        if (numbers.length != 2) {
            return null;
        }
        try {
            int from = Integer.valueOf(numbers[0]);
            int to = Integer.valueOf(numbers[1]);
            if (from > to) {
                return null;
            }
        } catch (NumberFormatException ignored) {
            return null;
        }

        return result;
    }

    /**
     * Invokes the user chosen functionality.
     *
     * @param parameters command line parameters needed for invoking the mode
     * @throws Exception if an exception has occurred.
     */
    @Override
    public void invoke(String... parameters) throws Exception {

        // Check if all needed parameters are present
        Map<String, String> parametersToValues = checkParameters(parameters);
        if (parametersToValues == null) {
            System.out.println("usage: java -jar TranslationTools.jar dialog -folder <arg> -range <arg>-<arg>");
            System.out.println("-folder <arg>      = path to the folder containing the D files");
            System.out.println("-range <arg>-<arg> = numerical range of string IDs that should be parsed");
            return;
        }

        // Parse the input folder and range
        Path folder = Paths.get(parametersToValues.get("folder"));
        Pattern range = Pattern.compile("^(\\d+)-(\\d+)$");
        Matcher rangeMatcher = range.matcher(parametersToValues.get("range"));
        //noinspection ResultOfMethodCallIgnored
        rangeMatcher.matches();
        int rangeMinInclusive = Integer.valueOf(rangeMatcher.group(1));
        int rangeMaxInclusive = Integer.valueOf(rangeMatcher.group(2));

        // Prepare the ID mappings
        prepareMappings();
        // Count the d files
        int count = countDFiles(folder);
        // Find all string IDs
        parseContent(folder, count);
        // Find all string relations
        parseStructure(folder, count);
        // Chop string IDs to user defined ID range
        chopMappingsToRange(rangeMinInclusive, rangeMaxInclusive);
        // Create the HTML dialog file
        createHtml(folder);
        // Create a string group file
        createGroups(folder);
    }
}
