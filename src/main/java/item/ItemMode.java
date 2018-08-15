package item;

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
 * This class extracts string IDs from ITM files.
 */
public class ItemMode implements IMode {

    private final static String OUTPUT_FILENAME = "ItemStrings.txt";
    private final static String OUTPUT_CSV_FILENAME = "ItemStrings.csv";

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
            System.out.println("Usage: java -jar TranslationTools.jar items --folder <arg> --range <arg>-<arg>");
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

        Set<Item> items = parseItems(folder);
        chopItemsToRange(items, rangeMinInclusive, rangeMaxInclusive);
        // Write the item string IDs into a file
        writeItemStringIdsToFile(items, folder, rangeMinInclusive, rangeMaxInclusive);
        // Write item strings IDs into a CSV file
        writeItemStringIdsToCsvFile(items, folder, rangeMinInclusive, rangeMaxInclusive);

        System.out
                .printf("Item strings written to '%s'%n", folder.resolve(OUTPUT_FILENAME).toAbsolutePath().toString());
        System.out.printf("CSV written to '%s'%n", folder.resolve(OUTPUT_CSV_FILENAME).toAbsolutePath().toString());
    }

    /**
     * Writes the string IDs of the given items into a CSV file to the given folder.
     *
     * @param items        the parsed items
     * @param folder       the output folder
     * @param minInclusive the minimum string ID, inclusive
     * @param maxInclusive the maximum string ID, inclusive
     * @throws IOException if an I/O error occurs
     */
    private void writeItemStringIdsToCsvFile(Set<Item> items, Path folder, int minInclusive, int maxInclusive) throws
            IOException {
        // Delete the old file and create a new one
        Files.deleteIfExists(folder.resolve(OUTPUT_CSV_FILENAME));
        Path file = Files.createFile(folder.resolve(OUTPUT_CSV_FILENAME));

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
     * @param folder       the output folder
     * @param minInclusive the minimum string ID, inclusive
     * @param maxInclusive the maximum string ID, inclusive
     * @throws IOException if an I/O error occurs
     */
    private void writeItemStringIdsToFile(Set<Item> items, Path folder, int minInclusive, int maxInclusive) throws
            IOException {
        // Delete the old file and create a new one
        Files.deleteIfExists(folder.resolve(OUTPUT_FILENAME));
        Path file = Files.createFile(folder.resolve(OUTPUT_FILENAME));

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
