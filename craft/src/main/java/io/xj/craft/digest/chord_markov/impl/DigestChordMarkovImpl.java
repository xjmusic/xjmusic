// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.digest.chord_markov.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.core.config.Config;
import io.xj.core.exception.CoreException;
import io.xj.core.ingest.Ingest;
import io.xj.core.model.chord.Sort;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.pattern_chord.PatternChord;
import io.xj.craft.chord.ChordMarkovNode;
import io.xj.craft.chord.ChordNode;
import io.xj.craft.digest.DigestType;
import io.xj.craft.digest.chord_markov.DigestChordMarkov;
import io.xj.craft.digest.impl.DigestImpl;
import io.xj.craft.exception.CraftException;
import io.xj.music.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 In-memory cache of ingest of all entities in a library
 <p>
 [#154234716] Architect wants ingest of library contents, to modularize graph mathematics used during craft, and provide the Artist with useful insight for developing the library.
 <p>
 DigestChordMarkov transposes all of its observations into the Key of the sequence/pattern the entities are being observed in-- so if a Sequence in the Key of GenericChord begins with a PatternChord D Major, that will be considered a modulo 2 semitones delta at the beginning of the sequence. Later, when generating a super sequence, if the target sequence is in the key of G and this outcome is selected, the first chord in the generated sequence which actually be A.
 */
public class DigestChordMarkovImpl extends DigestImpl implements DigestChordMarkov {
  private final Map<String, ChordMarkovNode> forwardNodeMap = Maps.newConcurrentMap();
  private final Map<String, ChordMarkovNode> reverseNodeMap = Maps.newConcurrentMap();
  private final Logger log = LoggerFactory.getLogger(DigestChordMarkovImpl.class);
  private final Integer markovOrder = Config.chordMarkovOrder();

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
      log.error("Failed to digest entities of ingest {}", ingest, e);
    }
  }

  /**
   Get only the entities of a particular parent (probably pattern entities in a parent pattern)

   @param chords   to search for entities
   @param parentId (pattern) to get pattern entities of
   @return collection of audio entities
   */
  private static Collection<PatternChord> chordsOf(Iterable<? extends PatternChord> chords, Object parentId) {
    Collection<PatternChord> result = Lists.newArrayList();
    chords.forEach(patternChord -> {
      if (Objects.equals(parentId, patternChord.getParentId())) result.add(patternChord);
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
   Put a PatternChord markov into the in-memory store. Keyed by precedent state (the chord leading up to these observations).
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
   Digest entities from ingest
   */
  private void digest() throws CraftException {
    for (Pattern pattern : ingest.getAllPatterns()) {
      try {
        computeAllNodes(ingest.getKeyOfPattern(pattern.getId()),
          chordsOf(ingest.getAllPatternChords(), pattern.getId()));

      } catch (CoreException e) {
        throw exception("Could not compute nodes", e);
      }
    }
  }

  /**
   Compute all possible chord markov nodes given a set of entities (e.g. from a Pattern or Audio)

   @param key    relative to which each chord's root will be computed in semitones modulo
   @param chords to compute all possible markov nodes of
   */
  private void computeAllNodes(Key key, Collection<PatternChord> chords) {
    if (chords.isEmpty()) return;

    // Forward nodes (likelihood of following node based on observation of preceding nodes)
    List<PatternChord> forwardNodes = Lists.newArrayList(chords);
    forwardNodes.sort(Sort.byPositionAscending);
    computeNodes(forwardNodeMap, key, forwardNodes);

    // Reverse nodes (likelihood of preceding node based on observation of following nodes in reverse time)
    List<PatternChord> reverseNodes = Lists.newArrayList(chords);
    reverseNodes.sort(Sort.byPositionDescending);
    computeNodes(reverseNodeMap, key, reverseNodes);
  }

  /**
   Compute all possible chord markov nodes given a set of entities (e.g. from a Pattern or Audio)

   @param markovNodeMap to store computed nodes
   @param key           relative to which each chord's root will be computed in semitones modulo
   @param orderedChords to compute all possible markov nodes of -- THE ORDER IS IMPORTANT: any node's precedent state is a snapshot of the nodes preceding it.
   */
  private void computeNodes(Map<String, ChordMarkovNode> markovNodeMap, Key key, List<PatternChord> orderedChords) {
    if (orderedChords.isEmpty()) return;

    // keep a buffer of the preceding N entities
    List<ChordNode> buffer = Lists.newArrayList();

    // "beginning of pattern" marker (null bookend)
    buffer.add(new ChordNode());

    // for each chord in sequence, compute and store all possible orders preceding it.
    for (PatternChord chord : orderedChords) {
      computeNode(buffer, markovNodeMap, key, chord);
    }

    // "end of pattern" marker (null bookend)
    addObservationToAllSubsets(markovNodeMap, buffer, new ChordNode());

    log.debug("totaled {} nodes in map, after computing nodes for {} entities", markovNodeMap.size(), orderedChords.size());
  }

  /**
   Compute a possible chord markov node given an entity (e.g. from a Pattern or Audio)

   @param buffer        to keep the preceding N entities
   @param markovNodeMap to store computed nodes
   @param key           relative to which each chord's root will be computed in semitones modulo
   @param chord         to compute markov node of -- THE ORDER IS IMPORTANT: any node's precedent state is a snapshot of the nodes preceding it.
   */
  private void computeNode(List<ChordNode> buffer, Map<String, ChordMarkovNode> markovNodeMap, Key key, PatternChord chord) {
    // the observation is transposed to the key of the pattern/sequence
    ChordNode chordNode = new ChordNode(key, chord);

    // all the observation to all subsets
    addObservationToAllSubsets(markovNodeMap, buffer, chordNode);

    // add the just-added chord to the buffer; if buffer is longer than the markov order, shift items from the top until it's the right size
    buffer.add(chordNode);
    if (buffer.size() > markovOrder) buffer.remove(0);
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

