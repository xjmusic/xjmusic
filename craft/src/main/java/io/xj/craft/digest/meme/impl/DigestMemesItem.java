// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.digest.meme.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.xj.core.model.program.sub.SequenceBinding;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 In-memory cache of ingest of a meme usage in a library
 <p>
 [#154234716] Artist wants to run a library ingest in order to understand all of the existing contents within the programs in a library.
 */
public class DigestMemesItem {
  private final String name;
  private final Collection<BigInteger> instrumentIds = Lists.newArrayList();
  private final Collection<BigInteger> programIds = Lists.newArrayList();
  private final Map<BigInteger, Collection<UUID>> sequenceIds = Maps.newHashMap(); // programId: collection of sequenceIds mapped to it

  /**
   New instance

   @param name of meme
   */
  public DigestMemesItem(String name) {
    this.name = name;
  }

  /**
   Get name of meme

   @return meme name
   */
  public String getName() {
    return name;
  }

  /**
   Add a program id, if it isn't already in the list

   @param id of program to add
   */
  public void addProgramId(BigInteger id) {
    if (!programIds.contains(id))
      programIds.add(id);
  }

  /**
   Add a program id, if it isn't already in the list

   @param sequenceBinding to add
   */
  public void addSequenceBinding(SequenceBinding sequenceBinding) {
    addProgramId(sequenceBinding.getProgramId());

    BigInteger programId = sequenceBinding.getProgramId();
    if (!sequenceIds.containsKey(programId)) {
      sequenceIds.put(programId, Lists.newArrayList());
    }

    UUID sequenceId = sequenceBinding.getSequenceId();
    if (!sequenceIds.get(programId).contains(sequenceId)) {
      sequenceIds.get(programId).add(sequenceId);
    }
  }

  /**
   Add a instrument id, if it isn't already in the list

   @param id of instrument to add
   */
  public void addInstrumentId(BigInteger id) {
    if (!instrumentIds.contains(id))
      instrumentIds.add(id);
  }

  /**
   Get the instrument ids in which this meme is used

   @return collection of instrument id
   */
  public Collection<BigInteger> getInstrumentIds() {
    return Collections.unmodifiableCollection(instrumentIds);
  }

  /**
   Get the program ids in which this meme is used

   @return collection of program id
   */
  public Collection<BigInteger> getProgramIds() {
    return Collections.unmodifiableCollection(programIds);
  }

  /**
   Get the sequence ids is used in sequences in a particular program

   @return collection of program id
   */
  public Collection<UUID> getSequenceIds(BigInteger programId) {
    return sequenceIds.getOrDefault(programId, Lists.newArrayList());
  }

}
