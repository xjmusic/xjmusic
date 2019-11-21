// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.fabricator;

import io.xj.core.access.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.Segment;

import com.google.inject.assistedinject.Assisted;

/**
 Fabricator content = contentFactory.fabricate(segment);
 ... do things with this content, like craft or dub ...
 content.putReport();
 */
@FunctionalInterface
public interface FabricatorFactory {
  /**
   Create a fabricator to fabricate a segment

   @return Fabricator
   @throws CoreException on failure
   @param access control
   @param segment Segment to be worked on
   */
  Fabricator fabricate(
    @Assisted("access") Access access,
    @Assisted("segment") Segment segment
  ) throws CoreException;
}
