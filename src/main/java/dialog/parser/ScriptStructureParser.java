/*
 * This file is part of the Translation Tools, modified on 28.08.17 00:10.
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See the COPYING file for more details.
 */

package dialog.parser;

import dialog.TranslationString;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

/**
 * This class is responsible for parsing a script file. For every string its neighbors are searched and linked to
 * each other so that a flat ID structure can be build.
 */
public class ScriptStructureParser {

    private final SortedMap<Integer, TranslationString> idsToDialogs;
    private final SortedMap<String, List<Integer>> fileNamesToIds;

    /**
     * Constructs a new structure parser that parses a script file. The extracted structures are stored in the dialog
     * strings of the given maps.
     *
     * @param idsToDialogs   the map where the relations between string IDs and dialog texts are stored
     * @param fileNamesToIds the map where the relations between file names and string IDs are stored
     */
    public ScriptStructureParser(SortedMap<Integer, TranslationString> idsToDialogs,
                                 SortedMap<String, List<Integer>> fileNamesToIds) {
        this.idsToDialogs = idsToDialogs;
        this.fileNamesToIds = fileNamesToIds;
    }

    /**
     * Parses a dialog file and extracts the structure of the contained dialogs.
     *
     * @param file the input file
     */
    public void parse(Path file) {
        // If the file contains no script IDs it is not in the list and we must return
        if (fileNamesToIds.get(file.getFileName().toString()) == null) {
            return;
        }
        // The set of string IDs is filled with all IDs in the current file
        Set<Integer> ids = new HashSet<>(fileNamesToIds.get(file.getFileName().toString()));
        // Remove every string ID that is not a script ID
        for (int each : ids) {
            if (idsToDialogs.get(each).getType() != TranslationString.Type.SCRIPT_HEAD &&
                    idsToDialogs.get(each).getType() != TranslationString.Type.SCRIPT_JOURNAL) {
                ids.remove(each);
            }
        }
        // For every remaining script ID set the pairwise neighbor status
        for (int eachId : ids) {
            for (int eachNeighborId : ids) {
                if (eachId != eachNeighborId) {
                    idsToDialogs.get(eachId).addNeighbor(eachNeighborId);
                }
            }
        }
    }

}
