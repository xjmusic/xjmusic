// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.link.Link;
import io.xj.core.model.link.LinkState;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Collection;

public interface LinkDAO {
  /**
   Create a new Link

   @param entity for the new Link.
   @return newly readMany Link record.
   */
  Link create(Access access, Link entity) throws Exception;

  /**
   Fetch one Link by id, if accessible

   @param access control
   @param id     to fetch
   @return Link if found
   @throws Exception on failure
   */
  @Nullable
  Link readOne(Access access, BigInteger id) throws Exception;

  /**
   Fetch id for the Link in a Chain at a given offset, if present

   @param access  control
   @param chainId to fetch link for
   @param offset  to fetch link at
   @return link id
   */
  Link readOneAtChainOffset(Access access, BigInteger chainId, BigInteger offset) throws Exception;

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
  Link readOneInState(Access access, BigInteger chainId, LinkState linkState, Timestamp linkBeginBefore) throws Exception;

  /**
   Read all Links that are accessible
   limit max # of links readable at once in environment configuration

   @param access  control
   @param chainId to read links for
   @return array of links as JSON
   @throws Exception on failure
   */
  Collection<Link> readAll(Access access, BigInteger chainId) throws Exception;

  /**
   Read all Links that are accessible, starting at a particular offset
   limit max # of links readable at once in environment configuration

   @param access     control
   @param chainId    to read all links from
   @param fromOffset to read links form
   @return array of links as JSON
   @throws Exception on failure
   */
  Collection<Link> readAllFromOffset(Access access, BigInteger chainId, BigInteger fromOffset) throws Exception;

  /**
   Read all Links that are accessible, starting at a particular time in seconds UTC since epoch.
   limit buffer ahead seconds readable at once in environment configuration
   <p>
   [#278] Chain Player lives in navbar, and handles all playback (audio waveform, link waveform, continuous chain) so the user always has central control over listening.

   @param access         control
   @param chainId        to read all links from
   @param fromSecondsUTC to read links from
   @return array of links as JSON
   @throws Exception on failure
   */
  Collection<Link> readAllFromSecondsUTC(Access access, BigInteger chainId, BigInteger fromSecondsUTC) throws Exception;

  /**
   Update a specified Link

   @param id     of specific Link to update.
   @param entity for the updated Link.
   */
  void update(Access access, BigInteger id, Link entity) throws Exception;

  /**
   Update the state of a specified Link

   @param id    of specific Link to update.
   @param state for the updated Link.
   */
  void updateState(Access access, BigInteger id, LinkState state) throws Exception;

  /**
   Destroy a specified Link, and all its child entities

   @param linkId of specific Link to destroy.
   */
  void destroy(Access access, BigInteger linkId) throws Exception;

}
