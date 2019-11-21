// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.fabricator;

import com.google.inject.assistedinject.Assisted;
import io.xj.core.access.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.Segment;

/**
 Fabricator content = contentFactory.workOn(segment);
 ... do things with this content, like craft or dub ...
 content.putReport();
 */
@FunctionalInterface
public interface SegmentWorkbenchFactory {
  /**
   Create a workbench to fabricate a particular segment

   @param access  control
   @param segment Segment to be worked on
   @return SegmentWorkbench
   @throws CoreException on failure
   */
  SegmentWorkbench workOn(
    @Assisted("access") Access access,
    @Assisted("segment") Segment segment
  ) throws CoreException;
}
