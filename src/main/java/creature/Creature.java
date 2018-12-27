package creature;

import java.util.Arrays;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * This class represents an creature with its string IDs.
 */
class Creature implements Comparable<Creature> {

    private int shortName;
    private int longName;
    private Set<Integer> pertainingStrings;
    private String fileName;

    /**
     * Constructs a new creature that represents an in-game creature with its 102 string IDs.
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

    /**
     * Compares this object with the specified object for order.  Returns a negative integer, zero, or a positive
     * integer as this object is less than, equal to, or greater than the specified object.
     * <p>
     * <p>The implementor must ensure
     * {@code sgn(x.compareTo(y)) == -sgn(y.compareTo(x))} for all {@code x} and {@code y}.  (This implies that {@code
     * x.compareTo(y)} must throw an exception iff {@code y.compareTo(x)} throws an exception.)
     * <p>
     * <p>The implementor must also ensure that the relation is transitive:
     * {@code (x.compareTo(y) > 0 && y.compareTo(z) > 0)} implies {@code x.compareTo(z) > 0}.
     * <p>
     * <p>Finally, the implementor must ensure that {@code x.compareTo(y)==0}
     * implies that {@code sgn(x.compareTo(z)) == sgn(y.compareTo(z))}, for all {@code z}.
     * <p>
     * <p>It is strongly recommended, but <i>not</i> strictly required that
     * {@code (x.compareTo(y)==0) == (x.equals(y))}.  Generally speaking, any class that implements the {@code
     * Comparable} interface and violates this condition should clearly indicate this fact.  The recommended language is
     * "Note: this class has a natural ordering that is inconsistent with equals."
     * <p>
     * <p>In the foregoing description, the notation
     * {@code sgn(}<i>expression</i>{@code )} designates the mathematical
     * <i>signum</i> function, which is defined to return one of {@code -1},
     * {@code 0}, or {@code 1} according to whether the value of
     * <i>expression</i> is negative, zero, or positive, respectively.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     * the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it from being compared to this object.
     */
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
