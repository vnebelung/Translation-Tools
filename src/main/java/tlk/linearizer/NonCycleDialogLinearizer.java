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
        // Find all strings that have no parents
        Set<Integer> roots = findRoots();
        return createOrder(roots);
    }

    /**
     * Creates an ordered group of string IDs, that means a set of string IDs, for given root string ID. For the
     * given IDs all their children and descendants are added to this group.
     *
     * @param roots the string IDs of all dialog roots
     * @return the set that collects the linearized string IDs
     */
    private Set<Integer> createOrder(Set<Integer> roots) {
        // Initialize the temporary list that contains string IDs which need to be visited
        Deque<Integer> deque = new LinkedList<>(roots);
        // Initialize the result that contains at the end all ordered string IDs in their order
        Set<Integer> result = new LinkedHashSet<>(idsToDialogs.size());

        // While the temporary list contains string IDs, continue
        while (!deque.isEmpty()) {
            // Rotate a string ID at the first position that is the best candidate fo being the next ID in the
            // resulting list
            rotateToCandidate(deque, result, 0);

            // Find all siblings of the first string ID (including itself) and add it to the resulting list. So all
            // siblings stay close together to make the process of translating easier
            Set<Integer> siblings = findSiblings(deque.removeFirst());
            deque.removeAll(siblings);
            result.addAll(siblings);

            // Add all children of the siblings to the temporary list
            LinkedList<Integer> children = new LinkedList<>();
            for (int each : siblings) {
                children.addAll(idsToDialogs.get(each).getChildren());
            }
            Iterator<Integer> reverseIterator = children.descendingIterator();
            while (reverseIterator.hasNext()) {
                int child = reverseIterator.next();
                if (!deque.contains(child) && !result.contains(child)) {
                    deque.addFirst(child);
                }
            }
        }
        return result;
    }

    /**
     * Finds for a given string ID all sibling IDs.
     *
     * @param id the string ID
     * @return siblings of the string ID, including the input ID
     */
    private Set<Integer> findSiblings(int id) {
        // If the string ID has no parents it is a root node
        if (idsToDialogs.get(id).getParents().isEmpty()) {
            return Collections.singleton(id);
        }
        Set<Integer> result = new LinkedHashSet<>();
        for (int eachParent : idsToDialogs.get(id).getParents()) {
            result.addAll(idsToDialogs.get(eachParent).getChildren());
        }
        return result;
    }

    /**
     * Rotates a string ID at the first position to the last position in the given queue if the string ID does have 1)
     * any not yet visited parents or 2) a sibling which has any not yet visited parents.
     *
     * @param deque   the string IDs
     * @param visited the visited string IDs
     */
    private void rotateToCandidate(Deque<Integer> deque, Set<Integer> visited, int iteration) {
        // If the iteration count is higher than the size of deque then there is no good candidate which fulfills 1)
        // and 2). Rotate to the minimal string ID instead
        if (iteration >= deque.size()) {
            if (deque.peekFirst() != Collections.min(deque).intValue()) {
                deque.addLast(deque.removeFirst());
                rotateToCandidate(deque, visited, iteration + 1);
            }
            return;
        }
        boolean isCandidate = true;
        outerLoop:
        for (int eachParent : idsToDialogs.get(deque.getFirst()).getParents()) {
            // Check for 1)
            if (!visited.contains(eachParent)) {
                isCandidate = false;
                break;
            }
            // Check for 2)
            for (int eachChild : idsToDialogs.get(eachParent).getChildren()) {
                for (int eachChildParent : idsToDialogs.get(eachChild).getParents()) {
                    if (!visited.contains(eachChildParent)) {
                        isCandidate = false;
                        break outerLoop;
                    }
                }
            }
        }
        if (isCandidate) {
            return;
        }
        // Rotate
        deque.addLast(deque.removeFirst());
        rotateToCandidate(deque, visited, iteration + 1);
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
