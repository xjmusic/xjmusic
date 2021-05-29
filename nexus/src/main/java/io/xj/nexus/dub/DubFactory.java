// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.dub;

import com.google.inject.assistedinject.Assisted;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.NexusException;

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
   Create DubMaster Dub instance for a particular segment
   [#141] Dub process Segment mix final output of instrument-audio-arrangements

   @param fabricator of dub
   @return MasterDub
   @throws NexusException on failure
   */
  DubMaster master(
    @Assisted("basis") Fabricator fabricator
  ) throws NexusException;

  /**
   Create DubShip Dub instance for a particular segment
   [#264] Segment audio is compressed to OGG and shipped to https://segment.xj.io

   @param fabricator of dub
   @return ShipDub
   @throws NexusException on failure
   */
  DubShip ship(
    @Assisted("basis") Fabricator fabricator
  ) throws NexusException;

}
