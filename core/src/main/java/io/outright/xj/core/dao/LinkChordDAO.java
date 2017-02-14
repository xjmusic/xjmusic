
// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.app.access.AccessControl;
import io.outright.xj.core.model.link_chord.LinkChordWrapper;

import org.jooq.types.ULong;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;

public interface LinkChordDAO {

  /**
   * Create a new LinkChord
   *
   * @param access control
   * @param data   for the new Account User.
   * @return newly created record as JSON
   */
  JSONObject create(AccessControl access, LinkChordWrapper data) throws Exception;

  /**
   * Fetch one Link Chord if accessible
   *
   * @param access control
   * @param id     of link
   * @return retrieved record as JSON
   * @throws Exception on failure
   */
  @Nullable
  JSONObject readOne(AccessControl access, ULong id) throws Exception;

  /**
   * Fetch all accessible Link Chord for one Link by id
   *
   * @param access    control
   * @param linkId to fetch links for.
   * @return JSONArray of links.
   * @throws Exception on failure
   */
  @Nullable
  JSONArray readAllIn(AccessControl access, ULong linkId) throws Exception;

  /**
   * Update a specified Link Chord if accessible
   *
   * @param access control
   * @param id of specific Chord to update.
   * @param data   for the updated Chord.
   */
  void update(AccessControl access, ULong id, LinkChordWrapper data) throws Exception;

  /**
   * Delete a specified Link Chord if accessible
   *
   * @param access control
   * @param id of specific link to delete.
   */
  void delete(AccessControl access, ULong id) throws Exception;
}
