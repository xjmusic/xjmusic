// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.analysis;

import com.google.inject.assistedinject.Assisted;
import io.xj.hub.access.HubAccess;

import java.util.UUID;

/**
 Template content Analysis #161199945
 */
@FunctionalInterface
public interface HubAnalysisFactory {

  /**
   Template content Analysis #161199945

   @return entities to be evaluated
   @throws HubAnalysisException on failure to of target entities
   @param access control
   @param type type of report to generate
   */
  Report report(
    @Assisted("access") HubAccess access,
    @Assisted("templateId") UUID templateId,
    @Assisted("analyze") Report.Type type
  ) throws HubAnalysisException;
}
