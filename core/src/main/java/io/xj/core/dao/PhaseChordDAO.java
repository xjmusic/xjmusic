// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.exception.DatabaseException;
import io.xj.core.model.chord.ChordSequence;
import io.xj.core.model.phase_chord.PhaseChord;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;

public interface PhaseChordDAO {

  /**
   Create a new PhaseChord

   @param access control
   @param entity for the new Account User.
   @return newly readMany record
   */
  PhaseChord create(Access access, PhaseChord entity) throws Exception;

  /**
   Fetch one Phase Chord if accessible

   @param access control
   @param id     of phase
   @return retrieved record
   @throws Exception on failure
   */
  @Nullable
  PhaseChord readOne(Access access, BigInteger id) throws Exception;

  /**
   Fetch all accessible Phase Chord for one Phase by id

   @param access  control
   @param phaseId to fetch phases for.
   @return JSONArray of phases.
   @throws Exception on failure
   */
  Collection<PhaseChord> readAll(Access access, BigInteger phaseId) throws Exception;

  /**
   Read all possible chord sequences for the specified phase
   @param access control
   @param phaseId to read chord sequences of
   @return collection of chord sequences
   */
  Collection<ChordSequence> readAllSequences(Access access, BigInteger phaseId) throws Exception;

  /**
   Update a specified Phase Chord if accessible

   @param access control
   @param id     of specific Chord to update.
   @param entity for the updated Chord.
   */
  void update(Access access, BigInteger id, PhaseChord entity) throws Exception;

  /**
   Delete a specified Phase Chord if accessible

   @param access control
   @param id     of specific phase to delete.
   */
  void delete(Access access, BigInteger id) throws Exception;
}
