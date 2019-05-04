// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.fabricator;

import io.xj.core.exception.CoreException;
import io.xj.core.model.segment.Segment;

import com.google.inject.assistedinject.Assisted;

/**
 Fabricator content = contentFactory.fabricate(segment);
 ... do things with this content, like craft or dub ...
 content.putReport();
 */
@FunctionalInterface
public interface FabricatorFactory {
  /**
   Create a content for Macro & Main sequence craft (previous segment and other cached resources)

   @param segment Segment to be worked on
   @return ChainWorkMaster
   @throws CoreException on failure
   */
  Fabricator fabricate(
    @Assisted("segment") Segment segment
  ) throws CoreException;
}
