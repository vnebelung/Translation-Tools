package dialog.parser;

import java.io.IOException;
import java.nio.file.Path;

public interface IParser {

    /**
     * Parses a file and extracts all string IDs and internal string IDs.
     *
     * @param file the input file containing string IDs
     * @throws IOException if an exception of some sort has occurred
     */
    void parse(Path file) throws IOException;

    /**
     * Returns the allowed file extension of the files that should be parsed by this parser.
     *
     * @return the allowed file extension
     */
    String getAllowedExtension();

    /**
     * Returns the parser type, that is either 'content' if the parser scans the files for plain string IDs or
     * 'structure' if the parser tries to retrieve the string ID structure.
     *
     * @return the parser's type
     */
    String getType();
}
