// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.library.Library;
import io.xj.core.model.library.LibraryHash;

import java.math.BigInteger;

public interface LibraryDAO extends DAO<Library> {

  /**
   [#154343470] Ops wants LibraryHash to compute the hash of an entire library, which can be used as a unique stamp of the state of the library's entire contents at any instant

   @param access control
   @param id     of library   to get hash of
   @return a JSONObject, which can then be reduced by whatever means to a simpler hash, e.g. MD5
   @throws Exception on failure
   */
  LibraryHash readHash(Access access, BigInteger id) throws Exception;

}
