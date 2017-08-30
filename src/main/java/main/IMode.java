/*
 * This file is part of the Translation Tools, modified on 26.08.17 01:18.
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See the COPYING file for more details.
 */

package main;

/**
 * This is an interface for all mode classes.
 */
public interface IMode {

    /**
     * Invokes the user chosen functionality.
     *
     * @param parameters command line parameters needed for invoking the mode
     * @throws Exception if an exception has occurred.
     */
    void invoke(String... parameters) throws Exception;

}
