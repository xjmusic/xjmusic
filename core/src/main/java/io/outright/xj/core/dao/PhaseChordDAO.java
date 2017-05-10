// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.model.phase_chord.PhaseChord;
import io.outright.xj.core.tables.records.PhaseChordRecord;

import org.jooq.Result;
import org.jooq.types.ULong;

import javax.annotation.Nullable;

public interface PhaseChordDAO {

  /**
   Create a new PhaseChord

   @param access control
   @param entity for the new Account User.
   @return newly readMany record
   */
  PhaseChordRecord create(Access access, PhaseChord entity) throws Exception;

  /**
   Fetch one Phase Chord if accessible

   @param access control
   @param id     of phase
   @return retrieved record
   @throws Exception on failure
   */
  @Nullable
  PhaseChordRecord readOne(Access access, ULong id) throws Exception;

  /**
   Fetch all accessible Phase Chord for one Phase by id

   @param access  control
   @param phaseId to fetch phases for.
   @return JSONArray of phases.
   @throws Exception on failure
   */
  Result<PhaseChordRecord> readAll(Access access, ULong phaseId) throws Exception;

  /**
   Update a specified Phase Chord if accessible

   @param access control
   @param id     of specific Chord to update.
   @param entity for the updated Chord.
   */
  void update(Access access, ULong id, PhaseChord entity) throws Exception;

  /**
   Delete a specified Phase Chord if accessible

   @param access control
   @param id     of specific phase to delete.
   */
  void delete(Access access, ULong id) throws Exception;
}
