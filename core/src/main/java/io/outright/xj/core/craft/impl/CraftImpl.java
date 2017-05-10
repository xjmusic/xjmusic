// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.craft.impl;

import io.outright.xj.core.app.exception.BusinessException;

import java.util.Objects;

/**
 [#214] If a Chain has Ideas associated with it directly, prefer those choices to any in the Library
 */
class CraftImpl {
  /**
   Ensure that an object is non-null, else throw exception

   @param name   of entity
   @param entity to check
   @throws BusinessException if null
   */
  void ensureExists(String name, Object entity) throws BusinessException {
    if (Objects.isNull(entity))
      throw new BusinessException(name + "does not exist!");
  }

  /**
   Return the first value if it's non-null, else the second

   @param d1 to check if non-null and return
   @param d2 to default to, if s1 is null
   @return s1 if non-null, else s2
   */
  Double available(Double d1, Double d2) {
    if (Objects.nonNull(d1) && !d1.isNaN() && !d1.equals(0d))
      return d1;
    else
      return d2;
  }

  /**
   Return the first value if it's non-null, else the second

   @param s1 to check if non-null and return
   @param s2 to default to, if s1 is null
   @return s1 if non-null, else s2
   */
  String available(String s1, String s2) {
    if (Objects.nonNull(s1) && s1.length() > 0)
      return s1;
    else
      return s2;
  }


}

