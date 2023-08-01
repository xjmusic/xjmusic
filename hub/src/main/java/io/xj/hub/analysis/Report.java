// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.analysis;

import com.google.common.collect.Maps;
import com.google.api.client.util.Sets;
import com.google.common.collect.Streams;
import io.xj.hub.client.HubClientException;
import io.xj.hub.client.HubContent;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.Program;
import io.xj.lib.entity.Entities;
import io.xj.lib.meme.MemeConstellation;
import io.xj.lib.meme.MemeStack;
import io.xj.lib.meme.MemeTaxonomy;
import io.xj.lib.util.Values;
import org.springframework.beans.factory.annotation.Value;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Template content Analysis https://www.pivotaltracker.com/story/show/161199945
 */
@SuppressWarnings("DuplicatedCode")
public abstract class Report {
  static final String TOP_ID = "top";
  static final String SCROLL_TO_TOP_MESSAGE = "scroll to top";
  static final String REPORT_HTML_RESOURCE_PATH = "analysis/report.html";
  static final String REPORT_HTML_PLACEHOLDER_TITLE = "{{title}}";
  static final String REPORT_HTML_PLACEHOLDER_BODY = "{{body}}";
  static final String title = "Content Analysis";
  static final String CELL_STATUS_GREEN_STYLE = "background-color:#141; color:#2f2; font-weight:900;";
  static final String CELL_STATUS_YELLOW_STYLE = "background-color:#441; color:#ff2; font-weight:900;";
  static final String CELL_STATUS_RED_STYLE = "background-color:#411; color:#f22; font-weight:900;";
  static final String CELL_STYLE = "vertical-align:top; padding-right:5px; padding-bottom:5px; padding-left:5px; border-left:2px solid #555; border-right:2px solid #555;";
  static final String CELL_HEADER_STYLE = String.format("%s %s", CELL_STYLE, "background-color: #666;");
  static final String UNORDERED_LIST_STYLE = "";
  static final String LIST_ITEM_STYLE = "";
  static final String P_STYLE = "margin-top:5px;";
  static final String ROW_STYLE = "border-bottom:2px solid #666;";
  static final String TABLE_STYLE = "";
  static final String H1_STYLE = "margin-top:10px;";
  static final String H2_STYLE = "margin-top:20px;";
  static final String USAGE_LINK_STYLE = "font-size:8pt; line-height:9pt; margin-right:4pt; white-space:nowrap;";
  protected final HubContent content;
  @Value("${app.base.url}")
  String appBaseUrl;

  /**
   * Create a comp from some Hub content
   */
  public Report(HubContent content) {
    this.content = content;
  }

  /**
   * Representation of the construction of a histogram of usage of all constellations
   */
  protected static class Histogram {
    Map<String, Count> histogram;

    public Histogram() {
      histogram = Maps.newHashMap();
    }

    public void addId(String key, UUID id) {
      if (!histogram.containsKey(key)) histogram.put(key, new Count());
      histogram.get(key).addId(id);
    }

    public Collection<UUID> getIds(String key) {
      if (histogram.containsKey(key)) return histogram.get(key).ids;
      return List.of();
    }

    protected static class Count {
      Set<UUID> ids;
      Integer total;

      public Count() {
        total = 0;
        ids = Sets.newHashSet();
      }

      public void addId(UUID programId) {
        ids.add(programId);
        total++;
      }
    }
  }

  /**
   * Compute the macro histogram for a template
   *
   * @param taxonomy of template
   * @return macro histogram for template
   */
  protected Histogram computeMacroHistogram(MemeTaxonomy taxonomy) {
    Collection<String> macroMemeNames;
    Collection<String> macroBindingMemeNames;
    var macroHistogram = new ReportChordInstruments.Histogram();
    for (var macroProgram : content.getPrograms(ProgramType.Macro)) {
      macroMemeNames = Entities.namesOf(content.getProgramMemes(macroProgram.getId()));
      for (var macroBinding : content.getSequenceBindingsForProgram(macroProgram.getId())) {
        macroBindingMemeNames = Entities.namesOf(content.getMemesForSequenceBinding(macroBinding.getId()));
        var memeNames = Streams.concat(macroMemeNames.parallelStream(), macroBindingMemeNames.parallelStream()).collect(Collectors.toSet());
        macroHistogram.addId(MemeStack.from(taxonomy, memeNames).getConstellation(), macroProgram.getId());
      }
    }
    return macroHistogram;
  }

  /**
   * Compute the main histogram for a template
   *
   * @param taxonomy       of template
   * @param macroHistogram computed for template
   * @return main histogram for template
   */
  protected Histogram computeMainHistogram(MemeTaxonomy taxonomy, Histogram macroHistogram) {
    Collection<String> mainBindingMemeNames;
    var mainHistogram = new ReportChordInstruments.Histogram();
    for (var macroConstellation : macroHistogram.histogram.keySet()) {
      var stack = MemeStack.from(taxonomy, MemeConstellation.toNames(macroConstellation));
      for (var mainProgram : content.getPrograms(ProgramType.Main)) {
        var mainMemes = Entities.namesOf(content.getProgramMemes(mainProgram.getId()));
        if (stack.isAllowed(mainMemes))
          for (var mainBinding : content.getSequenceBindingsForProgram(mainProgram.getId())) {
            mainBindingMemeNames = Entities.namesOf(content.getMemesForSequenceBinding(mainBinding.getId()));
            var memeNames = Streams.concat(mainMemes.parallelStream(), mainBindingMemeNames.parallelStream()).collect(Collectors.toSet());
            mainHistogram.addId(MemeStack.from(taxonomy, memeNames).getConstellation(), mainProgram.getId());
          }
      }
    }
    return mainHistogram;
  }

  /**
   * Render HTML of timestamp
   */
  public static String renderTimestampHTML() {
    return String.format("<div class=\"timestamp\">Analyzed %s</div>", Values.formatRfc1123UTC(Instant.now()));
  }

  /**
   * Render an HTML h1 tag with style
   *
   * @param content inside h1 tag
   * @param id      of tag
   * @return h1 tag with style and content
   */
  public static String H1(String content, String id) {
    return String.format("<H1 ID=\"%s\" STYLE=\"%s\">%s</H1>", id, H1_STYLE, content);
  }

  /**
   * Render an HTML h2 tag with style
   *
   * @param content inside h2 tag
   * @param id      of tag
   * @return h2 tag with style and content
   */
  public static String H2(String content, String id) {
    return String.format("<H2 ID=\"%s\" STYLE=\"%s\">%s</H2>", id, H2_STYLE, content);
  }

  /**
   * Render an HTML table tag with style
   *
   * @param contents inside table tag
   * @return table tag with style and content
   */
  public static String TABLE(String... contents) {
    return String.format("<TABLE STYLE=\"%s\">%s</TABLE>", TABLE_STYLE, String.join("\n", contents));
  }

  /**
   * Render an HTML row tag with style
   *
   * @param contents inside row tag
   * @return row tag with style and content
   */
  public static String TR(String... contents) {
    return String.format("<TR STYLE=\"%s\">%s</TR>", ROW_STYLE, String.join("\n", contents));
  }

  /**
   * Render an HTML cell tag with optional header style
   *
   * @param header   whether to make this a header cell
   * @param contents inside cell tag
   * @return cell tag with style and content
   */
  public static String TD(Boolean header, String... contents) {
    return String.format("<TD STYLE=\"%s\">%s</TD>", header ? CELL_HEADER_STYLE : CELL_STYLE, String.join("\n", contents));
  }

  /**
   * Render an HTML cell tag with style
   *
   * @param contents inside cell tag
   * @return cell tag with style and content
   */
  public static String TD(String... contents) {
    return TD(false, contents);
  }

  /**
   * Render an HTML cell tag with header style
   *
   * @param contents inside cell tag
   * @return cell tag with style and content
   */
  public static String headerTD(String... contents) {
    return TD(true, contents);
  }

  public enum CellStatus {
    NA,
    GREEN,
    YELLOW,
    RED
  }

  public enum CellSpecialValue {
    TRUE,
    FALSE,
    RED,
    GREEN,
    YELLOW
  }

  /**
   * Render an HTML cell tag with checkbox style
   *
   * @param value whether this cell is checked true (green checkbox)
   * @return cell tag with style and content
   */
  public static String checkboxTD(Boolean value) {
    return statusTD(value ? CellStatus.GREEN : CellStatus.NA);
  }

  /**
   * Render an HTML cell tag with checkbox style
   *
   * @param status of this cell
   * @return cell tag with style and content
   */
  public static String statusTD(CellStatus status) {
    return switch (status) {
      case NA -> String.format("<TD STYLE=\"%s\">%s</TD>", CELL_STYLE, "");
      case GREEN -> String.format("<TD STYLE=\"%s\">%s</TD>", CELL_STATUS_GREEN_STYLE, "✓");
      case YELLOW -> String.format("<TD STYLE=\"%s\">%s</TD>", CELL_STATUS_YELLOW_STYLE, "?");
      case RED -> String.format("<TD STYLE=\"%s\">%s</TD>", CELL_STATUS_RED_STYLE, "X");
    };
  }

  /**
   * Render an HTML unordered list tag with style
   *
   * @param contents inside unordered list tag
   * @return unordered list tag with style and content
   */
  public static String UL(String... contents) {
    return String.format("<UL STYLE=\"%s\">%s</UL>", UNORDERED_LIST_STYLE, String.join("\n", contents));
  }

  /**
   * Render an HTML list item tag with style
   *
   * @param contents inside list item tag
   * @return list item tag with style and content
   */
  public static String LI(String... contents) {
    return String.format("<LI STYLE=\"%s\">%s</LI>", LIST_ITEM_STYLE, String.join("\n", contents));
  }

  /**
   * Render an HTML paragraph tag with style
   *
   * @param content of paragraph
   * @return paragraph tag with style and content
   */
  public static String P(String content) {
    return String.format("<P STYLE=\"%s\">%s</P>", P_STYLE, content);
  }

  /**
   * Render an HTML link
   *
   * @param href    to link
   * @param content to display
   * @return link to program
   */
  protected static String A(Boolean launchBlank, String href, String content) {
    return String.format("<A STYLE=\"%s\" HREF=\"%s\" %s REL=\"noopener noreferrer\">%s</A>", USAGE_LINK_STYLE, href, launchBlank ? "TARGET=\"_blank\"" : "", content);
  }

  /**
   * Render HTML
   */
  public String renderHeaderHTML() throws HubClientException {
    return String.join(String.format("<A ID=\"%s\">", TOP_ID), "\n", H1(getType().getName(), "headline"), renderTimestampHTML());
  }

  /**
   * Output this computation as HTML
   *
   * @return HTML representation
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
   * Render organized sections
   *
   * @param sections to render
   * @return rendered sections prefaced by linked table of contents
   */
  protected String sectionsToHTML(List<Section> sections) {
    return
      UL(sections.parallelStream()
        .filter(Section::notEmpty)
        .map(s -> LI(s.renderRef()))
        .collect(Collectors.joining())) +
        sections.parallelStream()
          .filter(Section::notEmpty)
          .map(Section::render)
          .collect(Collectors.joining());
  }

  /**
   * Each type of report replaces this function with its own inner content
   *
   * @return content HTML
   */
  public abstract List<Section> computeSections();

  /**
   * Get the type of this comp
   *
   * @return comp type
   */
  public abstract Type getType();

  /**
   * Render an HTML link to a program
   *
   * @param program to link
   * @return link to program
   */
  protected String programRef(Program program) {
    return A(true, String.format("%s%s/%s", appBaseUrl, "programs", program.getId()), program.getName());
  }

  /**
   * Render an HTML link to a instrument
   *
   * @param instrument to link
   * @return link to instrument
   */
  protected String instrumentRef(Instrument instrument) {
    return A(true, String.format("%s%s/%s", appBaseUrl, "instruments", instrument.getId()), instrument.getName());
  }

  /**
   * Types of comps
   */
  public enum Type {
    ChordInstruments("Chord-mode Instruments"),
    Constellations("Constellations"),
    Events("Events"),
    MainProgramChords("Main Chords"),
    Memes("Memes");

    final String name;

    Type(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }

  /**
   * Organized sections of content@param id                   of section
   *
   * @param name          of section
   * @param rowColCells   row content
   * @param columnHeaders headers for columns
   * @param rowHeaders    headers for rows
   */
  protected record Section(String id, String name,
                           List<List<String>> rowColCells,
                           List<String> columnHeaders,
                           List<String> rowHeaders) {
    public static String checkboxValue(Boolean value) {
      return value ? CellSpecialValue.TRUE.toString() : CellSpecialValue.FALSE.toString();
    }

    public static Section empty() {
      return new Section(null, null, List.of(), List.of(), List.of());
    }

    public String render() {
      String[] rows = new String[rowColCells.size()];
      for (int i = 0; i < rowColCells.size(); i++)
        rows[i] = TR(
          rowHeaders.isEmpty() ? "" : headerTD(i < rowHeaders.size() ? rowHeaders.get(i) : ""),
          rowColCells.get(i).parallelStream()
            .map(v -> {
              if (CellSpecialValue.TRUE.toString().equals(v)) return checkboxTD(true);
              else if (CellSpecialValue.FALSE.toString().equals(v)) return checkboxTD(false);
              else if (CellSpecialValue.RED.toString().equals(v)) return statusTD(CellStatus.RED);
              else if (CellSpecialValue.GREEN.toString().equals(v)) return statusTD(CellStatus.GREEN);
              else if (CellSpecialValue.YELLOW.toString().equals(v)) return statusTD(CellStatus.YELLOW);
              else return TD(String.valueOf(v));
            })
            .collect(Collectors.joining()));

      return "<br/><br/><br/>" + H2(name, id) + A(false, String.format("#%s", TOP_ID), SCROLL_TO_TOP_MESSAGE) +
        TABLE(
          TR(rowHeaders.isEmpty() ? "" : headerTD(""), columnHeaders.parallelStream().map(String::valueOf).map(Report::headerTD).collect(Collectors.joining())),
          String.join("", rows));
    }

    boolean notEmpty() {
      return !rowColCells.isEmpty();
    }

    public String renderRef() {
      return A(false, String.format("#%s", id), name);
    }
  }

}
