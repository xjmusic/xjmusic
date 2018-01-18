// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.library.Library;

import java.math.BigInteger;
import java.util.Collection;

public interface LibraryDAO extends DAO<Library> {

  /**
   Fetch many pattern bound to a particular chain

   @param access  control
   @param chainId to fetch patterns for.
   @return collection of patterns.
   @throws Exception on failure
   */
  Collection<Library> readAllBoundToChain(Access access, BigInteger chainId) throws Exception;
}
