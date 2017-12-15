// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.link_chord.LinkChord;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;

public interface LinkChordDAO {

  /**
   Create a new LinkChord

   @param access control
   @param entity for the new Account User.
   @return newly readMany record
   */
  LinkChord create(Access access, LinkChord entity) throws Exception;

  /**
   Fetch one Link Chord if accessible

   @param access control
   @param id     of link
   @return retrieved record
   @throws Exception on failure
   */
  @Nullable
  LinkChord readOne(Access access, BigInteger id) throws Exception;

  /**
   Fetch all accessible Link Chord for one Link by id
   Order by position ascending

   @param access control
   @param linkId to fetch links for.
   @return JSONArray of links.
   @throws Exception on failure
   */
  Collection<LinkChord> readAll(Access access, BigInteger linkId) throws Exception;

  /**
   Fetch many linkChord for many Links by id, if accessible
   order by position descending, ala other "in chain" results

   @param access  control
   @param linkIds to fetch linkChords for.
   @return JSONArray of linkChords.
   @throws Exception on failure
   */
  Collection<LinkChord> readAllInLinks(Access access, Collection<BigInteger> linkIds) throws Exception;

  /**
   Update a specified Link Chord if accessible

   @param access control
   @param id     of specific Chord to update.
   @param entity for the updated Chord.
   */
  void update(Access access, BigInteger id, LinkChord entity) throws Exception;

  /**
   Delete a specified Link Chord if accessible

   @param access control
   @param id     of specific link to delete.
   */
  void delete(Access access, BigInteger id) throws Exception;

}
