// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.instrument.Instrument;

import java.math.BigInteger;
import java.util.Collection;

public interface InstrumentDAO extends DAO<Instrument> {

  /**
   Clone a Instrument into a new Instrument

   @param access  control
   @param cloneId of instrument to clone
   @param entity  for the new Instrument
   @return newly readMany record
   */
  Instrument clone(Access access, BigInteger cloneId, Instrument entity) throws Exception;

  /**
   Fetch many instrument for one Account by id, if accessible

   @param access    control
   @param accountId to fetch instruments for.
   @return Collection of instruments.
   @throws Exception on failure
   */
  Collection<Instrument> readAllInAccount(Access access, BigInteger accountId) throws Exception;

  /**
   Fetch all instrument visible to given access

   @param access control
   @return Collection of instruments.
   @throws Exception on failure
   */
  Collection<Instrument> readAll(Access access) throws Exception;

  /**
   Fetch many instrument bound to a particular chain

   @param access  control
   @param chainId to fetch instruments for.
   @return collection of instruments.
   @throws Exception on failure
   */
  Collection<Instrument> readAllBoundToChain(Access access, BigInteger chainId) throws Exception;


}
