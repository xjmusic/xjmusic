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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 Template content Analysis https://www.pivotaltracker.com/story/show/161199945
 */
public abstract class Report {
  private static final String TOP_ID = "top";
  private static final String SCROLL_TO_TOP_MESSAGE = "scroll to top";
  private static final String REPORT_HTML_RESOURCE_PATH = "analysis/report.html";
  private static final String REPORT_HTML_PLACEHOLDER_TITLE = "{{title}}";
  private static final String REPORT_HTML_PLACEHOLDER_BODY = "{{body}}";
  private static final String title = "Content Analysis";
  private static final String CELL_CHECKBOX_TRUE_STYLE = "background-color:#141; color:#2f2; font-weight:900;";
  private static final String CELL_STYLE = "vertical-align:top; padding-right:5px; padding-bottom:5px; padding-left:5px; border-left:2px solid #555; border-right:2px solid #555;";
  private static final String UNORDERED_LIST_STYLE = "";
  private static final String LIST_ITEM_STYLE = "";
  private static final String P_STYLE = "margin-top:5px;";
  private static final String ROW_STYLE = "border-bottom:2px solid #666;";
  private static final String ROW_HEADER_STYLE = "background-color: #666;";
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

   @param header   whether to make this a header row
   @param contents inside row tag
   @return row tag with style and content
   */
  public static String TR(Boolean header, String... contents) {
    return String.format("<TR STYLE=\"%s\">%s</TR>", header ? ROW_HEADER_STYLE : ROW_STYLE, String.join("\n", contents));
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
   Render an HTML cell tag with checkbox style

   @param value whether this cell is checked true (green checkbox)
   @return cell tag with style and content
   */
  public static String checkboxTD(Boolean value) {
    if (value)
      return String.format("<TD STYLE=\"%s\">%s</TD>", CELL_CHECKBOX_TRUE_STYLE, "✓");
    return String.format("<TD STYLE=\"%s\">%s</TD>", CELL_STYLE, "");
  }

  /**
   Render an HTML unordered list tag with style

   @param contents inside unordered list tag
   @return unordered list tag with style and content
   */
  public static String UL(String... contents) {
    return String.format("<UL STYLE=\"%s\">%s</UL>", UNORDERED_LIST_STYLE, String.join("\n", contents));
  }

  /**
   Render an HTML list item tag with style

   @param contents inside list item tag
   @return list item tag with style and content
   */
  public static String LI(String... contents) {
    return String.format("<LI STYLE=\"%s\">%s</LI>", LIST_ITEM_STYLE, String.join("\n", contents));
  }

  /**
   Render an HTML paragraph tag with style

   @param content of paragraph
   @return paragraph tag with style and content
   */
  public static String P(String content) {
    return String.format("<P STYLE=\"%s\">%s</P>", P_STYLE, content);
  }

  /**
   Render an HTML link

   @param href    to link
   @param content to display
   @return link to program
   */
  protected static String A(Boolean launchBlank, String href, String content) {
    return String.format("<A STYLE=\"%s\" HREF=\"%s\" %s REL=\"noopener noreferrer\">%s</A>", USAGE_LINK_STYLE, href, launchBlank ? "TARGET=\"_blank\"" : "", content);
  }

  /**
   Render HTML
   */
  public String renderHeaderHTML() throws HubClientException {
    return String.join(String.format("<A ID=\"%s\">", TOP_ID), "\n", H1(getType().getName(), "headline"), renderTimestampHTML());
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
          .replace(REPORT_HTML_PLACEHOLDER_BODY, String.format("%s\n%s", renderHeaderHTML(), sectionsToHTML(computeSections())));
    } catch (
      IOException e) {
      throw new HubAnalysisException("Failed to get report.html template!", e);
    } catch (HubClientException e) {
      throw new HubAnalysisException("Failed to analyze content!", e);
    }
  }

  /**
   Render organized sections

   @param sections to render
   @return rendered sections prefaced by linked table of contents
   */
  protected String sectionsToHTML(List<ReportSection> sections) {
    return
      UL(sections.stream()
        .filter(ReportSection::notEmpty)
        .map(s -> LI(s.renderRef()))
        .collect(Collectors.joining())) +
        sections.stream()
          .filter(ReportSection::notEmpty)
          .map(ReportSection::render)
          .collect(Collectors.joining());
  }

  /**
   Each type of report replaces this function with its own inner content

   @return content HTML
   */
  public abstract List<ReportSection> computeSections();

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
    return A(true, String.format("%s%s/%s", env.getAppBaseUrl(), "programs", program.getId()), program.getName());
  }

  /**
   Render an HTML link to a instrument

   @param instrument to link
   @return link to instrument
   */
  protected String instrumentRef(Instrument instrument) {
    return A(true, String.format("%s%s/%s", env.getAppBaseUrl(), "instruments", instrument.getId()), instrument.getName());
  }

  /**
   Types of comps
   */
  public enum Type {
    Events("Events"),
    MainProgramChords("Main Chords"),
    Memes("Memes"),
    Constellations("Constellations");

    private final String name;

    Type(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }

  /**
   Organized sections of content

   @param id                   of section
   @param name                 of section
   @param columnHeaderContents columns
   @param rowCellContents      row content
   */
  protected record ReportSection(String id, String name,
                                 List<String> columnHeaderContents,
                                 List<List<String>> rowCellContents) {
    public static final String CELL_CHECKBOX_TRUE_VALUE = "true";
    public static final String CELL_CHECKBOX_FALSE_VALUE = "false";

    public static String checkboxValue(Boolean value) {
      return value ? CELL_CHECKBOX_TRUE_VALUE : CELL_CHECKBOX_FALSE_VALUE;
    }

    public static ReportSection empty() {
      return new ReportSection(null, null, List.of(), List.of());
    }

    public String render() {
      return "<br/><br/><br/>" + H2(name, id) + A(false, String.format("#%s", TOP_ID), SCROLL_TO_TOP_MESSAGE) +
        TABLE(TR(true, columnHeaderContents.stream().map(String::valueOf).map(Report::TD).collect(Collectors.joining())),
          rowCellContents.stream()
            .map(row -> TR(false,
              row.stream()
                .map(v -> {
                  if (Objects.equals(CELL_CHECKBOX_TRUE_VALUE, v)) return checkboxTD(true);
                  else if (Objects.equals(CELL_CHECKBOX_FALSE_VALUE, v)) return checkboxTD(false);
                  else return TD(String.valueOf(v));
                })
                .collect(Collectors.joining())))
            .collect(Collectors.joining()));
    }

    boolean notEmpty() {
      return !rowCellContents.isEmpty();
    }

    public String renderRef() {
      return A(false, String.format("#%s", id), name);
    }
  }

}
