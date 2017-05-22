// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.model.link_chord.LinkChord;
import io.outright.xj.core.tables.records.LinkChordRecord;

import org.jooq.Result;
import org.jooq.types.ULong;

import javax.annotation.Nullable;
import java.sql.ResultSet;

public interface LinkChordDAO {

  /**
   Create a new LinkChord

   @param access control
   @param entity for the new Account User.
   @return newly readMany record
   */
  LinkChordRecord create(Access access, LinkChord entity) throws Exception;

  /**
   Fetch one Link Chord if accessible

   @param access control
   @param id     of link
   @return retrieved record
   @throws Exception on failure
   */
  @Nullable
  LinkChordRecord readOne(Access access, ULong id) throws Exception;

  /**
   Fetch all accessible Link Chord for one Link by id
   Order by position ascending

   @param access control
   @param linkId to fetch links for.
   @return JSONArray of links.
   @throws Exception on failure
   */
  Result<LinkChordRecord> readAll(Access access, ULong linkId) throws Exception;

  /**
   Fetch many linkChord for all Links in a Chain by id, if accessible
   order by position descending, ala other "in chain" results

   @param access control
   @param chainId to fetch linkChords for.
   @return JSONArray of linkChords.
   @throws Exception on failure
   */
  Result<LinkChordRecord> readAllInChain(Access access, ULong chainId) throws Exception;

  /**
   Update a specified Link Chord if accessible

   @param access control
   @param id     of specific Chord to update.
   @param entity for the updated Chord.
   */
  void update(Access access, ULong id, LinkChord entity) throws Exception;

  /**
   Delete a specified Link Chord if accessible

   @param access control
   @param id     of specific link to delete.
   */
  void delete(Access access, ULong id) throws Exception;

}
