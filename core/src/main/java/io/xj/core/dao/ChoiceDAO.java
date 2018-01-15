// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.choice.Choice;
import io.xj.core.model.pattern.PatternType;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;

public interface ChoiceDAO extends DAO<Choice> {

  /**
   Read one choice, binding a given pattern to a given link

   @param access    control
   @param linkId    to get choice for
   @param patternId to get choice for
   @return choice, or null if none exists
   @throws Exception on failure
   */
  @Nullable
  Choice readOneLinkPattern(Access access, BigInteger linkId, BigInteger patternId) throws Exception;

  /**
   Read the Choice of given type of Pattern for a given Link,
   including the phase offset, and all eitherOr
   phase offsets for that Pattern.

   @param access      control
   @param linkId      link to get choice for
   @param patternType type for choice to get
   @return record of choice, and eitherOr phase offsets
   */
  @Nullable
  Choice readOneLinkTypeWithAvailablePhaseOffsets(Access access, BigInteger linkId, PatternType patternType) throws Exception;

  /**
   Fetch many choice for many Links by id, if accessible

   @param access  control
   @param linkIds to fetch choices for.
   @return JSONArray of choices.
   @throws Exception on failure
   */
  Collection<Choice> readAllInLinks(Access access, Collection<BigInteger> linkIds) throws Exception;

}
