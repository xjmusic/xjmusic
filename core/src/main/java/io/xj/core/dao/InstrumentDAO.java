// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.pattern.Pattern;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;

public interface InstrumentDAO {

  /**
   Create a new Instrument

   @param access control
   @param entity for the new Account User.
   @return newly readMany record
   */
  Instrument create(Access access, Instrument entity) throws Exception;

  /**
   Clone a Instrument into a new Instrument

   @param access control
   @param cloneId of instrument to clone
   @param entity for the new Instrument
   @return newly readMany record
   */
  Instrument clone(Access access, BigInteger cloneId, Instrument entity) throws Exception;

  /**
   Fetch one instrument if accessible

   @param access control
   @param id     of instrument
   @return retrieved record
   @throws Exception on failure
   */
  @Nullable
  Instrument readOne(Access access, BigInteger id) throws Exception;

  /**
   Fetch many instrument for one Account by id, if accessible

   @param access    control
   @param accountId to fetch instruments for.
   @return Collection of instruments.
   @throws Exception on failure
   */
  Collection<Instrument> readAllInAccount(Access access, BigInteger accountId) throws Exception;

  /**
   Fetch many instrument for one Library by id, if accessible

   @param access    control
   @param libraryId to fetch instruments for.
   @return Collection of instruments.
   @throws Exception on failure
   */
  Collection<Instrument> readAllInLibrary(Access access, BigInteger libraryId) throws Exception;

  /**
   Fetch all instrument visible to given access

   @param access    control
   @return Collection of instruments.
   @throws Exception on failure
   */
  Collection<Instrument> readAll(Access access) throws Exception;

  /**
   Fetch many instrument for one Account by id, if accessible

   @param access         control
   @param chainId        to fetch instruments for.
   @param instrumentType to fetch
   @return Collection of instruments.
   @throws Exception on failure
   */
  Collection<Instrument> readAllBoundToChain(Access access, BigInteger chainId, InstrumentType instrumentType) throws Exception;

  /**
   Fetch many instrument for one Account by id, if accessible

   @param access         control
   @param chainId        to fetch instruments for.
   @param instrumentType to fetch
   @return Collection of instruments.
   @throws Exception on failure
   */
  Collection<Instrument> readAllBoundToChainLibrary(Access access, BigInteger chainId, InstrumentType instrumentType) throws Exception;

  /**
   (ADMIN ONLY)
   Update a specified Instrument

   @param access       control
   @param instrumentId of specific Instrument to update.
   @param entity       for the updated Instrument.
   */
  void update(Access access, BigInteger instrumentId, Instrument entity) throws Exception;

  /**
   (ADMIN ONLY)
   Delete a specified instrument

   @param access control
   @param id     of specific instrument to delete.
   */
  void delete(Access access, BigInteger id) throws Exception;

}
