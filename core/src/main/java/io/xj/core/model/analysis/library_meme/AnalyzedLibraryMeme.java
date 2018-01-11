// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.analysis.library_meme;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 In-memory cache of analysis of a meme usage in a library

 [#154234716] Artist wants to run a library analysis in order to understand all of the existing contents within the patterns in a library.
 */
public class AnalyzedLibraryMeme {
  private final String name;
  private final Collection<BigInteger> instrumentIds = Lists.newArrayList();
  private final Collection<BigInteger> patternIds = Lists.newArrayList();
  private final Map<BigInteger, Collection<BigInteger>> patternPhaseIds = Maps.newConcurrentMap();

  /**
   New instance

   @param name of meme
   */
  public AnalyzedLibraryMeme(String name) {
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
   Add a pattern id, if it isn't already in the list

   @param id of pattern to add
   */
  public void addPatternId(BigInteger id) {
    if (!patternIds.contains(id))
      patternIds.add(id);
  }

  /**
   Add a pattern id, if it isn't already in the list

   @param patternId parent of phase
   @param id        of phase to add
   */
  public void addPatternPhaseId(BigInteger patternId, BigInteger id) {
    if (!patternPhaseIds.containsKey(patternId))
      patternPhaseIds.put(patternId, Lists.newArrayList());

    if (!patternPhaseIds.get(patternId).contains(id))
      patternPhaseIds.get(patternId).add(id);
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
   Get the pattern ids in which this meme is used

   @return collection of pattern id
   */
  public Collection<BigInteger> getPatternIds() {
    return Collections.unmodifiableCollection(patternIds);
  }

  /**
   Get the pattern ids for which this meme is used in phases therein

   @return collection of pattern id
   */
  public Collection<BigInteger> getPhasePatternIds() {
    return patternPhaseIds.keySet();
  }

  /**
   Get the phase ids in which this meme is used, for a given pattern

   @param patternId to get phases in which the meme is used
   @return collection of pattern phase id
   */
  public Collection<BigInteger> getPhaseIds(BigInteger patternId) {
    return patternPhaseIds.get(patternId);
  }
}
