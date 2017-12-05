package tlk.linearizer;

import tlk.DialogString;

import java.util.*;

public class NonCycleDialogLinearizer implements IDialogLinearizer {

    private Map<Integer, DialogString> idsToDialogs;

    /**
     * Converts a set of string IDs that are somehow connected to each other into a linearized set.
     *
     * @param idsToDialogs the map where the relations between string IDs and dialog texts are stored
     * @return a linearized sorted set of string IDs
     * @throws IllegalArgumentException if idsToDialogs contains mappings that result in cycles in the dialog structure
     */
    @Override
    public Set<Integer> linearize(Map<Integer, DialogString> idsToDialogs) throws IllegalArgumentException {
        this.idsToDialogs = idsToDialogs;
        for (int each : this.idsToDialogs.keySet()) {
            // Check for every string whether itself or one of its children is part of a cyclic dialog structure
            if (checkForCycles(each, Collections.emptySet())) {
                throw new IllegalArgumentException();
            }
        }
        // We don't have any cycles so move on
        Set<Integer> result = new LinkedHashSet<>(idsToDialogs.size());
        Set<Integer> visited = new HashSet<>(idsToDialogs.size());
        // Find all strings that have no parents
        Set<Integer> roots = findRoots();
        createOrder(roots.iterator().next(), visited, result);
        return result;
    }

    /**
     * Creates an ordered group of string IDs, that means a set of string IDs, for a given string ID. For the given ID
     * all its parents and children are added to this group. The parents and children are then searched for their
     * parents and children as well.
     *
     * @param id        the string ID that is added to the group, including its children and parents
     * @param collector te set that collects the linearized string IDs
     * @param visited   all already visited IDs
     */
    private void createOrder(int id, Set<Integer> visited, Set<Integer> collector) {

        // If there is an not yet visited parent of the ID it is called first
        int firstUnvisitedParent = getFirstUnvisitedParent(idsToDialogs.get(id).getParents(), visited);
        if (firstUnvisitedParent != -1) {
            createOrder(firstUnvisitedParent, visited, collector);
            // The current ID will be visited again as it is a child of its parent node, so do nothing here
            return;
        }
        // Adds the ID to the collector and marks it as visited
        collector.add(id);
        visited.add(id);
        // Get all children which have parents (beside the current ID) that are not marked as visited
        Set<Integer> childrenWithUnvisitedParents =
                getChildrenWithUnvisitedParents(idsToDialogs.get(id).getChildren(), visited);

        if (childrenWithUnvisitedParents.isEmpty()) {
            // If there are no such children, add all children of the current ID to the collector and visit them one
            // by one
            collector.addAll(idsToDialogs.get(id).getChildren());
            for (int each : idsToDialogs.get(id).getChildren()) {
                if (!visited.contains(each)) {
                    createOrder(each, visited, collector);
                }
            }
        } else {
            // If there are such children, visit them and add afterwards all remaining children of the current ID to
            // the collector
            for (int each : childrenWithUnvisitedParents) {
                if (!visited.contains(each)) {
                    createOrder(each, visited, collector);
                }
            }
            collector.addAll(idsToDialogs.get(id).getChildren());
        }
    }

    /**
     * Returns all of the given children of a string ID that are not contained in the given set visited
     *
     * @param children all children of a string ID
     * @param visited  all string IDs that were already marked as visited
     * @return all unvisited children string IDs
     */
    private Set<Integer> getChildrenWithUnvisitedParents(Set<Integer> children, Set<Integer> visited) {
        Set<Integer> result = new LinkedHashSet<>();
        for (int eachChild : children) {
            // If the child was already visited, ignore it
            if (visited.contains(eachChild)) {
                continue;
            }
            // Otherwise go through all parents of the child
            for (int eachParent : idsToDialogs.get(eachChild).getParents()) {
                // If the parent of the child is not marked as visited, add the child to the result and proceed with
                // the next child
                if (!visited.contains(eachParent)) {
                    result.add(eachChild);
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Returns the first found parent of a given set of parents that is not contained in the given set visited
     *
     * @param parents all parents of a string ID
     * @param visited all string IDs that were already marked as visited
     * @return the first found unvisited parent
     */
    private int getFirstUnvisitedParent(Set<Integer> parents, Set<Integer> visited) {
        for (int each : parents) {
            // If the parent is not makred as visited, return it
            if (!visited.contains(each)) {
                return each;
            }
        }
        return -1;
    }

    /**
     * Returns all strings that have no parents.
     *
     * @return the parent-less string IDs
     */
    private Set<Integer> findRoots() {
        Set<Integer> result = new TreeSet<>();
        for (Map.Entry<Integer, DialogString> each : idsToDialogs.entrySet()) {
            if (each.getValue().getParents().isEmpty()) {
                // If string has no parents it is a root node
                result.add(each.getKey());
            }
        }
        return result;
    }

    /**
     * Checks whether the given string ID and one of its given predecessors are part of a cyclic dialog structure.
     *
     * @param id           the string ID
     * @param predecessors all predecessors of the given string
     * @return true, if the string with the given ID and one of its predecessors are part of a cyclic dialog structure
     */
    private boolean checkForCycles(int id, Set<Integer> predecessors) {
        // If the current ID is already one of its predecessors, we have found a cycle
        if (predecessors.contains(id)) {
            return true;
        }
        // Otherwise loop through all children
        for (int each : idsToDialogs.get(id).getChildren()) {
            Set<Integer> childPredecessors = new TreeSet<>(predecessors);
            childPredecessors.add(id);
            if (checkForCycles(each, childPredecessors)) {
                return true;
            }
        }
        return false;
    }

}
