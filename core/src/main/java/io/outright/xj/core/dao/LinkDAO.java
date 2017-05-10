// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.model.link.Link;
import io.outright.xj.core.model.link.LinkChoice;
import io.outright.xj.core.tables.records.LinkRecord;

import org.jooq.Result;
import org.jooq.types.ULong;

import javax.annotation.Nullable;
import java.sql.Timestamp;

public interface LinkDAO {
  /**
   Create a new Link

   @param entity for the new Link.
   @return newly readMany Link record.
   */
  LinkRecord create(Access access, Link entity) throws Exception;

  /**
   Fetch one Link by id, if accessible

   @param access control
   @param id     to fetch
   @return Link if found
   @throws Exception on failure
   */
  @Nullable
  LinkRecord readOne(Access access, ULong id) throws Exception;

  /**
   Fetch id for the Link in a Chain at a given offset, if present

   @param access  control
   @param chainId to fetch link for
   @param offset  to fetch link at
   @return link id
   */
  LinkRecord readOneAtChainOffset(Access access, ULong chainId, ULong offset) throws Exception;

  /**
   Fetch one Link by chainId and state, if present

   @param access          control
   @param chainId         to find link in
   @param linkState       linkState to find link in
   @param linkBeginBefore ahead to look for links
   @return Link if found
   @throws Exception on failure
   */
  @Nullable
  LinkRecord readOneInState(Access access, ULong chainId, String linkState, Timestamp linkBeginBefore) throws Exception;

  /**
   Read the Choice of given type of Idea for a given Link,
   including the phase offset, and all available
   phase offsets for that Idea.

   @param access     control
   @param linkOffset link to get choice for
   @param ideaType   type for choice to get
   @return record of choice, and available phase offsets
   */
  LinkChoice readLinkChoice(Access access, ULong linkOffset, String ideaType) throws Exception;

  /**
   Read all Links that are accessible

   @param access control
   @return array of links as JSON
   @throws Exception on failure
   */
  Result<LinkRecord> readAll(Access access, ULong chainId) throws Exception;

  /**
   Update a specified Link

   @param id     of specific Link to update.
   @param entity for the updated Link.
   */
  void update(Access access, ULong id, Link entity) throws Exception;

  /**
   Update the state of a specified Link

   @param id    of specific Link to update.
   @param state for the updated Link.
   */
  void updateState(Access access, ULong id, String state) throws Exception;

  /**
   Delete a specified Link

   @param linkId of specific Link to delete.
   */
  void delete(Access access, ULong linkId) throws Exception;
}
