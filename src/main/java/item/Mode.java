package item;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * This class extracts string IDs from ITM files.
 */
public class Mode {

    /**
     * Generates a CSV and a TXT file with all item strings of ITM files of the game in focus.
     *
     * @param itmFolder the folder path containing the D files
     * @param outTxt    the file path to the output TXT file containing the item string IDs
     * @param outCsv    the file path to the output CSV file containing the item string IDs
     * @param from      the minimum string ID (inclusive)
     * @param to        the maximum string ID (inclusive)
     * @throws IOException if an I/O error has occurred
     */
    public void invoke(String itmFolder, int from, int to, String outTxt, String outCsv) throws IOException {

        Set<Item> items = parseItems(Paths.get(itmFolder));
        chopItemsToRange(items, from, to);
        // Write the item string IDs into a file
        writeItemStringIdsTxt(items, Paths.get(outTxt), from, to);
        // Write item strings IDs into a CSV file
        writeItemStringIdsCsv(items, Paths.get(outCsv), from, to);

        System.out.printf("Item strings written to '%s'%n", Paths.get(outTxt).toAbsolutePath().toString());
        System.out.printf("CSV written to '%s'%n", Paths.get(outCsv).toAbsolutePath().toString());
    }

    /**
     * Writes the string IDs of the given items into a CSV file to the given folder.
     *
     * @param items        the parsed items
     * @param file         the output file
     * @param minInclusive the minimum string ID, inclusive
     * @param maxInclusive the maximum string ID, inclusive
     * @throws IOException if an I/O error occurs
     */
    private void writeItemStringIdsCsv(Set<Item> items, Path file, int minInclusive, int maxInclusive) throws
            IOException {
        // Delete the old file and create a new one
        Files.deleteIfExists(file);
        Files.createFile(file);

        try (BufferedWriter bufferedWriter = new BufferedWriter(Files.newBufferedWriter(file))) {
            bufferedWriter
                    .write("\"The file can include string IDs out of the user-defined string range " + minInclusive +
                            "-" + maxInclusive + "\",\"\",\"\",\"\",\"\"");
            bufferedWriter.newLine();
            bufferedWriter.write("\"\",\"\",\"\",\"\",\"\"");
            bufferedWriter.newLine();
            bufferedWriter.write("\"ITM File\",\"General Name\",\"Identified Name\",\"General Description\"," +
                    "\"Identified " + "Description\"");
            bufferedWriter.newLine();
            for (Item each : items) {
                bufferedWriter.write('"');
                bufferedWriter.write(String.valueOf(each.getFileName()));
                bufferedWriter.write("\",\"");
                bufferedWriter.write(String.valueOf((each.getGeneralName() == -1 ? "" : each.getGeneralName())));
                bufferedWriter.write("\",\"");
                bufferedWriter.write(String.valueOf((each.getIdentifiedName() == -1 ? "" : each.getIdentifiedName())));
                bufferedWriter.write("\",\"");
                bufferedWriter.write(String
                        .valueOf((each.getGeneralDescription() == -1 ? "" : each.getGeneralDescription())));
                bufferedWriter.write("\",\"");
                bufferedWriter.write(String
                        .valueOf((each.getIdentifiedDescription() == -1 ? "" : each.getIdentifiedDescription())));
                bufferedWriter.write('"');
                bufferedWriter.newLine();
            }
        }
    }

    /**
     * Writes the string IDs of the given items into a TXT file to the given folder.
     *
     * @param items        the parsed items
     * @param file         the output file
     * @param minInclusive the minimum string ID, inclusive
     * @param maxInclusive the maximum string ID, inclusive
     * @throws IOException if an I/O error occurs
     */
    private void writeItemStringIdsTxt(Set<Item> items, Path file, int minInclusive, int maxInclusive) throws
            IOException {
        // Delete the old file and create a new one
        Files.deleteIfExists(file);
        Files.createFile(file);

        try (BufferedWriter bufferedWriter = new BufferedWriter(Files.newBufferedWriter(file))) {
            for (Item each : items) {
                // Write string IDs only if they are in the given range
                if (each.isGeneralNameInRange(minInclusive, maxInclusive)) {
                    bufferedWriter.write(String.valueOf(each.getGeneralName()));
                    bufferedWriter.newLine();
                }
                if (each.isIdentifiedNameInRange(minInclusive, maxInclusive)) {
                    bufferedWriter.write(String.valueOf(each.getIdentifiedName()));
                    bufferedWriter.newLine();
                }
                if (each.isGeneralDescriptionInRange(minInclusive, maxInclusive)) {
                    bufferedWriter.write(String.valueOf(each.getGeneralDescription()));
                    bufferedWriter.newLine();
                }
                if (each.isIdentifiedDescriptionInRange(minInclusive, maxInclusive)) {
                    bufferedWriter.write(String.valueOf(each.getIdentifiedDescription()));
                    bufferedWriter.newLine();
                }
                bufferedWriter.newLine();
            }
        }
    }

    /**
     * Removes all string IDs that are not in the given ID range.
     *
     * @param items        the items
     * @param minInclusive the minimum string ID, inclusive
     * @param maxInclusive the maximum string ID, inclusive
     */
    private void chopItemsToRange(Set<Item> items, int minInclusive, int maxInclusive) {
        items.removeIf(item -> !item.isInRange(minInclusive, maxInclusive));
    }

    /**
     * Parses the item structure of all itm files in the given folder. The itm files are searched for all string IDs
     * that are used in game to display the item.
     *
     * @param folder the input folder
     * @throws IOException if an I/O error occurs
     */
    private Set<Item> parseItems(Path folder) throws IOException {
        Set<Item> result = new TreeSet<>();
        DirectoryStream<Path> files = Files.newDirectoryStream(folder, "*.ITM");
        List<Path> sortedFiles = new LinkedList<>();
        files.forEach(sortedFiles::add);
        // Sort files alphabetically
        sortedFiles.sort(Comparator.comparing(Path::toString));
        for (Path each : sortedFiles) {
            System.out.printf("Parse item: %s%n", each.getFileName());
            // Parse every itm file
            result.add(parseItem(each));
        }
        return result;
    }

    /**
     * Parses an item structure of a single given itm file.
     *
     * @param file the input file
     * @throws IOException if an I/O error occurs
     */
    private Item parseItem(Path file) throws IOException {
        byte[] fileContent = Files.readAllBytes(file);
        // Allocate a buffer for four integers, each 4 bytes long
        ByteBuffer byteBuffer = ByteBuffer.allocate(16);
        // The ITM file is little endian
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

        // Read the four string IDs
        byteBuffer.put(fileContent, 8, 4);
        byteBuffer.put(fileContent, 12, 4);
        byteBuffer.put(fileContent, 80, 4);
        byteBuffer.put(fileContent, 84, 4);

        // Convert the four string IDs to integers
        int generalName = byteBuffer.getInt(0);
        int identifiedName = byteBuffer.getInt(4);
        int generalDescription = byteBuffer.getInt(8);
        int identifiedDescription = byteBuffer.getInt(12);

        // Create a new item withe the four string IDs
        return new Item(file.getFileName().toString(), generalName, identifiedName, generalDescription,
                identifiedDescription);
    }

}
