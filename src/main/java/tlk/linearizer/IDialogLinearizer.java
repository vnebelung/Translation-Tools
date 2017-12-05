package tlk.linearizer;

import tlk.DialogString;

import java.util.Map;
import java.util.Set;

/**
 * This interface represents classes that are used to linearize a dialog tree structure.
 */
public interface IDialogLinearizer {

    /**
     * Converts a set of string IDs that are somehow connected to each other into a linearized sorted set.
     *
     * @param idsToDialogs the map where the relations between string IDs and dialog texts are stored
     * @return a linearized sorted set of string IDs
     * @throws IllegalArgumentException if idsToDialogs contains mappings that cannot be processed by the dialog
     *                                  linearizer
     */
    Set<Integer> linearize(Map<Integer, DialogString> idsToDialogs) throws IllegalArgumentException;
}
