// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.analysis;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.hub.access.HubAccess;
import io.xj.hub.client.HubClientException;
import io.xj.hub.client.HubContent;
import io.xj.hub.ingest.HubIngestException;
import io.xj.hub.ingest.HubIngestFactory;
import io.xj.lib.app.Environment;
import io.xj.lib.util.Values;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.xj.hub.analysis.Report.H1;
import static io.xj.hub.analysis.Report.H2;

/**
 Template content Analysis #161199945
 */
class HubAnalysisImpl implements HubAnalysis {
  private static final String REPORT_HTML_RESOURCE_PATH = "analysis/report.html";
  private static final String REPORT_HTML_PLACEHOLDER_TITLE = "{{title}}";
  private static final String REPORT_HTML_PLACEHOLDER_BODY = "{{body}}";
  private static final String title = "Content Analysis";
  private final Environment env;
  private final HubAccess access;
  private final Collection<Report.Type> compTypes;
  private final HubContent content;

  @Inject
  public HubAnalysisImpl(
    Environment env,
    @Assisted("access") HubAccess access,
    @Assisted("templateId") UUID templateId,
    @Assisted("analyze") Collection<Report.Type> compTypes,
    HubIngestFactory hubIngestFactory
  ) throws HubAnalysisException {
    this.env = env;
    this.access = access;
    this.compTypes = compTypes;
    try {
      content = new HubContent(hubIngestFactory.ingest(access, templateId).getAllEntities());

    } catch (HubIngestException e) {
      throw new HubAnalysisException("Failed to ingest content", e);
    } catch (HubClientException e) {
      throw new HubAnalysisException("Failed to build content index", e);
    }
  }

  @Override
  public HubAccess getAccess() {
    return access;
  }

  @Override
  public String toHTML() throws HubAnalysisException {
    try {
      return
        new String(new BufferedInputStream(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(REPORT_HTML_RESOURCE_PATH))).readAllBytes())
          .replace(REPORT_HTML_PLACEHOLDER_TITLE, title)
          .replace(REPORT_HTML_PLACEHOLDER_BODY, String.format("%s\n%s", renderHeaderHTML(), renderMainHTML()));
    } catch (IOException e) {
      throw new HubAnalysisException("Failed to get report.html template!", e);
    } catch (HubClientException e) {
      throw new HubAnalysisException("Failed to analyze content!", e);
    }
  }

  private String renderHeaderHTML() throws HubClientException {
    return String.join("\n", H1(content.getTemplate().getName(), "headline"), renderTimestampHTML(), renderTocHTML());
  }

  private String renderTocHTML() {
    return String.format("<ul>%s</ul>",
      compTypes.stream()
        .sorted()
        .map(this::getComp)
        .map(c -> String.format("<li><a href=\"#%s\">%s</a></li>", c.getType().toString(), c.getType().getName()))
        .collect(Collectors.joining("\n")));
  }

  private String renderTimestampHTML() {
    return String.format("<div class=\"timestamp\">Analyzed %s</div>", Values.formatRfc1123UTC(Instant.now()));
  }

  private String renderMainHTML() {
    if (compTypes.isEmpty()) return "<em>No analysis mode(s) specified!</em>";
    return compTypes.stream()
      .sorted()
      .map(this::getComp)
      .map(this::renderCompHTML)
      .collect(Collectors.joining("\n"));
  }

  private String renderCompHTML(Report analyzer) {
    return String.join("\n", H2(analyzer.getType().getName(), analyzer.getType().toString()), analyzer.toHTML());
  }

  /**
   Get the comp for the given type

   @param type of comp
   @return comp
   */
  private Report getComp(Report.Type type) {
    return switch (type) {
      case Memes -> new AnalyzeMemes(content, env);
      case MainProgramChords -> new AnalyzeMainProgramChords(content, env);
      case DrumInstrumentEvents -> new AnalyzeDrumInstrumentEvents(content, env);
    };
  }
}
