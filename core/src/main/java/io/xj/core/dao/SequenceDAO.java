// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.sequence.Sequence;
import io.xj.core.model.sequence.SequenceState;
import io.xj.core.model.sequence.SequenceType;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;

public interface SequenceDAO extends DAO<Sequence> {

  /**
   Clone a Sequence into a new Sequence

   @param access  control
   @param cloneId of sequence to clone
   @param entity  for the new Sequence
   @return newly readMany record
   */
  Sequence clone(Access access, BigInteger cloneId, Sequence entity) throws Exception;

  /**
   Read a given type of sequence for a given segment

   @param access      control
   @param segmentId      to read sequence for
   @param sequenceType type of sequence to read
   @return macro-type sequence; null if none found
   */
  @Nullable
  Sequence readOneTypeInSegment(Access access, BigInteger segmentId, SequenceType sequenceType) throws Exception;

  /**
   Fetch many sequence bound to a particular chain

   @param access  control
   @param chainId to fetch sequences for.
   @return collection of sequences.
   @throws Exception on failure
   */
  Collection<Sequence> readAllBoundToChain(Access access, BigInteger chainId) throws Exception;

  /**
   Fetch many sequence for one Account by id, if accessible

   @param access    control
   @param accountId to fetch sequences for.
   @return JSONArray of sequences.
   @throws Exception on failure
   */
  Collection<Sequence> readAllInAccount(Access access, BigInteger accountId) throws Exception;

  /**
   Fetch all sequence visible to given access

   @param access control
   @return JSONArray of sequences.
   @throws Exception on failure
   */
  Collection<Sequence> readAll(Access access) throws Exception;

  /**
   Fetch all Sequence in a certain state
   [INTERNAL USE ONLY]

   @param access control
   @param state  to get sequences in
   @return Result of sequence records.
   @throws Exception on failure
   */
  Collection<Sequence> readAllInState(Access access, SequenceState state) throws Exception;

  /**
   Erase a specified Sequence if accessible.
   [#154887174] SequenceErase job erase a Sequence and all its Patterns in the background, in order to keep the UI functioning at a reasonable speed.

   @param access control
   @param id     of specific sequence to erase.
   */
  void erase(Access access, BigInteger id) throws Exception;
}
