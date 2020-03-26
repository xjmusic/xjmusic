// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import io.xj.service.hub.HubException;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.model.ProgramType;
import io.xj.service.hub.model.Segment;
import io.xj.service.hub.model.SegmentChoice;

public interface SegmentChoiceDAO extends DAO<SegmentChoice> {

  /**
   Read one segment choice of a given type for a given segment

   @param access  control
   @param segment to get choice for
   @param type    of chocie to get
   @return segment choice of the given type for the given segment
   */
  SegmentChoice readOneOfTypeForSegment(Access access, Segment segment, ProgramType type) throws HubException;
}
