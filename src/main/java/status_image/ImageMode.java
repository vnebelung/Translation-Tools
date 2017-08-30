/*
 * This file is part of the Translation Tools, modified on 26.08.17 15:29.
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See the COPYING file for more details.
 */

/*
 * This file is part of the Translation Tools, modified on 26.08.17 15:29.
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See the COPYING file for more details.
 */

package status_image;

import main.IMode;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

/**
 * This class creates or updates a PNG file depending on the translation status of string IDs.
 */
public class ImageMode implements IMode {

    private static final int COLUMN_WIDTH = 10;

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
            System.out.println("usage: java -jar TranslationTools.jar status -ccsv <arg> -ocsv <arg> -out <arg>");
            System.out.println("-ccsv <arg> = the CSV file containing all strings of a project");
            System.out.println("-ocsv <arg> = the CSV file containing all out-of-date strings of a project");
            System.out.println("-out <arg>  = the PNG file which status visualization is updated or newly generated " +
                    "if not already existent");
            return;
        }

        // Parse the two CSV files
        SortedSet<Integer> completeIds = getIdsFromCsv(Paths.get(parametersToValues.get("ccsv")));
        SortedSet<Integer> outOfDateIds = getIdsFromCsv(Paths.get(parametersToValues.get("ocsv")));

        // Sore for each string Id whether it is still contained in the out-of-date list
        SortedMap<Integer, Boolean> translated = new TreeMap<>();
        for (Integer each : completeIds) {
            translated.put(each, !outOfDateIds.contains(each));
        }

        // Print status information
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("------------------------------------------------").append(System.lineSeparator());
        stringBuilder.append("Translated: ").append(completeIds.size() - outOfDateIds.size()).append("/")
                .append(completeIds.size()).append(", ")
                .append((completeIds.size() - outOfDateIds.size()) * 100 / completeIds.size()).append(" % done")
                .append(System.lineSeparator());
        stringBuilder.append("------------------------------------------------").append(System.lineSeparator());
        System.out.println(stringBuilder.toString());

        paint(Paths.get(parametersToValues.get("out")), translated);
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

        // Check whether -ccsv is present
        if (!result.containsKey("ccsv")) {
            return null;
        }
        // Check whether -ccsv <arg> is not empty
        if (result.get("ccsv").isEmpty()) {
            return null;
        }
        // Check whether ccsv argument can be parsed
        try {
            Path path = Paths.get(result.get("ccsv"));
            if (!Files.exists(path)) {
                return null;
            }
        } catch (InvalidPathException ignored) {
            return null;
        }

        // Check whether -ocsv is present
        if (!result.containsKey("ocsv")) {
            return null;
        }
        // Check whether -ocsv <arg> is not empty
        if (result.get("ocsv").isEmpty()) {
            return null;
        }
        // Check whether ocsv argument can be parsed
        try {
            Path path = Paths.get(result.get("ocsv"));
            if (!Files.exists(path)) {
                return null;
            }
        } catch (InvalidPathException ignored) {
            return null;
        }

        // Check whether -out is present
        if (!result.containsKey("out")) {
            return null;
        }
        // Check whether -ocsv <arg> is not empty
        if (result.get("out").isEmpty()) {
            return null;
        }
        // Check whether out argument can be parsed
        try {
            Paths.get(result.get("out"));
        } catch (InvalidPathException ignored) {
            return null;
        }

        return result;
    }

    /**
     * Paints the PNG that visualizes the translation progress. If the file at the given path does not exist yet, it
     * will be created as a COLUMN_WIDTH px wide image. Every row of the image represents ten string IDs. So row 1
     * visualizes string 1-10, row 2 visualizes string 11-20, and so on. The row will be red if at least one string id
     * is not translated yet. Otherwise the row will be green. If the file does exist already, a new column with
     * COLUMN_WIDTH px width will be attached at the right end of the image.
     *
     * @param png     the png file path
     * @param entries the string IDs with their translation flags
     * @throws IOException if an error occurs during reading a file
     */
    private void paint(Path png, SortedMap<Integer, Boolean> entries) throws IOException {

        BufferedImage newImage;
        if (Files.exists(png)) {
            // If the file exists, copy the old image into a new one and extend it by COLUMN_WIDTH + 1
            BufferedImage oldImage = ImageIO.read(png.toFile());
            newImage = new ExtendedBufferedImage(oldImage, 1 + COLUMN_WIDTH);
            Files.delete(png);
        } else {
            // If the file does not exist, create a new one
            newImage = new BufferedImage(COLUMN_WIDTH, (entries.size() - 1) / 10 + 1, BufferedImage.TYPE_INT_RGB);
        }

        // Paint new status column
        int newXPosition = newImage.getWidth() - COLUMN_WIDTH;
        // Counts the strings in the following for loop
        int count = 0;
        // Marker that indicates whether all particular 10 strings are translated or not
        boolean translated = false;
        for (Map.Entry<Integer, Boolean> each : entries.entrySet()) {
            // Will be set to the translated flag if count marks the current string as #1 of the 10-block,
            // will be set to its current value AND the translation flag if count marks the current string as #2-#9
            // of the 10-block
            translated = count % 10 == 0 ? each.getValue() : translated && each.getValue();
            // Paint the line if it is the tenth string or the last string
            if (count % 10 == 9 || count == entries.size() - 1) {
                for (int i = 0; i < COLUMN_WIDTH; i++) {
                    newImage.setRGB(i + newXPosition, count / 10,
                            translated ? Color.GREEN.getRGB() : Color.RED.getRGB());
                }
            }
            count++;
        }

        // Write the new image
        ImageIO.write(newImage, "png", png.toFile());
        System.out.println("The PNG file has been created in \"" + png.toAbsolutePath().toString() + "\"");
    }

    /**
     * Reads a given CSV file downloaded from the translation tool and returns all contained string IDs that are present
     * in the first column of the CSV.
     *
     * @param csv the CSV file
     * @return all contained string IDs
     * @throws IOException if an I/O error occurs reading from the CSV file or a malformed or unmappable byte sequence
     *                     is read
     */
    private SortedSet<Integer> getIdsFromCsv(Path csv) throws IOException {
        SortedSet<Integer> result = new TreeSet<>();
        List<String> lines = Files.readAllLines(csv);
        for (String each : lines) {
            try {
                each = each.substring(1);
                String[] elements = each.split("\",\"", 2);
                result.add(Integer.valueOf(elements[0]));
            } catch (NumberFormatException | StringIndexOutOfBoundsException ignored) {
                // Ignore all weird lines
            }
        }
        return result;
    }

}
