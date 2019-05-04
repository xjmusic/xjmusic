// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.library.Library;

import java.math.BigInteger;
import java.util.Collection;

public interface LibraryDAO extends DAO<Library> {

  /**
   Fetch many sequence bound to a particular chain

   @param access  control
   @param chainId to fetch sequences for.
   @return collection of sequences.
   @throws CoreException on failure
   */
  Collection<Library> readAllBoundToChain(Access access, BigInteger chainId) throws CoreException;
}
