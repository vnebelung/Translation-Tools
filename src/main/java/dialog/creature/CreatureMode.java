package dialog.creature;

import main.IMode;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class extracts string IDs from CRE files.
 */
public class CreatureMode implements IMode {

    private final static String OUTPUT_FILENAME = "CreatureStrings.txt";
    private final static String OUTPUT_CSV_FILENAME = "CreatureStrings.csv";

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
            System.out.println("Usage: java -jar TranslationTools.jar creatures --folder <arg> --range <arg>-<arg>");
            System.out.println("--folder <arg>      = path to the folder containing the ITM files");
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

        Set<Creature> creatures = parseCreatures(folder);
        chopCreaturesToRange(creatures, rangeMinInclusive, rangeMaxInclusive);
        // Write the creature string IDs into a file
        writeCreatureStringIdsToFile(creatures, folder, rangeMinInclusive, rangeMaxInclusive);
        // Write creature string IDs into a CSV file
        writeCreatureStringIdsToCsvFile(creatures, folder, rangeMinInclusive, rangeMaxInclusive);

        System.out.printf("Creature strings written to '%s'%n",
                folder.resolve(OUTPUT_FILENAME).toAbsolutePath().toString());
        System.out.printf("CSV written to '%s'%n", folder.resolve(OUTPUT_CSV_FILENAME).toAbsolutePath().toString());
    }

    /**
     * Writes the string IDs of the given creatures into a CSV file to the given folder.
     *
     * @param creatures    the parsed creatures
     * @param folder       the output folder
     * @param minInclusive the minimum string ID, inclusive
     * @param maxInclusive the maximum string ID, inclusive
     * @throws IOException if an I/O error occurs
     */
    private void writeCreatureStringIdsToCsvFile(Set<Creature> creatures, Path folder, int minInclusive,
                                                 int maxInclusive) throws IOException {
        // Delete the old file and create a new one
        Files.deleteIfExists(folder.resolve(OUTPUT_CSV_FILENAME));
        Path file = Files.createFile(folder.resolve(OUTPUT_CSV_FILENAME));

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
     * @param folder       the output folder
     * @param minInclusive the minimum string ID, inclusive
     * @param maxInclusive the maximum string ID, inclusive
     * @throws IOException if an I/O error occurs
     */
    private void writeCreatureStringIdsToFile(Set<Creature> creatures, Path folder, int minInclusive,
                                              int maxInclusive) throws IOException {
        // Delete the old file and create a new one
        Files.deleteIfExists(folder.resolve(OUTPUT_FILENAME));
        Path file = Files.createFile(folder.resolve(OUTPUT_FILENAME));

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
        creatures.removeIf(c -> !c.isInRange(minInclusive, maxInclusive));
    }

    /**
     * Parses the creature structure of all cre files in the given folder. The cre files are searched for all string IDs
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
     * Parses an creature structure of a single given cre file.
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

}
