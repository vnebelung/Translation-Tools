package tlk.linearizer;

import tlk.DialogString;

import java.util.*;

/**
 * This class is a linearizer that can be used for dialog structures that can contain cycles. The resulting
 * linearization is somewhat less than perfect.
 */
public class CycleDialogLinearizer implements IDialogLinearizer {

    private HashMap<Integer, DialogString> idsToDialogs;

    @Override
    public Set<Integer> linearize(Map<Integer, DialogString> idsToDialogs) {
        this.idsToDialogs = new HashMap<>(idsToDialogs);
        return createOrder(this.idsToDialogs.keySet().iterator().next());
    }

    /**
     * Creates an ordered group of string IDs, that means a set of string IDs, for a given string ID. For the given ID
     * all its parents and children are added to this group. The parents and children are then searched for their
     * parents and children as well. By this recursive approach a closed string group is created where all string are
     * somehow connected with each other.
     *
     * @param id the string ID that is added to the group, including its children and parents
     * @return the group of strings containing the given ID and all the children and parents IDs.
     */
    private Set<Integer> createOrder(int id) {
        Set<Integer> result = new LinkedHashSet<>();
        // Remove the added ID from the map that it will not be analyzed again
        DialogString dialogString = idsToDialogs.remove(id);
        // If the id is somehow not existent in the map, return an empty set
        if (dialogString == null) {
            return Collections.emptySet();
        }
        // Analyze all parents of the string ID
        for (int each : dialogString.getParents()) {
            result.addAll(createOrder(each));
        }
        // Add this string ID to the returned group
        result.add(id);
        // Add all children of this ID to the returned group. This leads to a more breadth-first search like approach
        result.addAll(dialogString.getChildren());
        // Analyze all children of the string ID
        for (int each : dialogString.getChildren()) {
            result.addAll(createOrder(each));
        }
        return result;
    }
}
