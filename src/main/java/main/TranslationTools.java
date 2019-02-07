/*
 * This file is part of the Translation Tools, modified on 29.08.17 22:47.
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See the COPYING file for more details.
 */

package main;

import commands.Command;
import commands.Subcommand;
import parameters.BooleanParameter;
import parameters.IntegerParameter;
import parameters.StringParameter;
import progress.Mode;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

/**
 * This class is the main class that is called when you start the tool over a the command line.
 */
class TranslationTools {

    /**
     * The main function is responsible for taking the command line parameters and passes them on for further
     * processing.
     *
     * @param args the command line parameters
     */
    public static void main(String[] args) {
        TranslationTools tools = new TranslationTools();
        tools.start(args);
    }

    /**
     * Starts the program by interpreting the first given command line parameter that indicates what mode shall be
     * invoked.
     *
     * @param args the command line parameters
     */
    private void start(String[] args) {
        if (args.length == 0) {
            // Avoid an exception in the following switch statement
            args = new String[1];
            args[0] = "";
        }

        // Prepare all possible modes
        CommandLine commandLine = new CommandLine();
        commandLine.createCommand("TranslationTools.jar", "The Translation Tools can assist in translating " +
                "games that are based on the Infinity engine, like BG:EE or BG:SoD. The tools can extract string " +
                "IDs and generate dialog structures from Infinity engine files, or can generate statistics about the " +
                "translation process.");
        commandLine.getCommand().add(new Subcommand("progress", "Generates a graphic that visualizes" +
                " the translation progress of a project and stores it into a PNG file, and additionally writes a " +
                "table with textual information into a TXT file. If the PNG file and/or the TXT table is already " +
                "existing, they are updated with the current progress data."));
        commandLine.getCommand().getSubcommand("progress").add(new StringParameter("complete-csv",
                "The path to the CSV file that contains all strings of a translation project, exported from the " +
                        "online translation tool."));
        commandLine.getCommand().getSubcommand("progress").add(new StringParameter("out-of-date-csv",
                "The path to the CSV file that contains all out-of-date strings of a translation project, exported " +
                        "from the online translation tool."));
        commandLine.getCommand().getSubcommand("progress").add(new StringParameter("unused-txt",
                "The path to the TXT file that contains all unused strings of a translation project, exported from " +
                        "NearInfinity with an up-to-date version of the game in focus."));
        commandLine.getCommand().getSubcommand("progress").add(new StringParameter("out-png",
                "The path to the PNG file that will contain the visualized progress information. If this file is " +
                        "already present, it will be updated with the current progress data."));
        commandLine.getCommand().getSubcommand("progress").add(new StringParameter("out-txt",
                "The path to the TXT file that will contain the textual progress information. If this file is " +
                        "already present, it will be updated with the current progress data."));
        commandLine.getCommand().getSubcommand("progress").add(new BooleanParameter("ignore-unused", "When set to " +
                "true, it is assumed that unused strings are ignored during the translation. This will affect the " +
                "textual progress information.", false));
        commandLine.getCommand().getSubcommand("progress").add(new IntegerParameter("suggestions",
                "The number of " + "suggestions as displayed in the translation tool."));
        commandLine.getCommand().add(new Subcommand("dialogs",
                "Generates an HTML file that contains the dialog structure and a TXT file with grouped dialog string " +
                        "IDs of BAF and D files of the game in focus."));
        commandLine.getCommand().getSubcommand("dialogs").add(new StringParameter("baf-folder",
                "The path to the folder that contains all BAF files of a game, exported from NearInfinity " +
                        "with an up-to-date version of the game in focus."));
        commandLine.getCommand().getSubcommand("dialogs").add(new StringParameter("d-folder",
                "The path to the folder that contains all D files of a game, exported from NearInfinity " +
                        "with an up-to-date version of the game in focus."));
        commandLine.getCommand().getSubcommand("dialogs").add(new IntegerParameter("string-id-from",
                "The string ID (inclusive) that is the lower bound of IDs that shall be parsed.", 0));
        commandLine.getCommand().getSubcommand("dialogs").add(new IntegerParameter("string-id-to",
                "The string ID (inclusive) that is the upper bound of IDs that shall be parsed."));
        commandLine.getCommand().getSubcommand("dialogs").add(new StringParameter("out-html",
                "The path of the file to that the HTML containing the dialog structure will be written."));
        commandLine.getCommand().getSubcommand("dialogs").add(new StringParameter("out-txt",
                "The path of the file to that the TXT file containing the dialog groups will be written."));
        commandLine.getCommand().add(new Subcommand("items",
                "Generates a CSV an a TXT file with all item strings of ITM files of the game in focus."));
        commandLine.getCommand().getSubcommand("items").add(new StringParameter("itm-folder",
                "The path to the folder that contains all ITM files of a game, exported from NearInfinity " +
                        "with an up-to-date version of the game in focus."));
        commandLine.getCommand().getSubcommand("items").add(new IntegerParameter("string-id-from",
                "The string ID (inclusive) that is the lower bound of IDs that shall be parsed.", 0));
        commandLine.getCommand().getSubcommand("items").add(new IntegerParameter("string-id-to",
                "The string ID (inclusive) that is the upper bound of IDs that shall be parsed.", Integer.MAX_VALUE));
        commandLine.getCommand().getSubcommand("items").add(new StringParameter("out-txt",
                "The path to the TXT file that will contain all string IDs of the parsed items."));
        commandLine.getCommand().getSubcommand("items").add(new StringParameter("out-csv",
                "The path to the CSV file that will contain all string IDs of the parsed items."));
        commandLine.getCommand().add(new Subcommand("creatures",
                "Generates a CSV an a TXT file with all creature strings of CRE files of the game in focus."));
        commandLine.getCommand().getSubcommand("creatures").add(new StringParameter("cre-folder",
                "The path to the folder that contains all CRE files of a game, exported from NearInfinity " +
                        "with an up-to-date version of the game in focus."));
        commandLine.getCommand().getSubcommand("creatures").add(new IntegerParameter("string-id-from",
                "The string ID (inclusive) that is the lower bound of IDs that shall be parsed. The default value is " +
                        "0.", 0));
        commandLine.getCommand().getSubcommand("creatures").add(new IntegerParameter("string-id-to",
                "The string ID (inclusive) that is the upper bound of IDs that shall be parsed. The default value is " +
                        "4,294,967,296.", Integer.MAX_VALUE));
        commandLine.getCommand().getSubcommand("creatures").add(new StringParameter("out-txt",
                "The path to the TXT file that will contain all string IDs of the parsed creatures."));
        commandLine.getCommand().getSubcommand("creatures").add(new StringParameter("out-csv",
                "The path to the CSV file that will contain all string IDs of the parsed creatures."));

        Subcommand subcommand;
        try {
            Command command = commandLine.parse(args);
            subcommand = command.getSubcommands().iterator().next();
        } catch (ParameterException e) {
            System.out.println(e.getMessage());
            return;
        }

        try {
            switch (subcommand.getName()) {
                case "progress":
                    Mode progressMode = new Mode();
                    progressMode.invoke((String) subcommand.getParameter("complete-csv").getValue(),
                            (String) subcommand.getParameter("out-of-date-csv").getValue(),
                            (String) subcommand.getParameter("unused-txt").getValue(),
                            (String) subcommand.getParameter("out-png").getValue(),
                            (String) subcommand.getParameter("out-txt").getValue(),
                            (Integer) subcommand.getParameter("suggestions").getValue(),
                            (Boolean) subcommand.getParameter("ignore-unused").getValue());
                    break;
                case "dialogs":
                    dialog.Mode dialogMode = new dialog.Mode();
                    dialogMode.invoke((Integer) subcommand.getParameter("string-id-from").getValue(),
                            (Integer) subcommand.getParameter("string-id-to").getValue(),
                            (String) subcommand.getParameter("baf-folder").getValue(),
                            (String) subcommand.getParameter("d-folder").getValue(),
                            (String) subcommand.getParameter("out-html").getValue(),
                            (String) subcommand.getParameter("out-txt").getValue());
                    break;
                case "items":
                    item.Mode itemMode = new item.Mode();
                    itemMode.invoke((String) subcommand.getParameter("itm-folder").getValue(),
                            (Integer) subcommand.getParameter("string-id-from").getValue(),
                            (Integer) subcommand.getParameter("string-id-to").getValue(),
                            (String) subcommand.getParameter("out-txt").getValue(),
                            (String) subcommand.getParameter("out-csv").getValue());
                    break;
                case "creatures":
                    creature.Mode creatureMode = new creature.Mode();
                    creatureMode.invoke((String) subcommand.getParameter("cre-folder").getValue(),
                            (String) subcommand.getParameter("out-txt").getValue(),
                            (String) subcommand.getParameter("out-csv").getValue(),
                            (Integer) subcommand.getParameter("string-id-from").getValue(),
                            (Integer) subcommand.getParameter("string-id-to").getValue());
                    break;
            }
        } catch (IOException | TransformerException | ParserConfigurationException e) {
            e.printStackTrace();
        }

    }

}
