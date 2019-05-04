// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.digest.meme.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.xj.core.model.sequence_pattern.SequencePattern;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 In-memory cache of ingest of a meme usage in a library
 <p>
 [#154234716] Artist wants to run a library ingest in order to understand all of the existing contents within the sequences in a library.
 */
public class DigestMemesItem {
  private final String name;
  private final Collection<BigInteger> instrumentIds = Lists.newArrayList();
  private final Collection<BigInteger> sequenceIds = Lists.newArrayList();
  private final Map<BigInteger, Collection<BigInteger>> patternIds = Maps.newConcurrentMap(); // sequenceId: collection of patternIds mapped to it

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
   Add a sequence id, if it isn't already in the list

   @param id of sequence to add
   */
  public void addSequenceId(BigInteger id) {
    if (!sequenceIds.contains(id))
      sequenceIds.add(id);
  }

  /**
   Add a sequence id, if it isn't already in the list

   @param sequencePattern to add
   */
  public void addSequencePattern(SequencePattern sequencePattern) {
    addSequenceId(sequencePattern.getSequenceId());

    BigInteger sequenceId = sequencePattern.getSequenceId();
    if (!patternIds.containsKey(sequenceId)) {
      patternIds.put(sequenceId, Lists.newArrayList());
    }

    BigInteger patternId = sequencePattern.getPatternId();
    if (!patternIds.get(sequenceId).contains(patternId)) {
      patternIds.get(sequenceId).add(patternId);
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
   Get the sequence ids in which this meme is used

   @return collection of sequence id
   */
  public Collection<BigInteger> getSequenceIds() {
    return Collections.unmodifiableCollection(sequenceIds);
  }

  /**
   Get the sequence ids for which this meme is used in patterns therein

   @return collection of sequence id
   */
  public Collection<BigInteger> getPatternIds(BigInteger sequenceId) {
    return patternIds.getOrDefault(sequenceId, Lists.newArrayList());
  }

}
