/*
 * This file is part of the Translation Tools, modified on 29.08.17 21:48.
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See the COPYING file for more details.
 */

/*
 * The embedded font file "Latin Modern Sans 10 Regular" is released under the GUST Font License. See the
 * COPYING-lmsans10-regular file for details.
 */

package tlk;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

/**
 * This class is responsible for creating an HTML file from the parsed dialog structures of TLK files. The HTML displays
 * the dialog structures in a well formatted style. The string IDs are linked to each other so that the user can
 * navigate between different dialog trees.
 */
class HtmlCreator {

    public final static String OUTPUT_FILENAME = "DialogOverview.html";
    private SortedMap<String, List<Integer>> filenamesToIds;
    private SortedMap<Integer, DialogString> idsToDialogs;

    /**
     * Constructs the HTML creator. The HTML is constructed out of the given maps.
     *
     * @param filenamesToIds the map where the relations between filenames and string IDs are stored
     * @param idsToDialogs   the map where the relations between string IDs and dialog texts are stored
     */
    HtmlCreator(SortedMap<String, List<Integer>> filenamesToIds, SortedMap<Integer, DialogString> idsToDialogs) {
        this.filenamesToIds = filenamesToIds;
        this.idsToDialogs = idsToDialogs;
    }

    /**
     * Creates the DOM structure and writes it into an HTML file in the given folder.
     *
     * @param folder the output folder
     * @throws ParserConfigurationException if a DocumentBuilder cannot be created which satisfies the configuration
     *                                      requested
     * @throws TransformerException         if an unrecoverable error occurs during the course of the transformation
     */
    void create(Path folder) throws ParserConfigurationException, TransformerException {
        // Create a new DOM
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = documentBuilder.newDocument();

        // Create the HTML 5 document type
        DocumentType documentType = documentBuilder.getDOMImplementation().createDocumentType("html", "", "");
        document.appendChild(documentType);

        // Create the root html element
        Element html = document.createElement("html");
        // Create the head element
        html.appendChild(buildHead(document));
        // Create the body element
        html.appendChild(buildBody(document));
        document.appendChild(html);

        // Create an example at the beginning of the HTML
        createExample(document);

        // Write the DOM to the output folder
        writeToFile(document, folder);
    }

    /**
     * Creates an example of a dialog block. The example block's links are pointing nowhere and id attributes are
     * non-existent.
     *
     * @param document the DOM document
     */
    private void createExample(Document document) {

        Element example = document.getElementById("example");

        Element span0 = document.createElement("span");
        span0.setAttribute("class", "sayparent");
        example.appendChild(span0);

        Element a0 = document.createElement("a");
        a0.setAttribute("class", "idlink");
        a0.setAttribute("href", "#");
        a0.appendChild(document.createTextNode("#39280"));
        span0.appendChild(a0);

        Element span1 = document.createElement("span");
        span1.setAttribute("class", "supporttext");
        span1.appendChild(document.createTextNode(
                "▾ BDVOGHIJ: Ja. Be in good cheer! Soon, you will breathe unfouled air, feel wine upon your lips, " +
                        "take a lady to—well, maybe not that one. But these other pleasures await you."));
        example.appendChild(span1);

        Element span2 = document.createElement("span");
        span2.setAttribute("class", "sayparent");
        example.appendChild(span2);

        Element a1 = document.createElement("a");
        a1.setAttribute("class", "idlink");
        a1.setAttribute("href", "#");
        a1.appendChild(document.createTextNode("#39279"));
        span2.appendChild(a1);

        Element span3 = document.createElement("span");
        span3.setAttribute("class", "supporttext");
        span3.appendChild(document.createTextNode(
                "▾ BDMINSCJ: Boo says the paths we choose will take us where we're meant to be."));
        example.appendChild(span3);

        Element span4 = document.createElement("span");
        span4.setAttribute("class", "sayparent");
        example.appendChild(span4);

        Element a2 = document.createElement("a");
        a2.setAttribute("class", "idlink");
        a2.setAttribute("href", "#");
        a2.appendChild(document.createTextNode("#39268"));
        span4.appendChild(a2);

        Element span5 = document.createElement("span");
        span5.setAttribute("class", "supporttext");
        span5.appendChild(document.createTextNode("▾ You did everything you could to turn her from it."));
        example.appendChild(span5);

        Element span6 = document.createElement("span");
        span6.setAttribute("class", "sayparent");
        example.appendChild(span6);

        Element a3 = document.createElement("a");
        a3.setAttribute("class", "idlink");
        a3.setAttribute("href", "#");
        a3.appendChild(document.createTextNode("#39269"));
        span6.appendChild(a3);

        Element span7 = document.createElement("span");
        span7.setAttribute("class", "supporttext");
        span7.appendChild(document.createTextNode(
                "▾ And she paid the price for walking that path. But you are free again. That is something, surely?"));
        example.appendChild(span7);

        Element span8 = document.createElement("span");
        span8.setAttribute("class", "sayparent");
        example.appendChild(span8);

        Element a4 = document.createElement("a");
        a4.setAttribute("class", "idlink");
        a4.setAttribute("href", "#");
        a4.appendChild(document.createTextNode("#39270"));
        span8.appendChild(a4);

        Element span9 = document.createElement("span");
        span9.setAttribute("class", "supporttext");
        span9.appendChild(document.createTextNode(
                "▾ Caelar was no longer a girl. She knew the havoc wrought in her name and allowed it to go on. The " +
                        "responsibility for all that has happened these past weeks is hers and hers alone."));
        example.appendChild(span9);

        Element span10 = document.createElement("span");
        span10.setAttribute("class", "say");
        span10.appendChild(document.createTextNode("SAY "));
        example.appendChild(span10);

        Element span11 = document.createElement("span");
        span11.setAttribute("class", "idlink");
        span11.appendChild(document.createTextNode("#39271"));
        span10.appendChild(span11);

        Element span12 = document.createElement("span");
        span12.setAttribute("class", "text");
        span12.appendChild(document.createTextNode("A cold comfort. But it's all I have left to me now."));
        example.appendChild(span12);

        Element span13 = document.createElement("span");
        span13.setAttribute("class", "journalparent");
        example.appendChild(span13);

        Element a5 = document.createElement("a");
        a5.setAttribute("class", "idlink");
        a5.setAttribute("href", "#");
        a5.appendChild(document.createTextNode("#39273"));
        span13.appendChild(a5);

        Element span14 = document.createElement("span");
        span14.setAttribute("class", "supporttext");
        span14.appendChild(document.createTextNode(
                "▾ This pit has left its mark on me. I will never be free of it. But let us leave, all the same."));
        example.appendChild(span14);

        Element span15 = document.createElement("span");
        span15.setAttribute("class", "journalparent");
        example.appendChild(span15);

        Element a6 = document.createElement("a");
        a6.setAttribute("class", "idlink");
        a6.setAttribute("href", "#");
        a6.appendChild(document.createTextNode("#39276"));
        span15.appendChild(a6);

        Element span16 = document.createElement("span");
        span16.setAttribute("class", "supporttext");
        span16.appendChild(document.createTextNode("▾ It will not remain so for long."));
        example.appendChild(span16);

        Element span17 = document.createElement("span");
        span17.setAttribute("class", "journalparent");
        example.appendChild(span17);

        Element a7 = document.createElement("a");
        a7.setAttribute("class", "idlink");
        a7.setAttribute("href", "#");
        a7.appendChild(document.createTextNode("#39278"));
        span17.appendChild(a7);

        Element span18 = document.createElement("span");
        span18.setAttribute("class", "supporttext");
        span18.appendChild(document.createTextNode(
                "▾ There is no consolation in that for me. But I shall grieve later. For now, let us be done with " +
                        "this place."));
        example.appendChild(span18);

        Element span19 = document.createElement("span");
        span19.setAttribute("class", "journal");
        span19.appendChild(document.createTextNode("JOURNAL "));
        example.appendChild(span19);

        Element span20 = document.createElement("span");
        span20.setAttribute("class", "idlink");
        span20.appendChild(document.createTextNode("#59851"));
        span19.appendChild(span20);

        Element span21 = document.createElement("span");
        span21.setAttribute("class", "text");
        span21.appendChild(document.createTextNode("** No text specified in TLK file **"));
        example.appendChild(span21);

        Element span22 = document.createElement("span");
        span22.setAttribute("class", "reply");
        span22.appendChild(document.createTextNode("REPLY "));
        example.appendChild(span22);

        Element span23 = document.createElement("span");
        span23.setAttribute("class", "idlink");
        span23.appendChild(document.createTextNode("#39275"));
        span22.appendChild(span23);

        Element span24 = document.createElement("span");
        span24.setAttribute("class", "text");
        span24.appendChild(document.createTextNode(
                "BDCORWIJ: Forgive me, Master Argent, but we must leave this place. Belhifet's fallen, but the portal" +
                        " to Dragonspear is still open..."));
        example.appendChild(span24);

        Element span25 = document.createElement("span");
        span25.setAttribute("class", "replychild");
        example.appendChild(span25);

        Element a8 = document.createElement("a");
        a8.setAttribute("class", "idlink");
        a8.setAttribute("href", "#");
        a8.appendChild(document.createTextNode("#39276"));
        span25.appendChild(a8);

        Element span26 = document.createElement("span");
        span26.setAttribute("class", "supporttext");
        span26.appendChild(document.createTextNode("▸ It will not remain so for long."));
        example.appendChild(span26);


        // Add line numbers for referencing
        NodeList spans = example.getChildNodes();
        for (int i = 0; i < spans.getLength(); i = i + 3) {
            Element lineNo = document.createElement("span");
            lineNo.setAttribute("class", "lineno");
            lineNo.appendChild(document.createTextNode(String.valueOf(i / 3)));
            spans.item(i).getParentNode().insertBefore(lineNo, spans.item(i));
        }
    }

    /**
     * Writes a DOM to an HTML file in the given folder.
     *
     * @param document the DOM document
     * @param folder   the output folder
     * @throws TransformerException if an unrecoverable error occurs during the course of the transformation
     */
    private void writeToFile(Document document, Path folder) throws TransformerException {
        // Transform the DOM to an HTML file
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        Result output = new StreamResult(folder.resolve(OUTPUT_FILENAME).toFile());
        Source input = new DOMSource(document);
        transformer.transform(input, output);
    }

    /**
     * Creates the body element.
     *
     * @param document the DOM document
     * @return the body element
     */
    private Element buildBody(Document document) {
        Element result = document.createElement("body");
        result.setAttribute("onload", "init();");

        Element h1 = document.createElement("h1");
        h1.appendChild(document.createTextNode("SOD: Dialog/Journal Structure"));
        result.appendChild(h1);

        Element information1 = document.createElement("p");
        information1.appendChild(document.createTextNode(
                "Based on the decompiled TLK files of package \"SOD 00793 " + "Dialog Export May 2017.zip\"."));
        result.appendChild(information1);

        Element information2 = document.createElement("p");
        information2.appendChild(document.createTextNode("Created on: " + Instant.now()));
        result.appendChild(information2);

        Element searchDiv = document.createElement("div");
        searchDiv.setAttribute("id", "search");
        result.appendChild(searchDiv);

        Element searchInput = document.createElement("input");
        searchInput.setAttribute("id", "input");
        searchInput.setAttribute("type", "text");
        searchInput.setAttribute("placeholder", "Jump to string ID");
        searchDiv.appendChild(searchInput);

        Element h2example = document.createElement("h2");
        h2example.appendChild(document.createTextNode("Example"));
        result.appendChild(h2example);

        Element example = document.createElement("p");
        example.setAttribute("id", "example");
        example.setIdAttribute("id", true);
        result.appendChild(example);

        Element information3 = document.createElement("p");
        information3.appendChild(document.createTextNode(
                "String IDs in magenta color are clickable links that point to other connected strings in the dialog" +
                        ". You can navigate through clicked links with your browser's back and forth buttons."));
        result.appendChild(information3);

        Element information4 = document.createElement("p");
        information4.appendChild(document.createTextNode(
                "Line 5 is a string that is displayed as a spoken text directed to the player. The string ID is not " +
                        "clickable as this is the reference string for this string block."));
        information4.appendChild(document.createElement("br"));
        information4.appendChild(document.createTextNode(
                "Lines 0-4 are all preceding strings to line 5, that is all strings that line 5 is a possible answer " +
                        "to. The string IDs are clickable."));
        information4.appendChild(document.createElement("br"));
        information4.appendChild(document.createTextNode(
                "Line 9 is a succeeding journal entry string of line 5 that is being made as the result of a chosen " +
                        "answer of the player. The string ID is not clickable as this is a reference journal string " +
                        "for this string block. Unlike lines 5 or 10 this string has no succeeding string IDs."));
        information4.appendChild(document.createElement("br"));
        information4.appendChild(document.createTextNode(
                "Lines 6-8 are all preceding strings to line 9, that is all strings that line 9 is a possible result " +
                        "of. Obviously line 5 is also a preceding string to line 9. The string IDs are clickable."));
        information4.appendChild(document.createElement("br"));
        information4.appendChild(document.createTextNode(
                "Line 10 is a succeeding string of line 5 that is displayed as a possible answer choice that can be " +
                        "chosen by the player. There are no preceding string IDs to this line except line 5. The " +
                        "string ID is not clickable as this is a reference reply string for this string block."));
        information4.appendChild(document.createElement("br"));
        information4.appendChild(document.createTextNode(
                "Line 11 is the only succeeding string of line 10 that is displayed as a spoken text directed to the " +
                        "player. The string ID is clickable."));
        result.appendChild(information4);

        // For every TLK file create its dialog blocks
        for (Map.Entry<String, List<Integer>> each : filenamesToIds.entrySet()) {
            result.appendChild(buildBlocks(document, each.getKey(), each.getValue()));
        }

        return result;
    }

    /**
     * Creates a parent element for al dialog blocks that are contained in the given file. The dialog blocks start with
     * on of the given string IDs of "SAY" strings.
     *
     * @param document the DOM document
     * @param file     the filename
     * @param sayIds   the string IDs of the reference SAY strings
     * @return a div element containing all dialog blocks of the given file
     */
    private Element buildBlocks(Document document, String file, List<Integer> sayIds) {
        Element result = document.createElement("div");
        result.setAttribute("class", "blocks");

        // For every string ID of a SAY string create a dialog block
        for (int i = 0; i < sayIds.size(); i++) {
            Element h2 = document.createElement("h2");
            h2.appendChild(document.createTextNode("// File " + file + ".d"));
            result.appendChild(h2);

            result.appendChild(buildBlock(document, sayIds.get(i)));

            System.out.printf("Write HTML part %d/%d: %s%n", i + 1, sayIds.size(), file);
        }

        return result;
    }

    /**
     * Creates a dialog block for a given reference string ID of a "SAY" string.
     *
     * @param document the DOM document
     * @param sayId    the string ID of the reference SAY string
     * @return a p element containing the string IDs of the dialog block
     */
    private Element buildBlock(Document document, int sayId) {
        String sayFilename = idsToDialogs.get(sayId).getFilename();

        Element result = document.createElement("p");
        result.setAttribute("class", "block");

        // Create all parents of the reference SAY string
        for (int each : idsToDialogs.get(sayId).getParents()) {

            // Create a dummy span element for layout reasons
            Element sayParent = document.createElement("span");
            sayParent.setAttribute("class", "sayparent");
            result.appendChild(sayParent);

            // Create the link to the parent's string ID
            Element sayParentLink = document.createElement("a");
            sayParentLink.setAttribute("href", "#id" + each);
            sayParentLink.setAttribute("class", "idlink");
            sayParentLink.appendChild(document.createTextNode("#" + each));
            sayParent.appendChild(sayParentLink);

            // Create a span element with the parent's text
            Element parentText = document.createElement("span");
            parentText.setAttribute("class", "supporttext");
            result.appendChild(parentText);

            parentText.appendChild(document.createTextNode("▾ " + idsToDialogs.get(each).getText(sayFilename)));
        }

        // Create a span element with the reference string ID
        Element say = document.createElement("span");
        say.setAttribute("id", "id" + sayId);
        say.setIdAttribute("id", true);
        say.setAttribute("class", "say");
        say.appendChild(document.createTextNode("SAY "));
        result.appendChild(say);

        // Create a span element with the reference anchor ID
        Element sayLink = document.createElement("span");
        sayLink.setAttribute("class", "idlink");
        sayLink.appendChild(document.createTextNode("#" + sayId));
        say.appendChild(sayLink);

        // Create a span element with the reference text
        Element sayText = document.createElement("span");
        sayText.setAttribute("class", "text");
        sayText.appendChild(document.createTextNode(idsToDialogs.get(sayId).getText(sayFilename)));
        result.appendChild(sayText);

        // Create every child of the reference SAY string
        for (int eachChild : idsToDialogs.get(sayId).getChildren()) {

            // Output depends whether the child is a dialog or a journal entry
            switch (idsToDialogs.get(eachChild).getType()) {
                // If the child is of type DIALOG, it is a REPLY string
                case DIALOG:
                    // List all parents of the child
                    for (int eachParent : idsToDialogs.get(eachChild).getParents()) {

                        // Do not list the reference element as it was already printed
                        if (eachParent == sayId) {
                            continue;
                        }

                        // Create a dummy span element for layout reasons
                        Element replyParent = document.createElement("span");
                        replyParent.setAttribute("class", "replyparent");
                        result.appendChild(replyParent);

                        // Create the link to the parent's string ID
                        Element replyParentLink = document.createElement("a");
                        replyParentLink.setAttribute("href", "#id" + eachParent);
                        replyParentLink.setAttribute("class", "idlink");
                        replyParentLink.appendChild(document.createTextNode("#" + eachParent));
                        replyParent.appendChild(replyParentLink);

                        // Create a span element with the parent's text
                        Element replyParentText = document.createElement("span");
                        replyParentText.setAttribute("class", "supporttext");
                        replyParentText.appendChild(
                                document.createTextNode("▾ " + idsToDialogs.get(eachParent).getText(sayFilename)));
                        result.appendChild(replyParentText);
                    }

                    // Create a span element with the REPLY child string ID
                    Element reply = document.createElement("span");
                    reply.setAttribute("class", "reply");
                    reply.appendChild(document.createTextNode("REPLY "));
                    result.appendChild(reply);

                    // Create a span element with the REPLY child anchor ID
                    Element replyLink = document.createElement("span");
                    replyLink.setAttribute("id", "id" + eachChild);
                    replyLink.setAttribute("class", "idlink");
                    replyLink.appendChild(document.createTextNode("#" + eachChild));
                    reply.appendChild(replyLink);

                    // Create a span element with the reference text
                    Element replyText = document.createElement("span");
                    replyText.setAttribute("class", "text");
                    replyText.appendChild(document.createTextNode(idsToDialogs.get(eachChild).getText(sayFilename)));
                    result.appendChild(replyText);

                    // Create every child of the REPLY child string
                    for (int eachGrandchild : idsToDialogs.get(eachChild).getChildren()) {

                        // Create a dummy span element for layout reasons
                        Element replyChild = document.createElement("span");
                        replyChild.setAttribute("class", "replychild");
                        result.appendChild(replyChild);

                        // Create the link to the grandchild's string ID
                        Element replyChildLink = document.createElement("a");
                        replyChildLink.setAttribute("href", "#id" + eachGrandchild);
                        replyChildLink.setAttribute("class", "idlink");
                        replyChildLink.appendChild(document.createTextNode("#" + eachGrandchild));
                        replyChild.appendChild(replyChildLink);

                        // Create a span element with the grandchild's text
                        Element replyChildText = document.createElement("span");
                        replyChildText.setAttribute("class", "supporttext");
                        replyChildText.appendChild(
                                document.createTextNode("▸ " + idsToDialogs.get(eachGrandchild).getText(sayFilename)));
                        result.appendChild(replyChildText);
                    }

                    break;
                // If the child is of type JOURNAL, it is a JOURNAL string
                case JOURNAL:
                    // List all parents of the child
                    for (int eachParent : idsToDialogs.get(eachChild).getParents()) {
                        if (eachParent == sayId) {
                            continue;
                        }

                        // Create a dummy span element for layout reasons
                        Element journalParent = document.createElement("span");
                        journalParent.setAttribute("class", "journalparent");
                        result.appendChild(journalParent);

                        // Create the link to the parent's string ID
                        Element journalParentLink = document.createElement("a");
                        journalParentLink.setAttribute("href", "#id" + eachParent);
                        journalParentLink.setAttribute("class", "idlink");
                        journalParentLink.appendChild(document.createTextNode("#" + eachParent));
                        journalParent.appendChild(journalParentLink);

                        // Create a span element with the parent's text
                        Element journalParentText = document.createElement("span");
                        journalParentText.setAttribute("class", "supporttext");
                        journalParentText.appendChild(
                                document.createTextNode("▾ " + idsToDialogs.get(eachParent).getText(sayFilename)));
                        result.appendChild(journalParentText);
                    }

                    // Create a span element with the JOURNAL child string ID
                    Element journal = document.createElement("span");
                    journal.setAttribute("class", "journal");
                    journal.appendChild(document.createTextNode("JOURNAL "));
                    result.appendChild(journal);

                    // Create a span element with the JOURNAL child anchor ID
                    Element journalLink = document.createElement("span");
                    journalLink.setAttribute("class", "idlink");
                    journalLink.appendChild(document.createTextNode("#" + eachChild));
                    journal.appendChild(journalLink);

                    // Create a span element with the reference text
                    Element journalText = document.createElement("span");
                    journalText.setAttribute("class", "text");
                    journalText.appendChild(document.createTextNode(idsToDialogs.get(eachChild).getText(sayFilename)));
                    result.appendChild(journalText);
                    break;
                // If the child is of type ERROR, it is an erroneous string
                case ERROR:
                    Element error = document.createElement("span");
                    error.setAttribute("class", "error");
                    error.appendChild(document.createTextNode("ERROR"));
                    result.appendChild(error);
                    break;
            }
        }

        return result;
    }

    /**
     * Creates the head element.
     *
     * @param document the DOM document
     * @return the head element
     */
    private Element buildHead(Document document) {
        Element result = document.createElement("head");

        Element meta = document.createElement("meta");
        meta.setAttribute("charset", "UTF-8");
        result.appendChild(meta);

        Element title = document.createElement("title");
        title.appendChild(document.createTextNode("SOD Translation Helper"));
        result.appendChild(title);

        Element script = document.createElement("script");
        script.appendChild(document.createTextNode(
                "function init() { var input = document.getElementById('input'); input" +
                        ".addEventListener('keypress', function(e) { if (e.keyCode == 13) { window.location.href = " +
                        "'#id' + document.getElementById('input').value; } } ); }"));
        result.appendChild(script);

        Element style = document.createElement("style");
        style.setAttribute("type", "text/css");
        StringBuilder styles = new StringBuilder();
        // The font file "Latin Modern Sans 10 Regular" is embedded as a base64-encoded WOFF2 file to avoid the slow
        // fetching of a remote font file
        styles.append(
                "@font-face { font-family: 'latin_modern_roman10_regular'; src: url(data:application/font-woff2;" +
                        "charset=utf-8;base64," +
                        "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) format('woff2'); font-weight: normal; font-style: normal; }\n");
        styles.append("html { font-family: 'latin_modern_roman10_regular'; background-color: rgb(39, 40, 34); " +
                "color: rgb" + "(255, 255, 255); }\n");
        styles.append("h2 { font-size: 1em; }\n");
        styles.append("p.block { background-color: rgba(255, 255, 255, 0.025); padding: 0.4em; border-radius: " +
                "0.1em; display: grid; grid-template-columns: auto 1fr; grid-gap: 0.1em 0.6em; overflow: hidden; " +
                "white-space: nowrap; }\n");
        styles.append("span.say { color: rgb(174, 129, 255); display: inline-block; padding-bottom: 0.5em;}\n");
        styles.append("span.text { color: rgb(142, 137, 113); display: inline-block; }\n");
        styles.append("span.supporttext { color: rgb(99, 95, 79); display: inline-block; }\n");
        styles.append("span.reply, span.journal { color: rgb(174, 129, 255); display: inline-block; padding-left: " +
                "2em; }\n");
        styles.append("span.idlink { color: rgb(230, 219, 116); text-decoration: none; }\n");
        styles.append("a.idlink { color: rgb(249, 38, 114); text-decoration: none; }\n");
        styles.append("a.idlink:hover { text-decoration: underline; }\n");
        styles.append("span.sayparent { display: inline-block; }\n");
        styles.append("span.replychild { display: inline-block; padding-left: 5.4em; }\n");
        styles.append("span.replyParent, span.journalparent { display: inline-block; padding-left: 2em; }\n");
        styles.append("span.replychild + span.supporttext + span.reply, span.replychild + span.supporttext + span" +
                ".reply + span.text, span.replychild + span.supporttext + span.replyparent, span.replychild + span" +
                ".supporttext + span.replyparent + span.supporttext, span.journal + span.text + span" +
                ".replyparent, span.journal + span.text + span.replyparent + span.supporttext, span.reply + span.text" +
                " + span.journal, span.reply + span.text + span.journal + span.text, span.journal + span.text + span" +
                ".reply, span.journal + span.text + span.reply + span.text, span.reply + span.text + span.reply, span" +
                ".reply + span.text + span.reply + span.text, span.reply + span.text + span.replyparent, span.reply +" +
                " span.text + span.replyparent + span.supporttext { padding-top: 0.5em; }\n");
        styles.append("p#example { background-color: rgba(255, 255, 255, 0.025); padding: 0.4em; border-radius: " +
                "0.1em; display: grid; grid-template-columns: auto auto 1fr; grid-gap: 0.1em 0.6em; overflow: " +
                "hidden; white-space: nowrap; }\n");
        styles.append(
                "p#example span.reply, p#example span.reply + span.text, p#example span.journal + span.text + span" +
                        ".lineno { padding-top: 0.5em; }\n");
        styles.append("div#search { position: fixed; left: 1em; bottom: 1em; width: 10em; }\n");
        styles.append(
                "input#input { background-color: rgb(39, 40, 34); color: rgb(142, 137, 113); border: 1px solid rgb" +
                        "(99, 95, 79); border-radius: 0.2em; font-family: 'latin_modern_roman10_regular'; font-size: " +
                        "1em; box-shadow: 0 0 0.8em 0.4em rgb(39, 40, 34, 0.75); padding: 0.1em; }");
        style.appendChild(document.createTextNode(styles.toString()));
        result.appendChild(style);

        return result;
    }
}
