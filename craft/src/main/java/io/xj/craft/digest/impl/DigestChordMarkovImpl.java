// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.digest.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.core.config.Config;
import io.xj.core.exception.CoreException;
import io.xj.core.ingest.Ingest;
import io.xj.core.model.ProgramSequenceChord;
import io.xj.craft.chord.ChordMarkovNode;
import io.xj.craft.chord.ChordNode;
import io.xj.craft.digest.DigestChordMarkov;
import io.xj.craft.digest.DigestType;
import io.xj.craft.exception.CraftException;
import io.xj.music.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 In-memory cache of ingest of all entities in a library
 <p>
 [#154234716] Architect wants ingest of library contents, to modularize graph mathematics used during craft, and provide the Artist with useful insight for developing the library.
 <p>
 DigestChordMarkov transposes all of its observations into the Key of the sequence/pattern the entities are being observed in-- so if a Sequence in the Key of GenericChord begins with a SequenceChord D Major, that will be considered a modulo 2 semitones delta at the beginning of the sequence. Later, when generating a super sequence, if the target sequence is in the key of G and this outcome is selected, the first chord in the generated sequence which actually be A.
 */
public class DigestChordMarkovImpl extends DigestImpl implements DigestChordMarkov {
  private final Map<String, ChordMarkovNode> forwardNodeMap = Maps.newHashMap();
  private final Map<String, ChordMarkovNode> reverseNodeMap = Maps.newHashMap();
  private final Logger log = LoggerFactory.getLogger(DigestChordMarkovImpl.class);
  private final Integer markovOrder = Config.getChordMarkovOrder();

  /**
   Instantiate a new digest with a collection of target entities

   @param ingest to digest
   */
  @Inject
  public DigestChordMarkovImpl(
    @Assisted("ingest") Ingest ingest
  ) {
    super(ingest, DigestType.DigestChordMarkov);
    log.info("will digest {}", ingest);
    try {
      digest();
    } catch (Exception e) {
      log.error("Failed to digest entities create ingest {}", ingest, e);
    }
  }

  /**
   Get only the entities of a particular parent (probably pattern entities in a parent pattern)

   @param chords   to search for entities
   @param parentId (pattern) to get pattern entities of
   @return collection of audio entities
   */
  private static Collection<ProgramSequenceChord> chordsOf(Iterable<? extends ProgramSequenceChord> chords, Object parentId) {
    Collection<ProgramSequenceChord> result = Lists.newArrayList();
    chords.forEach(sequenceChord -> {
      if (Objects.equals(parentId, sequenceChord.getParentId())) result.add(sequenceChord);
    });
    return result;
  }

  /**
   Add an observation to all subsets of a precedent state (a 3-order chain requires 3 passes, the 1-order pass, 2-order pass, and 3-order pass)
   */
  private static void addObservationToAllSubsets(Map<String, ChordMarkovNode> markovNodeMap, Iterable<ChordNode> fromBuffer, ChordNode observation) {
    List<ChordNode> buffer = Lists.newArrayList(fromBuffer);
    boolean more = !buffer.isEmpty();
    while (more) {
      addObservation(markovNodeMap, buffer, observation);
      buffer.remove(0);
      more = !buffer.isEmpty();
    }
  }

  /**
   Put a SequenceChord markov into the in-memory store. Keyed by precedent state (the chord leading up to these observations).
   If no observations have been added with the given precedent state, a new chord markov node will be instantiated.

   @param precedentState the chord progression preceding this observation
   @param observation    observation to add.
   */
  private static void addObservation(Map<String, ChordMarkovNode> markovNodeMap, List<ChordNode> precedentState, ChordNode observation) {
    String key = new ChordMarkovNode(precedentState).precedentStateDescriptor();
    if (!markovNodeMap.containsKey(key))
      markovNodeMap.put(key, new ChordMarkovNode(precedentState));

    markovNodeMap.get(key).addObservation(observation);
  }

  /**
   Digest entities of ingest
   */
  private void digest() throws CraftException {
/*
    for (Pattern pattern : ingest.getAllPatterns()) {
      try {
        computeAllNodes(ingest.getKeyOfPattern(pattern.getId()), pattern.getChords()); // FUTURE implement getChords() on pattern

      } catch (CoreException e) {
        throw exception("Could not compute nodes", e);
      }
    }
*/
  }

  /**
   Get the key of any pattern-- if the pattern has no key, get the pattern of its sequence

   @param id of pattern to get key of
   @return key of pattern
   */
  private Key getKeyOfPattern(BigInteger id) throws CoreException {
/*
    // if null pattern return empty key
    Pattern pattern = ingest.fetchOnePattern(id);
    if (Objects.isNull(pattern))
      return Key.of("");

    // if pattern has key, use that
    if (Objects.nonNull(pattern.getKey()) && !pattern.getKey().isEmpty())
      return Key.of(pattern.getKey());

    // pattern has no key; use sequence key. if null sequence return empty key
    Sequence sequence = ingest.fetchOneSequence(pattern.getProgramSequenceId());
    if (Objects.isNull(sequence))
      return Key.of("");
    return Key.of(sequence.getKey());
*/
    return Key.of("C");
  }


  /**
   Compute all possible chord markov nodes given a set of entities (e.g. of a Pattern or Audio)

   @param key    relative to which each chord's root will be computed in semitones modulo
   @param chords to compute all possible markov nodes of
   */
  private void computeAllNodes(Key key, Collection<ProgramSequenceChord> chords) {
/*
    if (chords.isEmpty()) return;

    // Forward nodes (likelihood of following node based on observation of preceding nodes)
    List<SequenceChord> forwardNodes = Lists.newArrayList(chords);
    forwardNodes.sort(ChordEntity.byPositionAscending);
    computeNodes(forwardNodeMap, key, forwardNodes);

    // Reverse nodes (likelihood of preceding node based on observation of following nodes in reverse time)
    List<SequenceChord> reverseNodes = Lists.newArrayList(chords);
    reverseNodes.sort(ChordEntity.byPositionDescending);
    computeNodes(reverseNodeMap, key, reverseNodes);
*/
  }

  /**
   Compute all possible chord markov nodes given a set of entities (e.g. of a Pattern or Audio)

   @param markovNodeMap to store computed nodes
   @param key           relative to which each chord's root will be computed in semitones modulo
   @param orderedChords to compute all possible markov nodes of -- THE ORDER IS IMPORTANT: any node's precedent state is a snapshot of the nodes preceding it.
   */
  private void computeNodes(Map<String, ChordMarkovNode> markovNodeMap, Key key, List<ProgramSequenceChord> orderedChords) {
/*
    if (orderedChords.isEmpty()) return;

    // keep a buffer of the preceding N entities
    List<ChordNode> buffer = Lists.newArrayList();

    // "beginning of pattern" marker (null bookend)
    buffer.add(new ChordNode());

    // for each chord in sequence, compute and store all possible orders preceding it.
    for (SequenceChord chord : orderedChords) {
      computeNode(buffer, markovNodeMap, key, chord);
    }

    // "end of pattern" marker (null bookend)
    addObservationToAllSubsets(markovNodeMap, buffer, new ChordNode());

    log.debug("totaled {} nodes in map, after computing nodes for {} entities", markovNodeMap.size(), orderedChords.size());
*/
  }

  /**
   Compute a possible chord markov node given an entity (e.g. of a Pattern or Audio)

   @param buffer        to keep the preceding N entities
   @param markovNodeMap to store computed nodes
   @param key           relative to which each chord's root will be computed in semitones modulo
   @param chord         to compute markov node of -- THE ORDER IS IMPORTANT: any node's precedent state is a snapshot of the nodes preceding it.
   */
  private void computeNode(List<ChordNode> buffer, Map<String, ChordMarkovNode> markovNodeMap, Key key, ProgramSequenceChord chord) {
/*
    // the observation is transposed to the key of the pattern/sequence
    ChordNode chordNode = new ChordNode(key, chord);

    // all the observation to all subsets
    addObservationToAllSubsets(markovNodeMap, buffer, chordNode);

    // add the just-added chord to the buffer; if buffer is longer than the markov order, shift items of the top until it's the right size
    buffer.add(chordNode);
    if (buffer.size() > markovOrder) buffer.remove(0);
*/
  }

  @Override
  public Map<String, ChordMarkovNode> getForwardNodeMap() {
    return forwardNodeMap;
  }

  @Override
  public Map<String, ChordMarkovNode> getReverseNodeMap() {
    return reverseNodeMap;
  }

  @Override
  public Integer getMarkovOrder() {
    return markovOrder;
  }

}

