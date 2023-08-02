// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.analysis;


import io.xj.hub.access.HubAccess;

import java.util.UUID;

/**
 * Template content Analysis https://www.pivotaltracker.com/story/show/161199945
 */
@FunctionalInterface
public interface HubAnalysisFactory {

  /**
   * Template content Analysis https://www.pivotaltracker.com/story/show/161199945
   *
   * @param access control
   * @param type   type of report to generate
   * @return entities to be evaluated
   * @throws HubAnalysisException on failure to of target entities
   */
  Report report(
    HubAccess access,
    UUID templateId,
    Report.Type type
  ) throws HubAnalysisException;
}
