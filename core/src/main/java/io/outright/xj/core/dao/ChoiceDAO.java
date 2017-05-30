// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.model.choice.Choice;
import io.outright.xj.core.tables.records.ChoiceRecord;

import org.jooq.Result;
import org.jooq.types.ULong;

import javax.annotation.Nullable;
import java.util.List;

public interface ChoiceDAO {
  /**
   Create a new Choice

   @param entity for the new Choice.
   @return newly readMany Choice record.
   */
  ChoiceRecord create(Access access, Choice entity) throws Exception;

  /**
   Fetch one Choice by id, if accessible

   @param access   control
   @param choiceId to fetch
   @return Choice if found
   @throws Exception on failure
   */
  @Nullable
  ChoiceRecord readOne(Access access, ULong choiceId) throws Exception;

  /**
   Read one choice, binding a given idea to a given link

   @param access control
   @param linkId to get choice for
   @param ideaId to get choice for
   @return choice, or null if none exists
   @throws Exception on failure
   */
  @Nullable
  ChoiceRecord readOneLinkIdea(Access access, ULong linkId, ULong ideaId) throws  Exception;

  /**
   Read the Choice of given type of Idea for a given Link,
   including the phase offset, and all eitherOr
   phase offsets for that Idea.

   @param access     control
   @param linkId link to get choice for
   @param ideaType   type for choice to get
   @return record of choice, and eitherOr phase offsets
   */
  @Nullable
  Choice readOneLinkTypeWithAvailablePhaseOffsets(Access access, ULong linkId, String ideaType) throws Exception;

  /**
   Read all Choices that are accessible

   @param access control
   @return array of choices as JSON
   @throws Exception on failure
   */
  Result<ChoiceRecord> readAll(Access access, ULong linkId) throws Exception;

  /**
   Fetch many choice for many Links by id, if accessible

   @return JSONArray of choices.
   @throws Exception on failure
    @param access control
   @param linkIds to fetch choices for.
   */
  Result<ChoiceRecord> readAllInLinks(Access access, List<ULong> linkIds) throws Exception;

  /**
   Update a specified Choice

   @param choiceId of specific Choice to update.
   @param entity   for the updated Choice.
   */
  void update(Access access, ULong choiceId, Choice entity) throws Exception;

  /**
   Delete a specified Choice

   @param choiceId of specific Choice to delete.
   */
  void delete(Access access, ULong choiceId) throws Exception;

}
