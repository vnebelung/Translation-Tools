package dialog.linearizer;

import dialog.TranslationString;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ScriptLinearizer {

    /**
     * Returns a new instance of the class ScriptLinearizer.
     *
     * @return an instance of ScriptLinearizer
     */
    public static ScriptLinearizer getInstance() {
        return new ScriptLinearizer();
    }

    /**
     * Converts a set of string IDs that are somehow connected to each other into a linearized list.
     *
     * @param idsToDialogs the map where the relations between string IDs and dialog texts are stored
     * @return a linearized list of string IDs
     * @throws IllegalArgumentException if idsToDialogs contains mappings that cannot be processed by the dialog
     *                                  linearizer
     */
    public List<Integer> linearize(Map<Integer, TranslationString> idsToDialogs) throws IllegalArgumentException {
        return new LinkedList<>(idsToDialogs.keySet());
    }
}
