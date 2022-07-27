// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.analysis;

import io.xj.hub.client.HubClientException;
import io.xj.hub.client.HubContent;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.Program;
import io.xj.lib.app.Environment;
import io.xj.lib.util.Values;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Objects;

/**
 Template content Analysis https://www.pivotaltracker.com/story/show/161199945
 */
public abstract class Report {
  private static final String REPORT_HTML_RESOURCE_PATH = "analysis/report.html";
  private static final String REPORT_HTML_PLACEHOLDER_TITLE = "{{title}}";
  private static final String REPORT_HTML_PLACEHOLDER_BODY = "{{body}}";
  private static final String title = "Content Analysis";
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
   Render HTML of timestamp
   */
  public static String renderTimestampHTML() {
    return String.format("<div class=\"timestamp\">Analyzed %s</div>", Values.formatRfc1123UTC(Instant.now()));
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
   Render HTML
   */
  public String renderHeaderHTML() throws HubClientException {
    return String.join("\n", H1(getType().getName(), "headline"), renderTimestampHTML());
  }

  /**
   Output this computation as HTML

   @return HTML representation
   */
  public String toHTML() throws HubAnalysisException {
    try {
      return
        new String(new BufferedInputStream(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(REPORT_HTML_RESOURCE_PATH))).readAllBytes())
          .replace(REPORT_HTML_PLACEHOLDER_TITLE, title)
          .replace(REPORT_HTML_PLACEHOLDER_BODY, String.format("%s\n%s", renderHeaderHTML(), renderContentHTML()));
    } catch (
      IOException e) {
      throw new HubAnalysisException("Failed to get report.html template!", e);
    } catch (HubClientException e) {
      throw new HubAnalysisException("Failed to analyze content!", e);
    }
  }

  /**
   Each type of report replaces this function with its own inner content

   @return content HTML
   */
  public abstract String renderContentHTML();

  /**
   Get the type of this comp

   @return comp type
   */
  public abstract Type getType();

  /**
   Render an HTML link to a program

   @param program to link
   @return link to program
   */
  protected String programRef(Program program) {
    return String.format("<A STYLE=\"%s\" HREF=\"%s%s/%s\" TARGET=\"_blank\" REL=\"noopener noreferrer\">%s</A>",
      USAGE_LINK_STYLE, env.getAppBaseUrl(), "programs", program.getId(), program.getName());
  }

  /**
   Render an HTML link to a instrument

   @param instrument to link
   @return link to instrument
   */
  protected String instrumentRef(Instrument instrument) {
    return String.format("<A STYLE=\"%s\" HREF=\"%s%s/%s\" TARGET=\"_blank\" REL=\"noopener noreferrer\">%s</A>",
      USAGE_LINK_STYLE, env.getAppBaseUrl(), "instruments", instrument.getId(), instrument.getName());
  }

  /**
   Types of comps
   */
  public enum Type {
    Events("Events"),
    MainProgramChords("Main Chords"),
    Memes("Memes");

    private final String name;

    Type(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }
}
