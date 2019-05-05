/*
 * This file is part of the Translation Tools, modified on 29.08.17 22:47.
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See the COPYING file for more details.
 */

package main;

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
        CommandLine commandLine = new CommandLine("TranslationTools.jar",
                "The Translation Tools can assist in translating " +
                        "games that are based on the Infinity engine, like BG:EE or BG:SoD. The tools can extract " +
                        "string " +
                        "IDs and generate dialog structures from Infinity engine files, or can generate statistics " +
                        "about the " + "translation process.");

        Subcommand progress = commandLine.createSubcommand("progress", "Generates a graphic that visualizes" +
                " the translation progress of a project and stores it into a PNG file, and additionally writes a " +
                "table with textual information into a TXT file. If the PNG file and/or the TXT table is already " +
                "existing, they are updated with the current progress data.");
        progress.add(commandLine.createStringParameter("complete-csv",
                "The path to the CSV file that contains all strings of a translation project, exported from the " +
                        "online translation tool."));
        progress.add(commandLine.createStringParameter("out-of-date-csv",
                "The path to the CSV file that contains all out-of-date strings of a translation project, exported " +
                        "from the online translation tool."));
        progress.add(commandLine.createStringParameter("unused-txt",
                "The path to the TXT file that contains all unused strings of a translation project, exported from " +
                        "NearInfinity with an up-to-date version of the game in focus."));
        progress.add(commandLine.createStringParameter("out-png",
                "The path to the PNG file that will contain the visualized progress information. If this file is " +
                        "already present, it will be updated with the current progress data."));
        progress.add(commandLine.createStringParameter("out-txt",
                "The path to the TXT file that will contain the textual progress information. If this file is " +
                        "already present, it will be updated with the current progress data."));
        progress.add(commandLine.createBooleanParameter("ignore-unused", "When set to " +
                "true, it is assumed that unused strings are ignored during the translation. This will affect the " +
                "textual progress information.").withDefaultValue(false));
        progress.add(commandLine.createIntegerParameter("suggestions",
                "The number of " + "suggestions as displayed in the translation tool."));
        commandLine.getCommand().add(progress);

        Subcommand dialogs = commandLine.createSubcommand("dialogs",
                "Generates an HTML file that contains the dialog structure and a TXT file with grouped dialog string " +
                        "IDs of BAF and D files of the game in focus.");
        dialogs.add(commandLine.createStringParameter("baf-folder",
                "The path to the folder that contains all BAF files of a game, exported from NearInfinity " +
                        "with an up-to-date version of the game in focus."));
        dialogs.add(commandLine.createStringParameter("d-folder",
                "The path to the folder that contains all D files of a game, exported from NearInfinity " +
                        "with an up-to-date version of the game in focus."));
        dialogs.add(commandLine.createIntegerParameter("string-id-from",
                "The string ID (inclusive) that is the lower bound of IDs that shall be parsed.").withDefaultValue(0));
        dialogs.add(commandLine.createIntegerParameter("string-id-to",
                "The string ID (inclusive) that is the upper bound of IDs that shall be parsed."));
        dialogs.add(commandLine.createStringParameter("out-html",
                "The path of the file to that the HTML containing the dialog structure will be written."));
        dialogs.add(commandLine.createStringParameter("out-txt",
                "The path of the file to that the TXT file containing the dialog groups will be written."));
        commandLine.getCommand().add(dialogs);

        Subcommand items = commandLine.createSubcommand("items",
                "Generates a CSV and a TXT file with all item strings of ITM files of the game in focus.");
        items.add(commandLine.createStringParameter("itm-folder",
                "The path to the folder that contains all ITM files of a game, exported from NearInfinity " +
                        "with an up-to-date version of the game in focus."));
        items.add(commandLine.createIntegerParameter("string-id-from",
                "The string ID (inclusive) that is the lower bound of IDs that shall be parsed.").withDefaultValue(0));
        items.add(commandLine.createIntegerParameter("string-id-to",
                "The string ID (inclusive) that is the upper bound of IDs that shall be parsed.")
                .withDefaultValue(Integer.MAX_VALUE));
        items.add(commandLine.createStringParameter("out-txt",
                "The path to the TXT file that will contain all string IDs of the parsed items."));
        items.add(commandLine.createStringParameter("out-csv",
                "The path to the CSV file that will contain all string IDs of the parsed items."));
        commandLine.getCommand().add(items);

        Subcommand creatures = commandLine.createSubcommand("creatures",
                "Generates a CSV and a TXT file with all creature strings of CRE files of the game in focus.");
        creatures.add(commandLine.createStringParameter("cre-folder",
                "The path to the folder that contains all CRE files of a game, exported from NearInfinity " +
                        "with an up-to-date version of the game in focus."));
        creatures.add(commandLine.createIntegerParameter("string-id-from",
                "The string ID (inclusive) that is the lower bound of IDs that shall be parsed. The default value is " +
                        "0.").withDefaultValue(0));
        creatures.add(commandLine.createIntegerParameter("string-id-to",
                "The string ID (inclusive) that is the upper bound of IDs that shall be parsed. The default value is " +
                        "4,294,967,296.").withDefaultValue(Integer.MAX_VALUE));
        creatures.add(commandLine.createStringParameter("out-txt",
                "The path to the TXT file that will contain all string IDs of the parsed creatures."));
        creatures.add(commandLine.createStringParameter("out-csv",
                "The path to the CSV file that will contain all string IDs of the parsed creatures."));
        commandLine.getCommand().add(creatures);

        Subcommand tables = commandLine.createSubcommand("tables",
                "Generates aTXT file with all table string IDs of " + "2DA files of the game in focus.");
        tables.add(commandLine.createStringParameter("2da-folder", "The path to the folder that contains all 2DA " +
                "files of a game, exported from NearInfinity with an up-to-date version of the game in focus."));
        tables.add(commandLine.createIntegerParameter("string-id-from",
                "The string ID (inclusive) that is the lower bound of IDs that shall be parsed. The default value is " +
                        "0.").withDefaultValue(0));
        tables.add(commandLine.createIntegerParameter("string-id-to",
                "The string ID (inclusive) that is the upper bound of IDs that shall be parsed. The default value is " +
                        "4,294,967,296.").withDefaultValue(Integer.MAX_VALUE));
        tables.add(commandLine.createStringParameter("out-txt",
                "The path to the TXT file that will contain all string IDs of the parsed tables."));
        commandLine.getCommand().add(tables);

        ParsedCommand parsedCommand;
        try {
            parsedCommand = commandLine.parse(args);
        } catch (ParameterException e) {
            System.out.println(e.getMessage());
            return;
        }

        try {
            switch (parsedCommand.getSubcommand().getName()) {
                case "progress":
                    Mode progressMode = new Mode();
                    progressMode.invoke(parsedCommand.getSubcommand().getStringParameter("complete-csv").getValue(),
                            parsedCommand.getSubcommand().getStringParameter("out-of-date-csv").getValue(),
                            parsedCommand.getSubcommand().getStringParameter("unused-txt").getValue(),
                            parsedCommand.getSubcommand().getStringParameter("out-png").getValue(),
                            parsedCommand.getSubcommand().getStringParameter("out-txt").getValue(),
                            parsedCommand.getSubcommand().getIntegerParameter("suggestions").getValue(),
                            parsedCommand.getSubcommand().getBooleanParameter("ignore-unused").getValue());
                    break;
                case "dialogs":
                    dialog.Mode dialogMode = new dialog.Mode();
                    dialogMode.invoke(parsedCommand.getSubcommand().getIntegerParameter("string-id-from").getValue(),
                            parsedCommand.getSubcommand().getIntegerParameter("string-id-to").getValue(),
                            parsedCommand.getSubcommand().getStringParameter("baf-folder").getValue(),
                            parsedCommand.getSubcommand().getStringParameter("d-folder").getValue(),
                            parsedCommand.getSubcommand().getStringParameter("out-html").getValue(),
                            parsedCommand.getSubcommand().getStringParameter("out-txt").getValue());
                    break;
                case "items":
                    item.Mode itemMode = new item.Mode();
                    itemMode.invoke(parsedCommand.getSubcommand().getStringParameter("itm-folder").getValue(),
                            parsedCommand.getSubcommand().getIntegerParameter("string-id-from").getValue(),
                            parsedCommand.getSubcommand().getIntegerParameter("string-id-to").getValue(),
                            parsedCommand.getSubcommand().getStringParameter("out-txt").getValue(),
                            parsedCommand.getSubcommand().getStringParameter("out-csv").getValue());
                    break;
                case "creatures":
                    creature.Mode creatureMode = new creature.Mode();
                    creatureMode.invoke(parsedCommand.getSubcommand().getStringParameter("cre-folder").getValue(),
                            parsedCommand.getSubcommand().getStringParameter("out-txt").getValue(),
                            parsedCommand.getSubcommand().getStringParameter("out-csv").getValue(),
                            parsedCommand.getSubcommand().getIntegerParameter("string-id-from").getValue(),
                            parsedCommand.getSubcommand().getIntegerParameter("string-id-to").getValue());
                    break;
                case "tables":
                    table.Mode tableMode = new table.Mode();
                    tableMode.invoke(parsedCommand.getSubcommand().getStringParameter("2da-folder").getValue(),
                            parsedCommand.getSubcommand().getStringParameter("out-txt").getValue(),
                            parsedCommand.getSubcommand().getIntegerParameter("string-id-from").getValue(),
                            parsedCommand.getSubcommand().getIntegerParameter("string-id-to").getValue());
                    break;
            }
        } catch (IOException | TransformerException | ParserConfigurationException e) {
            e.printStackTrace();
        }

    }

}
