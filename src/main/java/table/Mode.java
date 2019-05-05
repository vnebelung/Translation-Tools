package table;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * This class extracts string IDs from 2DA files.
 */
public class Mode {

    /**
     * Generates a TXT file with all table strings of 2DA files of the game in focus.
     *
     * @param twodaFolder the folder path containing the CRE files
     * @param outTxt      the file path to the output TXT file containing the item string IDs
     * @param from        the minimum string ID (inclusive) * @param to the maximum string ID (inclusive)
     * @throws IOException if an I/O error has occurred
     */
    public void invoke(String twodaFolder, String outTxt, int from, int to) throws IOException {

        Set<Table> tables = parseTables(Paths.get(twodaFolder));
        chopTablesToRange(tables, from, to);
        // Write the creature string IDs into a file
        writeTableStringIdsTxt(tables, Paths.get(outTxt), from, to);

        System.out.printf("Table strings written to '%s'%n", Paths.get(outTxt).toAbsolutePath().toString());
    }

    /**
     * Writes the string IDs of the given tables into a TXT file to the given folder.
     *
     * @param tables       the parsed tables
     * @param file         the output file
     * @param minInclusive the minimum string ID, inclusive
     * @param maxInclusive the maximum string ID, inclusive
     * @throws IOException if an I/O error occurs
     */
    private void writeTableStringIdsTxt(Set<Table> tables, Path file, int minInclusive, int maxInclusive) throws
            IOException {
        // Delete the old file and create a new one
        Files.deleteIfExists(file);
        Files.createFile(file);

        try (BufferedWriter bufferedWriter = new BufferedWriter(Files.newBufferedWriter(file))) {
            for (Table table : tables) {
                bufferedWriter.write("// " + table.getFileName());
                bufferedWriter.newLine();
                bufferedWriter.newLine();
                // Write string IDs only if they are in the given range
                for (int id : table.getStringIds()) {
                    if (id >= minInclusive && id <= maxInclusive) {
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
     * @param tables       the tables
     * @param minInclusive the minimum string ID, inclusive
     * @param maxInclusive the maximum string ID, inclusive
     */
    private void chopTablesToRange(Set<Table> tables, int minInclusive, int maxInclusive) {
        tables.removeIf(table -> !table.isInRange(minInclusive, maxInclusive));
    }

    /**
     * Parses the table structure of all 2DA files in the given folder. The 2DA files are searched for all string IDs.
     *
     * @param folder the input folder
     * @throws IOException if an I/O error occurs
     */
    private Set<Table> parseTables(Path folder) throws IOException {
        Set<Table> result = new TreeSet<>();
        DirectoryStream<Path> files = Files.newDirectoryStream(folder, "*.2DA");
        List<Path> sortedFiles = new LinkedList<>();
        files.forEach(sortedFiles::add);
        // Sort files alphabetically
        sortedFiles.sort(Comparator.comparing(Path::toString));
        for (Path each : sortedFiles) {
            System.out.printf("Parse table: %s%n", each.getFileName());
            // Parse every cre file
            result.add(parseTable(each));
        }
        return result;
    }

    /**
     * Parses a table structure of a single given 2DA file.
     *
     * @param file the input file
     * @throws IOException if an I/O error occurs
     */
    private Table parseTable(Path file) throws IOException {
        Table result = new Table(file.getFileName().toString());

        // Read every line
        for (String line : Files.readAllLines(file)) {
            //Explode the line on all spaces
            for (String part : line.split(" +")) {
                // Try to extract an int. If part is not an int, ignore it.
                try {
                    result.add(Integer.parseInt(part));
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return result;
    }

}
