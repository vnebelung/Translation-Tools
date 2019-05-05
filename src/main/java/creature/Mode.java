package creature;

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
 * This class extracts string IDs from CRE files.
 */
public class Mode {

    /**
     * Generates a TXT and a CSV file with all creature strings of CRE files of the game in focus.
     *
     * @param creFolder the folder path containing the CRE files
     * @param outTxt    the file path to the output TXT file containing the item string IDs
     * @param outCsv    the file path to the output CSV file containing the item string IDs
     * @param from      the minimum string ID (inclusive) * @param to the maximum string ID (inclusive)
     * @throws IOException if an I/O error has occurred
     */
    public void invoke(String creFolder, String outTxt, String outCsv, int from, int to) throws IOException {

        Set<Creature> creatures = parseCreatures(Paths.get(creFolder));
        chopCreaturesToRange(creatures, from, to);
        // Write the creature string IDs into a file
        writeCreatureStringIdsTxt(creatures, Paths.get(outTxt), from, to);
        // Write creature string IDs into a CSV file
        writeCreatureStringIdsCsv(creatures, Paths.get(outCsv), from, to);

        System.out.printf("Creature strings written to '%s'%n", Paths.get(outTxt).toAbsolutePath().toString());
        System.out.printf("CSV written to '%s'%n", Paths.get(outCsv).toAbsolutePath().toString());
    }

    /**
     * Writes the string IDs of the given creatures into a CSV file to the given folder.
     *
     * @param creatures    the parsed creatures
     * @param file         the output file
     * @param minInclusive the minimum string ID, inclusive
     * @param maxInclusive the maximum string ID, inclusive
     * @throws IOException if an I/O error occurs
     */
    private void writeCreatureStringIdsCsv(Set<Creature> creatures, Path file, int minInclusive,
                                           int maxInclusive) throws IOException {
        // Delete the old file and create a new one
        Files.deleteIfExists(file);
        Files.createFile(file);

        try (BufferedWriter bufferedWriter = new BufferedWriter(Files.newBufferedWriter(file))) {
            bufferedWriter
                    .write("\"The file can include string IDs out of the user-defined string range " + minInclusive +
                            "-" + maxInclusive + "\"");
            bufferedWriter.newLine();
            bufferedWriter.write("\"\"");
            bufferedWriter.newLine();
            bufferedWriter.write("\"CRE File\",\"Short Name\",\"Long Name\",\"Pertaining Strings\"");
            bufferedWriter.newLine();
            for (Creature each : creatures) {
                bufferedWriter.write('"');
                bufferedWriter.write(String.valueOf(each.getFileName()));
                bufferedWriter.write("\",\"");
                bufferedWriter.write(String.valueOf((each.getShortName() == -1 ? "" : each.getShortName())));
                bufferedWriter.write("\",\"");
                bufferedWriter.write(String.valueOf((each.getLongName() == -1 ? "" : each.getLongName())));
                for (int id : each.getPertainingStringsinRange(minInclusive, maxInclusive)) {
                    bufferedWriter.write("\",\"");
                    bufferedWriter.write(String.valueOf(id));
                }
                bufferedWriter.write('"');
                bufferedWriter.newLine();
            }
        }
    }

    /**
     * Writes the string IDs of the given creatures into a TXT file to the given folder.
     *
     * @param creatures    the parsed creatures
     * @param file         the output file
     * @param minInclusive the minimum string ID, inclusive
     * @param maxInclusive the maximum string ID, inclusive
     * @throws IOException if an I/O error occurs
     */
    private void writeCreatureStringIdsTxt(Set<Creature> creatures, Path file, int minInclusive,
                                           int maxInclusive) throws IOException {
        // Delete the old file and create a new one
        Files.deleteIfExists(file);
        Files.createFile(file);

        try (BufferedWriter bufferedWriter = new BufferedWriter(Files.newBufferedWriter(file))) {
            for (Creature creature : creatures) {
                // Write string IDs only if they are in the given range
                if (creature.isShortNameInRange(minInclusive, maxInclusive)) {
                    bufferedWriter.write(String.valueOf(creature.getShortName()));
                    bufferedWriter.newLine();
                }
                if (creature.isLongNameInRange(minInclusive, maxInclusive)) {
                    bufferedWriter.write(String.valueOf(creature.getLongName()));
                    bufferedWriter.newLine();
                }
                if (creature.isPertainingStringsInRange(minInclusive, maxInclusive)) {
                    for (int id : creature.getPertainingStringsinRange(minInclusive, maxInclusive)) {
                        bufferedWriter.write(String.valueOf(id));
                        bufferedWriter.newLine();
                    }
                }
                bufferedWriter.newLine();
            }
        }
    }

    /**
     * Removes all string IDs that are not in the given ID range.
     *
     * @param creatures    the creatures
     * @param minInclusive the minimum string ID, inclusive
     * @param maxInclusive the maximum string ID, inclusive
     */
    private void chopCreaturesToRange(Set<Creature> creatures, int minInclusive, int maxInclusive) {
        creatures.removeIf(creature -> !creature.isInRange(minInclusive, maxInclusive));
    }

    /**
     * Parses the creature structure of all CRE files in the given folder. The CRE files are searched for all string IDs
     * that are used in game in context of the creature.
     *
     * @param folder the input folder
     * @throws IOException if an I/O error occurs
     */
    private Set<Creature> parseCreatures(Path folder) throws IOException {
        Set<Creature> result = new TreeSet<>();
        DirectoryStream<Path> files = Files.newDirectoryStream(folder, "*.CRE");
        List<Path> sortedFiles = new LinkedList<>();
        files.forEach(sortedFiles::add);
        // Sort files alphabetically
        sortedFiles.sort(Comparator.comparing(Path::toString));
        for (Path each : sortedFiles) {
            System.out.printf("Parse creature: %s%n", each.getFileName());
            // Parse every cre file
            result.add(parseCreature(each));
        }
        return result;
    }

    /**
     * Parses an creature structure of a single given CRE file.
     *
     * @param file the input file
     * @throws IOException if an I/O error occurs
     */
    private Creature parseCreature(Path file) throws IOException {
        byte[] fileContent = Files.readAllBytes(file);
        // Allocate a buffer for 102 integers, each 4 bytes long
        ByteBuffer byteBuffer = ByteBuffer.allocate(408);
        // The CRE file is little endian
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

        // Read the four string IDs
        byteBuffer.put(fileContent, 8, 4);
        byteBuffer.put(fileContent, 12, 4);
        for (int i = 0; i < 100; i++) {
            byteBuffer.put(fileContent, 164 + 4 * i, 4);
        }

        // Convert the 102 string IDs to integers
        int shortName = byteBuffer.getInt(0);
        int longName = byteBuffer.getInt(4);
        int[] pertaining = new int[100];
        for (int i = 0; i < 100; i++) {
            pertaining[i] = byteBuffer.getInt(8 + i * 4);
        }

        // Create a new creature with the 102 string IDs
        return new Creature(file.getFileName().toString(), shortName, longName, pertaining);
    }

}
