// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.dubworker.dub;

import io.xj.core.app.exception.ConfigException;
import io.xj.core.basis.Basis;

import com.google.inject.assistedinject.Assisted;

/**
 Dub is performed:
 1. Dub
 <p>
 Basis basis = basisFactory.createBasis(link);
 dubFactory.master(basis).dub();
 basis.sendReport();
 */
public interface DubFactory {

  /**
   Create Master Dub instance for a particular link
   [#141] Dubworker Link mix final output from instrument-audio-arrangements

   @param basis of dub
   @return MasterDub
   @throws ConfigException on failure
   */
  MasterDub master(
    @Assisted("basis") Basis basis
  ) throws ConfigException;

  /**
   Create Ship Dub instance for a particular link
   [#264] Link audio is compressed to MP3 and shipped to https://link.xj.io

   @param basis of dub
   @return ShipDub
   @throws ConfigException on failure
   */
  ShipDub ship(
    @Assisted("basis") Basis basis
  ) throws ConfigException;

}
