// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.analysis;

import io.xj.hub.client.HubContent;
import io.xj.lib.app.Environment;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 Template content Analysis #161199945
 */
public abstract class Report {
    private static final String CELL_STYLE = "vertical-align:top; padding-right:5px; padding-bottom:5px;";
    private static final String ROW_STYLE = "border-top:2px solid #333;";
    private static final String TABLE_STYLE = "";
    private static final String H1_STYLE = "margin-top:10px;";
    private static final String H2_STYLE = "margin-top:20px;";
    private static final String USAGE_LINK_STYLE = "font-size:8pt; line-height:9pt; margin-right:4pt; white-space:nowrap;";
    protected final HubContent content;
    protected final Environment env;

    /**
     Create a comp from some Hub content
     */
    public Report(HubContent content, Environment env) {
        this.content = content;
        this.env = env;
    }

    /**
     Render an HTML h1 tag with style

     @param content inside h1 tag
     @param id      of tag
     @return h1 tag with style and content
     */
    public static String H1(String content, String id) {
        return String.format("<H1 ID=\"%s\" STYLE=\"%s\">%s</H1>", id, H1_STYLE, content);
    }

    /**
     Render an HTML h2 tag with style

     @param content inside h2 tag
     @param id      of tag
     @return h2 tag with style and content
     */
    public static String H2(String content, String id) {
        return String.format("<H2 ID=\"%s\" STYLE=\"%s\">%s</H2>", id, H2_STYLE, content);
    }

    /**
     Render an HTML table tag with style

     @param contents inside table tag
     @return table tag with style and content
     */
    public static String TABLE(String... contents) {
        return String.format("<TABLE STYLE=\"%s\">%s</TABLE>", TABLE_STYLE, String.join("\n", contents));
    }

    /**
     Render an HTML row tag with style

     @param contents inside row tag
     @return row tag with style and content
     */
    public static String TR(String... contents) {
        return String.format("<TR STYLE=\"%s\">%s</TR>", ROW_STYLE, String.join("\n", contents));
    }

    /**
     Render an HTML cell tag with style

     @param contents inside cell tag
     @return cell tag with style and content
     */
    public static String TD(String... contents) {
        return String.format("<TD STYLE=\"%s\">%s</TD>", CELL_STYLE, String.join("\n", contents));
    }

    /**
     Output this computation as HTML

     @return HTML representation
     */
    abstract String toHTML();

    /**
     Get the type of this comp

     @return comp type
     */
    abstract Type getType();

    /**
     Render an HTML link to a program

     @param programId to link
     @return link to program
     */
    protected String programRef(UUID programId) {
        return String.format("<A STYLE=\"%s\" HREF=\"%s%s/%s\" TARGET=\"_blank\" REL=\"noopener noreferrer\">%s</A>",
                USAGE_LINK_STYLE, env.getAppBaseUrl(), "programs", programId, content.getProgram(programId).orElseThrow().getName());
    }

    /**
     Render an HTML link to a instrument

     @param instrumentId to link
     @return link to instrument
     */
    protected String instrumentRef(UUID instrumentId) {
        return String.format("<A STYLE=\"%s\" HREF=\"%s%s/%s\" TARGET=\"_blank\" REL=\"noopener noreferrer\">%s</A>",
                USAGE_LINK_STYLE, env.getAppBaseUrl(), "instruments", instrumentId, content.getInstrument(instrumentId).orElseThrow().getName());
    }

    /**
     Types of comps
     */
    public enum Type {
        DrumInstrumentEvents("Drum Events"),
        MainProgramChords("Main Chords"),
        Memes("Memes");

        private final String name;

        Type(String name) {
            this.name = name;
        }

        public static Collection<Type> fromValues(Collection<String> values) {
            return values.stream().map(Type::valueOf).collect(Collectors.toSet());
        }

        public String getName() {
            return name;
        }
    }
}
