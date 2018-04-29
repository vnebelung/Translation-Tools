package dialog.linearizer;

import dialog.TranslationString;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class ScriptLinearizer implements IDialogLinearizer {

    /**
     * Converts a set of string IDs that are somehow connected to each other into a linearized sorted set.
     *
     * @param idsToDialogs the map where the relations between string IDs and dialog texts are stored
     * @return a linearized sorted set of string IDs
     * @throws IllegalArgumentException if idsToDialogs contains mappings that cannot be processed by the dialog
     *                                  linearizer
     */
    @Override
    public Set<Integer> linearize(Map<Integer, TranslationString> idsToDialogs) throws IllegalArgumentException {
        return new TreeSet<>(idsToDialogs.keySet());
    }
}
