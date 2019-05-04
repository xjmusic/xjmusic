// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.dub;

import io.xj.core.exception.CoreException;
import io.xj.core.fabricator.Fabricator;
import io.xj.dub.master.MasterDub;
import io.xj.dub.ship.ShipDub;

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
   [#141] Dubworker Segment mix final output from instrument-audio-arrangements

   @param fabricator of dub
   @return MasterDub
   @throws CoreException on failure
   */
  MasterDub master(
    @Assisted("basis") Fabricator fabricator
  ) throws CoreException;

  /**
   Create Ship Dub instance for a particular segment
   [#264] Segment audio is compressed to OGG_VORBIS and shipped to https://segment.xj.io

   @param fabricator of dub
   @return ShipDub
   @throws CoreException on failure
   */
  ShipDub ship(
    @Assisted("basis") Fabricator fabricator
  ) throws CoreException;

}
