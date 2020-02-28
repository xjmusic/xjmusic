// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.dao;

import io.xj.lib.core.access.Access;
import io.xj.lib.core.exception.CoreException;
import io.xj.lib.core.model.ProgramType;
import io.xj.lib.core.model.Segment;
import io.xj.lib.core.model.SegmentChoice;

public interface SegmentChoiceDAO extends DAO<SegmentChoice> {

  /**
   Read one segment choice of a given type for a given segment

   @param access control
   @param segment to get choice for
   @param type    of chocie to get
   @return segment choice of the given type for the given segment
   */
  SegmentChoice readOneOfTypeForSegment(Access access, Segment segment, ProgramType type) throws CoreException;
}
