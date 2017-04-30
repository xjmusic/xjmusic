// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.model.phase_chord.PhaseChordWrapper;

import org.jooq.types.ULong;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;

public interface PhaseChordDAO {

  /**
   Create a new PhaseChord

   @param access control
   @param data   for the new Account User.
   @return newly created record as JSON
   */
  JSONObject create(AccessControl access, PhaseChordWrapper data) throws Exception;

  /**
   Fetch one Phase Chord if accessible

   @param access control
   @param id     of phase
   @return retrieved record as JSON
   @throws Exception on failure
   */
  @Nullable
  JSONObject readOne(AccessControl access, ULong id) throws Exception;

  /**
   Fetch all accessible Phase Chord for one Phase by id

   @param access  control
   @param phaseId to fetch phases for.
   @return JSONArray of phases.
   @throws Exception on failure
   */
  @Nullable
  JSONArray readAllIn(AccessControl access, ULong phaseId) throws Exception;

  /**
   Update a specified Phase Chord if accessible

   @param access control
   @param id     of specific Chord to update.
   @param data   for the updated Chord.
   */
  void update(AccessControl access, ULong id, PhaseChordWrapper data) throws Exception;

  /**
   Delete a specified Phase Chord if accessible

   @param access control
   @param id     of specific phase to delete.
   */
  void delete(AccessControl access, ULong id) throws Exception;
}
