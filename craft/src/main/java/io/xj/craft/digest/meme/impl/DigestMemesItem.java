// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.digest.meme.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

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
  private final Map<BigInteger, Collection<BigInteger>> sequencePatternIds = Maps.newConcurrentMap();

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

   @param sequenceId parent of pattern
   @param patternId        of pattern to add
   */
  public void addSequencePatternId(BigInteger sequenceId, BigInteger patternId) {
    if (!sequencePatternIds.containsKey(sequenceId))
      sequencePatternIds.put(sequenceId, Lists.newArrayList());

    if (!sequencePatternIds.get(sequenceId).contains(patternId))
      sequencePatternIds.get(sequenceId).add(patternId);
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
  public Collection<BigInteger> getPatternSequenceIds() {
    return sequencePatternIds.keySet();
  }

  /**
   Get the pattern ids in which this meme is used, for a given sequence

   @param sequenceId to get patterns in which the meme is used
   @return collection of sequence pattern id
   */
  public Collection<BigInteger> getPatternIds(BigInteger sequenceId) {
    return sequencePatternIds.getOrDefault(sequenceId, ImmutableList.of());
  }
}
