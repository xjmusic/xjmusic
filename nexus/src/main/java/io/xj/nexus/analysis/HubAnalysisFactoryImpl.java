// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.analysis;

import io.xj.hub.HubContent;
import io.xj.nexus.hub_client.access.HubAccess;
import org.springframework.stereotype.Service;

/**
 * Template content Analysis https://www.pivotaltracker.com/story/show/161199945
 */
@Service
class HubAnalysisFactoryImpl implements HubAnalysisFactory {

  public HubAnalysisFactoryImpl() {
  }

  /**
   * Get the comp for the given type
   *
   * @param type of comp
   * @return comp
   */
  public Report report(HubAccess access, HubContent content, Report.Type type) throws HubAnalysisException {
    try {
      return switch (type) {
        case ChordInstruments -> new ReportChordInstruments(content);
        case Constellations -> new ReportConstellations(content);
        case Events -> new ReportEvents(content);
        case MainProgramChords -> new ReportMainProgramChords(content);
        case Memes -> new ReportMemes(content);
      };

    } catch (Exception e) {
      throw new HubAnalysisException("Failed to build content index", e);
    }
  }
}
