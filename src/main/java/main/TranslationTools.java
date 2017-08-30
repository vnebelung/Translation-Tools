/*
 * This file is part of the Translation Tools, modified on 29.08.17 22:47.
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See the COPYING file for more details.
 */

package main;

import status_image.ImageMode;
import tlk.DialogStructureMode;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is the main class that is called when you start the tool over a the command line.
 */
class TranslationTools {

    /**
     * The main function is responsible for taking the command line parameters and passes them on for further
     * processing.
     *
     * @param args the command line parameters
     * @throws IOException if an exception of some sort has occurred
     */
    public static void main(String[] args) throws Exception {
        TranslationTools tools = new TranslationTools();
        tools.start(args);
    }

    /**
     * Starts the program by interpreting the first given command line parameter that indicates what mode shall be
     * invoked.
     *
     * @param args the command line parameters
     * @throws Exception if an exception of some sort has occurred
     */
    private void start(String[] args) throws Exception {
        if (args.length == 0) {
            // Avoid an exception in the following switch statement
            args = new String[1];
            args[0] = "";
        }

        // Prepare all possible modes
        Map<String, IMode> modes = new HashMap<>();
        modes.put("status", new ImageMode());
        modes.put("dialog", new DialogStructureMode());

        if (!modes.containsKey(args[0])) {
            // If the first parameter is not a functionality defined above
            System.out.println("usage: java -jar TranslationTools.jar [status|dialog] [options]");
            System.out.println(
                    "status  = generates or updates a graphic that visualizes the translation status of a project");
            System.out.println("dialog  = analyzes TLK files and generates dialog structure overviews");
            return;
        }

        modes.get(args[0]).invoke(Arrays.copyOfRange(args, 1, args.length));
    }

}
