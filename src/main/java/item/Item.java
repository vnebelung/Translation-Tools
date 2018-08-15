package item;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * This class represents an item with its string IDs.
 */
class Item implements Comparable<Item> {

    private final int generalName;
    private final int identifiedName;
    private final int generalDescription;
    private final int identifiedDescription;
    private String fileName;

    /**
     * Constructs a new item that represents an in-game item with its four string IDs.
     *
     * @param fileName              the item's file name
     * @param generalName           the item's "general name" string ID
     * @param identifiedName        the item's "identified name" string ID
     * @param generalDescription    the item's "general description" string ID
     * @param identifiedDescription the item's "identified description" string ID
     */
    Item(String fileName, int generalName, int identifiedName, int generalDescription, int identifiedDescription) {
        this.fileName = fileName;
        this.generalName = generalName;
        this.identifiedName = identifiedName;
        this.generalDescription = generalDescription;
        this.identifiedDescription = identifiedDescription;
    }

    /**
     * Returns the "general name" string ID of the item.
     *
     * @return the "general name" string ID.
     */
    int getGeneralName() {
        return generalName;
    }

    /**
     * Returns the "identified name" string ID of the item.
     *
     * @return the "identified name" string ID.
     */
    int getIdentifiedName() {
        return identifiedName;
    }

    /**
     * Returns the "general description" string ID of the item.
     *
     * @return the "general description" string ID.
     */
    int getGeneralDescription() {
        return generalDescription;
    }

    /**
     * Returns the "identified description" string ID of the item.
     *
     * @return the "identified description" string ID.
     */
    int getIdentifiedDescription() {
        return identifiedDescription;
    }

    /**
     * Returns whether the "identified description" string ID is in the given range.
     *
     * @param minInclusive the minimum string ID, inclusive
     * @param maxInclusive the maximum string ID, inclusive
     * @return true if "identified description" is in the given range
     */
    boolean isIdentifiedDescriptionInRange(int minInclusive, int maxInclusive) {
        return minInclusive <= identifiedDescription && identifiedDescription <= maxInclusive;
    }

    /**
     * Returns whether the "identified name" string ID is in the given range.
     *
     * @param minInclusive the minimum string ID, inclusive
     * @param maxInclusive the maximum string ID, inclusive
     * @return true if "identified name" is in the given range
     */
    boolean isIdentifiedNameInRange(int minInclusive, int maxInclusive) {
        return minInclusive <= identifiedName && identifiedName <= maxInclusive;
    }

    /**
     * Returns whether the "general description" string ID is in the given range.
     *
     * @param minInclusive the minimum string ID, inclusive
     * @param maxInclusive the maximum string ID, inclusive
     * @return true if "general description" is in the given range
     */
    boolean isGeneralDescriptionInRange(int minInclusive, int maxInclusive) {
        return minInclusive <= generalDescription && generalDescription <= maxInclusive;
    }

    /**
     * Returns whether the "general name" string ID is in the given range.
     *
     * @param minInclusive the minimum string ID, inclusive
     * @param maxInclusive the maximum string ID, inclusive
     * @return true if "general name" is in the given range
     */
    boolean isGeneralNameInRange(int minInclusive, int maxInclusive) {
        return minInclusive <= generalName && generalName <= maxInclusive;
    }

    /**
     * Returns whether all four string IDs are in the given range.
     *
     * @param minInclusive the minimum string ID, inclusive
     * @param maxInclusive the maximum string ID, inclusive
     * @return true if all four string IDs are in the given range
     */
    boolean isInRange(int minInclusive, int maxInclusive) {
        if (isGeneralNameInRange(minInclusive, maxInclusive)) {
            return true;
        }
        if (isIdentifiedNameInRange(minInclusive, maxInclusive)) {
            return true;
        }
        if (isGeneralDescriptionInRange(minInclusive, maxInclusive)) {
            return true;
        }
        return isIdentifiedDescriptionInRange(minInclusive, maxInclusive);
    }

    /**
     * Compares this object with the specified object for order. Returns a negative integer, zero, or a positive integer
     * as this object is less than, equal to, or greater than the specified object.
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
    public int compareTo(Item o) {
        SortedSet<Integer> ids = new TreeSet<>();
        ids.add(generalName);
        ids.add(generalDescription);
        ids.add(identifiedName);
        ids.add(identifiedDescription);
        SortedSet<Integer> idsO = new TreeSet<>();
        idsO.add(o.generalName);
        idsO.add(o.generalDescription);
        idsO.add(o.identifiedName);
        idsO.add(o.identifiedDescription);
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
