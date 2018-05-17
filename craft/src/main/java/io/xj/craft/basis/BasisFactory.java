// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.basis;

import io.xj.core.exception.ConfigException;
import io.xj.core.model.segment.Segment;

import com.google.inject.assistedinject.Assisted;

/**
 Basis basis = basisFactory.createBasis(segment);
 ... do things with this basis, like craft or dub ...
 basis.sendReport();
 */
@FunctionalInterface
public interface BasisFactory {
  /**
   Create a basis for Macro & Main sequence craft (previous segment and other cached resources)

   @param segment Segment to be worked on
   @return ChainWorkMaster
   @throws ConfigException on failure
   */
  Basis createBasis(
    @Assisted("segment") Segment segment
  ) throws ConfigException;
}
