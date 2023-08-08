// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.analysis;


import io.xj.hub.HubContent;
import io.xj.nexus.hub_client.access.HubAccess;

/**
 * Template content Analysis https://www.pivotaltracker.com/story/show/161199945
 */
@FunctionalInterface
public interface HubAnalysisFactory {

  /**
   * Template content Analysis https://www.pivotaltracker.com/story/show/161199945
   *
   * @param access  control
   * @param content hub content
   * @return entities to be evaluated
   * @throws HubAnalysisException on failure to of target entities
   */
  Report report(
    HubAccess access,
    HubContent content,
    Report.Type type
  ) throws HubAnalysisException;
}
