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

package progress;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class creates or updates a PNG and a TXT file depending on the translation status of string IDs.
 */
public class Mode {

    private static final int COLUMN_WIDTH = 10;

    /**
     * Invokes the user chosen functionality.
     *
     * @param completeCsv  path to the CSV file that contains all strings of a translation project
     * @param outOfDateCsv path to the CSV file that contains all out-of-date strings of a translation project
     * @param outPng       path to the PNG file that will contain the visualized progress information
     * @param outTxt       path to the TXT file that will contain the textual progress information
     * @param unusedTxt    path to the TXT file that contains all unused strings of a translation project
     * @param ignoreUnused true if unused strings will be ignored for the progress
     * @param suggestions  the number of suggestions displayed in the translation tool
     * @throws IOException if an I/O exception of some sort has occurred
     */
    public void invoke(String completeCsv, String outOfDateCsv, String unusedTxt, String outPng, String outTxt,
                       int suggestions, boolean ignoreUnused) throws IOException {

        // Parse the CSV file and write all IDs into the map
        SortedMap<Integer, Set<IdState>> idsToState = new TreeMap<>();
        for (Integer id : getIdsFromCsv(Paths.get(completeCsv))) {
            idsToState.put(id, EnumSet.of(IdState.ACCEPTED));
        }

        // Parse the CSV file and write all out-of-date IDs into the map
        for (Integer id : getIdsFromCsv(Paths.get(outOfDateCsv))) {
            idsToState.get(id).remove(IdState.ACCEPTED);
            idsToState.get(id).add(IdState.OUT_OF_DATE);
        }

        // Parse the TXT file and write all unused IDs into the map
        for (Integer id : getIdsFromTxt(Paths.get(unusedTxt))) {
            // If the string ID of the NearInfinity export is not part of the translation tool export, continue
            if (!idsToState.containsKey(id)) {
                continue;
            }
            idsToState.get(id).add(IdState.UNUSED);
        }

        paint(Paths.get(outPng), idsToState);

        write(Paths.get(outTxt), idsToState, suggestions, ignoreUnused);
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
    private void paint(Path png, SortedMap<Integer, Set<IdState>> entries) throws IOException {
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
        for (Map.Entry<Integer, Set<IdState>> each : entries.entrySet()) {
            // Will be set to the translated flag if count marks the current string as #1 of the 10-block,
            // will be set to its current value AND the translation flag if count marks the current string as #2-#9
            // of the 10-block
            translated = count % 10 == 0 ? each.getValue().contains(IdState.ACCEPTED) :
                    translated && each.getValue().contains(IdState.ACCEPTED);
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
     * Writes a small ASCII table that shows the translation progress. If the file at the given path does not exist yet,
     * it will be created. If the file does exist already, a new row with will be attached at the bottom of the table.
     *
     * @param txt          the txt file path
     * @param entries      the string IDs with their translation flags
     * @param ignoreUnused true if unused strings will be ignored for the progress
     * @param suggestions  the number of suggestions displayed in the translation tool
     * @throws IOException if an error occurs during reading a file
     */
    private void write(Path txt, SortedMap<Integer, Set<IdState>> entries, int suggestions, boolean ignoreUnused) throws
            IOException {
        LinkedList<String> lines = new LinkedList<>();
        if (!Files.exists(txt)) {
            // Write header if file does not exist yet
            lines.add("Date         # Untouched   # Suggested   # Accepted   Progress");
            lines.add("-----------+-------------+-------------+------------+---------");
        } else {
            // Read in all existing lines
            lines.addAll(Files.readAllLines(txt));
        }

        // Delete the last line until it is not empty
        while (lines.getLast().isEmpty()) {
            lines.removeLast();
        }

        // Write the new line
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
        int unused = (int) entries.values().stream().filter(i -> i.contains(IdState.UNUSED)).count();
        int total = entries.size();
        int accepted = (int) entries.values().stream().filter(i -> i.contains(IdState.ACCEPTED)).count();
        int untouched = total - accepted - suggestions;
        int percentage = (int) (100.0 * (suggestions + 2 * accepted) / (2 * (total - (ignoreUnused ? unused : 0))));
        lines.add(dateTimeFormatter.format(LocalDate.now()) + "   " + String.format("%1$11s", untouched) + "   " +
                String.format("%1$11s", suggestions) + "   " + String.format("%1$10s", accepted) + "   " +
                String.format("%1$7s", percentage + "%"));

        // Write the new image
        try (FileWriter fileWriter = new FileWriter(txt.toFile())) {
            for (String line : lines) {
                fileWriter.write(line);
                fileWriter.write(System.lineSeparator());
            }
        }
        System.out.println("The TXT file has been created in \"" + txt.toAbsolutePath().toString() + "\"");
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

    /**
     * Reads a given TXT file extracted from NearInfinity and returns all contained string IDs.
     *
     * @param txt the TXT file
     * @return all contained string IDs
     * @throws IOException if an I/O error occurs reading from the CSV file or a malformed or unmappable byte sequence
     *                     is read
     */
    private SortedSet<Integer> getIdsFromTxt(Path txt) throws IOException {
        Pattern pattern = Pattern.compile("^StringRef: (\\d+) ");
        SortedSet<Integer> result = new TreeSet<>();
        List<String> lines = Files.readAllLines(txt);
        for (String each : lines) {
            Matcher matcher = pattern.matcher(each);
            if (!matcher.find()) {
                continue;
            }
            result.add(Integer.valueOf(matcher.group(1)));
        }
        return result;
    }

    private enum IdState {ACCEPTED, UNUSED, OUT_OF_DATE}

}
