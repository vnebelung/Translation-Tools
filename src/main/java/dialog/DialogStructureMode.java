/*
 * This file is part of the Translation Tools, modified on 29.08.17 23:43.
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See the COPYING file for more details.
 */

package dialog;

import dialog.parser.*;
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
     * @throws ParserConfigurationException if a HTML DocumentBuilder cannot be created which satisfies the
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
        for (Iterator<Map.Entry<String, List<Integer>>> iterator = fileNamesToIds.entrySet().iterator();
             iterator.hasNext(); ) {
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
        DirectoryStream<Path> files = Files.newDirectoryStream(folder, "*." + parser.getAllowedExtension());
        int totalNumFiles = countFiles(folder, parser.getAllowedExtension());
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
     * @param folder        the input folder
     * @param fileExtension the file extension
     * @return the number of files with the given file extension
     * @throws IOException if an I/O error occurs
     */
    private int countFiles(Path folder, String fileExtension) throws IOException {
        DirectoryStream<Path> files = Files.newDirectoryStream(folder, "*." + fileExtension);
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
            // Read key without the -- chars
            String key = parameters[i].substring(2);
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
            System.out.println();
            System.out.println("Usage: java -jar TranslationTools.jar dialog --folder <arg> --range <arg>-<arg>");
            System.out.println("--folder <arg>      = path to the folder containing the D files");
            System.out.println("--range <arg>-<arg> = numerical range of string IDs that should be parsed");
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

        // Find all string IDs in d files
        parseFiles(new DialogContentParser(idsToDialogs, internalIdsToIds, fileNamesToIds), folder);
        // Find all string relations in d files
        parseFiles(new DialogStructureParser(idsToDialogs, internalIdsToIds), folder);

        // Find all string IDs in baf files
        parseFiles(new ScriptContentParser(idsToDialogs, fileNamesToIds), folder);
        // Find all string relations in baf files
        parseFiles(new ScriptStructureParser(idsToDialogs, fileNamesToIds), folder);

        // Chop string IDs to user defined ID range
        chopMappingsToRange(rangeMinInclusive, rangeMaxInclusive);
        // Create the HTML dialog file
        createHtml(folder);
        // Create a string group file
        createGroups(folder, rangeMinInclusive, rangeMaxInclusive);

        System.out.printf("Groups written to '%s'%n",
                folder.resolve(GroupCreator.OUTPUT_FILENAME).toAbsolutePath().toString());
        System.out.printf("HTML written to '%s'%n",
                folder.resolve(HtmlCreator.OUTPUT_FILENAME).toAbsolutePath().toString());
    }
}
