// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.analysis;

import io.xj.hub.access.HubAccess;
import io.xj.hub.client.HubClientException;
import io.xj.hub.client.HubContent;
import io.xj.hub.ingest.HubIngestException;
import io.xj.hub.ingest.HubIngestFactory;
import io.xj.lib.util.ValueException;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Template content Analysis https://www.pivotaltracker.com/story/show/161199945
 */
@Service
class HubAnalysisFactoryImpl implements HubAnalysisFactory {
  private final HubIngestFactory hubIngestFactory;

  public HubAnalysisFactoryImpl(HubIngestFactory hubIngestFactory) {
    this.hubIngestFactory = hubIngestFactory;
  }

  /**
   * Get the comp for the given type
   *
   * @param type of comp
   * @return comp
   */
  public Report report(HubAccess access, UUID templateId, Report.Type type) throws HubAnalysisException {
    try {
      var content = new HubContent(hubIngestFactory.ingest(access, templateId).getAllEntities());

      return switch (type) {
        case ChordInstruments -> new ReportChordInstruments(content);
        case Constellations -> new ReportConstellations(content);
        case Events -> new ReportEvents(content);
        case MainProgramChords -> new ReportMainProgramChords(content);
        case Memes -> new ReportMemes(content);
      };

    } catch (HubIngestException e) {
      throw new HubAnalysisException("Failed to ingest content", e);
    } catch (HubClientException e) {
      throw new HubAnalysisException("Failed to build content index", e);
    } catch (ValueException e) {
      throw new HubAnalysisException("Failed to parse value", e);
    }
  }
}
