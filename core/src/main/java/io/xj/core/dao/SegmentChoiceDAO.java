// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.ProgramType;
import io.xj.core.model.Segment;
import io.xj.core.model.SegmentChoice;

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
