// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.dub;

import io.xj.lib.core.exception.CoreException;
import io.xj.lib.core.fabricator.Fabricator;

import com.google.inject.assistedinject.Assisted;

/**
 Dub is performed:
 1. Dub
 <p>
 Fabricator basis = basisFactory.fabricate(segment);
 dubFactory.master(basis).dub();
 basis.putReport();
 */
public interface DubFactory {

  /**
   Create Master Dub instance for a particular segment
   [#141] Dubworker Segment mix final output of instrument-audio-arrangements

   @param fabricator of dub
   @return MasterDub
   @throws CoreException on failure
   */
  Master master(
    @Assisted("basis") Fabricator fabricator
  ) throws CoreException;

  /**
   Create Ship Dub instance for a particular segment
   [#264] Segment audio is compressed to OGG and shipped to https://segment.xj.io

   @param fabricator of dub
   @return ShipDub
   @throws CoreException on failure
   */
  Ship ship(
    @Assisted("basis") Fabricator fabricator
  ) throws CoreException;

}
