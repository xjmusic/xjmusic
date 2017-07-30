// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.app.access.impl.Access;
import io.xj.core.model.link.Link;
import io.xj.core.model.link.LinkState;
import io.xj.core.tables.records.LinkRecord;

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

   @return Link if found
   @throws Exception on failure
    @param access          control
   @param chainId         to find link in
   @param linkState       linkState to find link in
   @param linkBeginBefore ahead to look for links
   */
  @Nullable
  LinkRecord readOneInState(Access access, ULong chainId, LinkState linkState, Timestamp linkBeginBefore) throws Exception;

  /**
   Read all Links that are accessible
   limit max # of links readable at once in environment configuration

   @param access control
   @return array of links as JSON
   @throws Exception on failure
   */
  Result<LinkRecord> readAll(Access access, ULong chainId) throws Exception;

  /**
   Read all Links that are accessible, starting at a particular offset
   limit max # of links readable at once in environment configuration

   @param access  control
   @param chainId to read all links from
   @return array of links as JSON
   @throws Exception on failure
   */
  Result<LinkRecord> readAllFromOffset(Access access, ULong chainId, ULong fromOffset) throws Exception;

  /**
   Read all Links that are accessible, starting at a particular time in seconds UTC since epoch.
   limit buffer ahead seconds readable at once in environment configuration
   <p>
   Supports [#278] Chain Player lives in navbar, and handles all playback (audio waveform, link waveform, continuous chain) so the user always has central control over listening.

   @param access  control
   @param chainId to read all links from
   @return array of links as JSON
   @throws Exception on failure
   */
  Result<LinkRecord> readAllFromSecondsUTC(Access access, ULong chainId, ULong fromSecondsUTC) throws Exception;

  /**
   Update a specified Link

   @param id     of specific Link to update.
   @param entity for the updated Link.
   */
  void update(Access access, ULong id, Link entity) throws Exception;

  /**
   Update the state of a specified Link
   * @param id    of specific Link to update.
   @param state for the updated Link.

   */
  void updateState(Access access, ULong id, LinkState state) throws Exception;

  /**
   Destroy a specified Link, and all its child entities

   @param linkId of specific Link to destroy.
   */
  void destroy(Access access, ULong linkId) throws Exception;

}
