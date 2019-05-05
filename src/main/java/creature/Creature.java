package creature;

import java.util.Arrays;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * This class represents a creature with its string IDs.
 */
class Creature implements Comparable<Creature> {

    private int shortName;
    private int longName;
    private Set<Integer> pertainingStrings;
    private String fileName;

    /**
     * Constructs a new creature that represents an in-game creature with its string IDs.
     *
     * @param shortName         the creature's "short name" string ID
     * @param longName          the creatures's "long name" string ID
     * @param pertainingStrings the creature's "pertaining" string IDs
     */
    Creature(String fileName, int shortName, int longName, int[] pertainingStrings) {
        this.fileName = fileName;
        this.shortName = shortName;
        this.longName = longName;
        this.pertainingStrings = Arrays.stream(pertainingStrings).boxed().collect(Collectors.toSet());
    }

    /**
     * Returns the "short name" string ID of the creature.
     *
     * @return the "short name" string ID.
     */
    int getShortName() {
        return shortName;
    }

    /**
     * Returns the "long name" string ID of the creature.
     *
     * @return the "long name" string ID.
     */
    int getLongName() {
        return longName;
    }

    /**
     * Returns the "pertaining" string IDs of the creature that are in the given range.
     *
     * @param minInclusive the minimum string ID, inclusive
     * @param maxInclusive the maximum string ID, inclusive
     * @return the "pertaining" string ID.
     */
    Set<Integer> getPertainingStringsinRange(int minInclusive, int maxInclusive) {
        return pertainingStrings.stream().filter(p -> minInclusive <= p && p <= maxInclusive)
                .collect(Collectors.toSet());
    }

    /**
     * Returns whether the "short name" string ID is in the given range.
     *
     * @param minInclusive the minimum string ID, inclusive
     * @param maxInclusive the maximum string ID, inclusive
     * @return true if "short name" is in the given range
     */
    boolean isShortNameInRange(int minInclusive, int maxInclusive) {
        return minInclusive <= shortName && shortName <= maxInclusive;
    }

    /**
     * Returns whether the "long name" string ID is in the given range.
     *
     * @param minInclusive the minimum string ID, inclusive
     * @param maxInclusive the maximum string ID, inclusive
     * @return true if "long name" is in the given range
     */
    boolean isLongNameInRange(int minInclusive, int maxInclusive) {
        return minInclusive <= longName && longName <= maxInclusive;
    }

    /**
     * Returns whether one or more "pertaining" string ID are in the given range.
     *
     * @param minInclusive the minimum string ID, inclusive
     * @param maxInclusive the maximum string ID, inclusive
     * @return true if at least one "pertaining" string is in the given range
     */
    boolean isPertainingStringsInRange(int minInclusive, int maxInclusive) {
        for (Integer pertainingString : pertainingStrings) {
            if (minInclusive <= pertainingString && pertainingString <= maxInclusive) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether all string IDs are in the given range.
     *
     * @param minInclusive the minimum string ID, inclusive
     * @param maxInclusive the maximum string ID, inclusive
     * @return true if all string IDs are in the given range
     */
    boolean isInRange(int minInclusive, int maxInclusive) {
        if (isShortNameInRange(minInclusive, maxInclusive)) {
            return true;
        }
        if (isLongNameInRange(minInclusive, maxInclusive)) {
            return true;
        }
        return isPertainingStringsInRange(minInclusive, maxInclusive);
    }

    @Override
    public int compareTo(Creature o) {
        SortedSet<Integer> ids = new TreeSet<>();
        ids.add(shortName);
        ids.add(longName);
        ids.addAll(pertainingStrings);
        SortedSet<Integer> idsO = new TreeSet<>();
        idsO.add(o.shortName);
        idsO.add(o.longName);
        idsO.addAll(o.pertainingStrings);
        while (!ids.isEmpty() && !idsO.isEmpty()) {
            int min = ids.first();
            ids.remove(min);
            int minO = idsO.first();
            idsO.remove(minO);
            if (min != minO) {
                return min - minO;
            }
        }
        if (ids.isEmpty() && idsO.isEmpty()) {
            return fileName.compareTo(o.fileName);
        } else if (ids.isEmpty()) {
            return -1;
        } else {
            return 1;
        }
    }

    /**
     * Returns the name of the item file.
     *
     * @return the file's name
     */
    String getFileName() {
        return fileName;
    }
}
