package table;

import java.util.Set;
import java.util.TreeSet;

/**
 * This class represents a table with its string IDs.
 */
public class Table implements Comparable<Table> {

    private String fileName;
    private Set<Integer> stringIds = new TreeSet<>();

    /**
     * Constructs a new table that represents an in-game table with its string IDs.
     *
     * @param fileName the table's file name
     */
    Table(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Returns the file name of the table.
     *
     * @return the table's file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Returns all string IDs of the table.
     *
     * @return the table's string IDs
     */
    public Set<Integer> getStringIds() {
        return stringIds;
    }

    /**
     * Adds the given string ID to the string IDs of this table..
     *
     * @param stringId the string ID that will be added
     */
    public void add(Integer stringId) {
        stringIds.add(stringId);
    }

    @Override
    public int compareTo(Table o) {
        return fileName.compareTo(o.fileName);
    }

    /**
     * Returns whether all string IDs are in the given range.
     *
     * @param minInclusive the minimum string ID, inclusive
     * @param maxInclusive the maximum string ID, inclusive
     * @return true if all string IDs are in the given range
     */
    boolean isInRange(int minInclusive, int maxInclusive) {
        for (Integer stringId : stringIds) {
            if (stringId >= minInclusive && stringId <= maxInclusive) {
                return true;
            }
        }
        return false;
    }
}
