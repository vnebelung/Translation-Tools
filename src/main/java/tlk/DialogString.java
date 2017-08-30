/*
 * This file is part of the Translation Tools, modified on 27.08.17 22:15.
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See the COPYING file for more details.
 */

package tlk;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This class is responsible for storing the text of a string and the relation of the string to its parents and
 * children.
 */
public class DialogString implements Comparable<DialogString> {

    private static int counter = 0;
    private int id;
    private String text;
    private Type type;
    private Set<Integer> children = new LinkedHashSet<>();
    private Set<Integer> parents = new LinkedHashSet<>();

    /**
     * Constructs the dialog string. This constructor is private because the construction is done via the factory
     * pattern because the string has to have an unique ID. This ID has nothing to do with string IDs and is only used
     * for class internal reasons so that different instances of this class containing the same text can be
     * distinguished from each other.
     *
     * @param text the string text
     * @param type the string type
     * @param id   the internal id
     */
    private DialogString(String text, Type type, int id) {
        this.text = text;
        this.type = type;
        this.id = id;
    }

    /**
     * Constructs a new dialog string with a given text and type.
     *
     * @param text the string text
     * @param type the string type
     * @return the constructed dialog string
     */
    static DialogString create(String text, Type type) {
        DialogString result = new DialogString(text, type, counter);
        counter++;
        return result;
    }

    /**
     * Adds a dialog string represented by its string ID as a child of this dialog string.
     *
     * @param child the child's string ID
     */
    void addChild(int child) {
        children.add(child);
    }

    /**
     * Adds a dialog string represented by its string ID as a parent of this dialog string.
     *
     * @param parent the parent's string ID
     */
    void addParent(int parent) {
        parents.add(parent);
    }

    /**
     * Removes a dialog string represented by its string ID from the parents of this dialog string.
     *
     * @param parent the parent's string ID
     */
    void removeParent(int parent) {
        parents.remove(parent);
    }

    /**
     * Removes a dialog string represented by its string ID from the children of this dialog string.
     *
     * @param child the child's string ID
     */
    void removeChild(int child) {
        children.remove(child);
    }

    /**
     * Returns the string IDs of all children of this dialog string
     *
     * @return the children's string IDs
     */
    Set<Integer> getChildren() {
        return children;
    }

    /**
     * Returns the string IDs of all parents of this string
     *
     * @return the parent's string IDs
     */
    Set<Integer> getParents() {
        return parents;
    }

    /**
     * Returns the text of this dialog string.
     *
     * @return the text
     */
    String getText() {
        return text;
    }

    /**
     * Compares this object with the specified object for order.  Returns a negative integer, zero, or a positive
     * integer as this object is less than, equal to, or greater than the specified object.
     * <p>
     * <p>The implementor must ensure <tt>sgn(x.compareTo(y)) == -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and
     * <tt>y</tt>.  (This implies that <tt>x.compareTo(y)</tt> must throw an exception iff <tt>y.compareTo(x)</tt>
     * throws an exception.)
     * <p>
     * <p>The implementor must also ensure that the relation is transitive: <tt>(x.compareTo(y)&gt;0 &amp;&amp;
     * y.compareTo(z)&gt;0)</tt> implies <tt>x.compareTo(z)&gt;0</tt>.
     * <p>
     * <p>Finally, the implementor must ensure that <tt>x.compareTo(y)==0</tt> implies that <tt>sgn(x.compareTo(z)) ==
     * sgn(y.compareTo(z))</tt>, for all <tt>z</tt>.
     * <p>
     * <p>It is strongly recommended, but <i>not</i> strictly required that <tt>(x.compareTo(y)==0) ==
     * (x.equals(y))</tt>.  Generally speaking, any class that implements the <tt>Comparable</tt> interface and violates
     * this condition should clearly indicate this fact.  The recommended language is "Note: this class has a natural
     * ordering that is inconsistent with equals."
     * <p>
     * <p>In the foregoing description, the notation <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the
     * mathematical <i>signum</i> function, which is defined to return one of <tt>-1</tt>, <tt>0</tt>, or <tt>1</tt>
     * according to whether the value of <i>expression</i> is negative, zero or positive.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     * the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it from being compared to this object.
     */
    @Override
    public int compareTo(DialogString o) {
        int result = text.compareTo(o.text);
        if (result == 0) {
            return id - o.id;
        }
        return result;
    }

    /**
     * Returns the type of this dialog string.
     *
     * @return the type
     */
    Type getType() {
        return type;
    }

    enum Type {DIALOG, JOURNAL, ERROR}
}
