// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.craft.digest.chord_markov.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import io.xj.core.config.Config;
import io.xj.core.model.chord.Chord;
import io.xj.core.model.chord.ChordMarkovNode;
import io.xj.core.model.chord.ChordNode;
import io.xj.core.transport.JSON;
import io.xj.craft.digest.DigestType;
import io.xj.craft.digest.chord_markov.DigestChordMarkov;
import io.xj.craft.digest.impl.DigestImpl;
import io.xj.craft.ingest.Ingest;
import io.xj.music.Key;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 In-memory cache of ingest of all chords in a library
 <p>
 [#154234716] Architect wants ingest of library contents, to modularize graph mathematics used during craft, and provide the Artist with useful insight for developing the library.
 <p>
 DigestChordMarkov transposes all of its observations into the Key of the pattern/phase the chords are being observed in-- so if a Sequence in the Key of GenericChord begins with a Chord D Major, that will be considered a modulo 2 semitones delta at the beginning of the sequence. Later, when generating a superpattern, if the target pattern is in the key of G and this outcome is selected, the first chord in the generated sequence which actually be A.
 */
public class DigestChordMarkovImpl extends DigestImpl implements DigestChordMarkov {
  private final Map<String, ChordMarkovNode> forwardNodeMap = Maps.newConcurrentMap();
  private final Map<String, ChordMarkovNode> reverseNodeMap = Maps.newConcurrentMap();

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
    try {
      digest();
    } catch (Exception e) {
      Logger log = LoggerFactory.getLogger(DigestChordMarkovImpl.class);
      log.error("Failed to digest chords of ingest {}", ingest, e);
    }
  }

  /**
   Express a single chord markov node as a JSON object, for digest reporting purposes

   @param node to express
   @return JSON object
   */
  private static JSONObject toJSONObject(ChordMarkovNode node) {
    JSONArray obsArr = new JSONArray();
    node.getNodeMap().forEach(observation -> obsArr.put(JSON.objectFrom(observation)));
    JSONObject result = new JSONObject();
    result.put(KEY_OBSERVATIONS, obsArr);
    return result;
  }

  /**
   Get only the chords of a particular parent (probably phase chords in a parent phase)

   @param chords   to search for chords
   @param parentId (phase) to get phase chords of
   @return collection of audio chords
   */
  private static Collection<Chord> chordsOf(Collection<? extends Chord> chords, Object parentId) {
    Collection<Chord> result = Lists.newArrayList();
    chords.forEach(chord -> {
      if (Objects.equals(parentId, chord.getParentId())) result.add(chord);
    });
    return result;
  }

  @Override
  public JSONObject toJSONObject() {
    JSONObject result = new JSONObject();
    result.put(KEY_OBSERVATIONS_FORWARD, toJSONObject(forwardNodeMap));
    result.put(KEY_OBSERVATIONS_REVERSE, toJSONObject(reverseNodeMap));
    return result;
  }

  /**
   Represent a node map as JSON object for reporting

   @param nodeMap to represent
   @return json object
   */
  private JSONObject toJSONObject(Map<String, ChordMarkovNode> nodeMap) {
    JSONObject result = new JSONObject();
    nodeMap.forEach((key, markovNode) -> result.put(key, toJSONObject(markovNode)));
    return result;
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

  /**
   Digest entities from ingest
   */
  private void digest() {
    ingest.phases().forEach(phase ->
      computeAllNodes(ingest.phaseKey(phase.getId()),
        chordsOf(ingest.phaseChords(), phase.getId())));
  }

  /**
   Compute all possible chord markov nodes given a set of chords (e.g. from a Phase or Audio)

   @param key    relative to which each chord's root will be computed in semitones modulo
   @param chords to compute all possible markov nodes of
   */
  private void computeAllNodes(Key key, Collection<Chord> chords) {
    if (chords.isEmpty()) return;

    // Forward nodes (likelihood of following node based on observation of preceding nodes)
    List<Chord> forwardNodes = Lists.newArrayList(chords);
    forwardNodes.sort(Chord.byPositionAscending);
    computeNodes(forwardNodeMap, key, forwardNodes);

    // Reverse nodes (likelihood of preceding node based on observation of following nodes in reverse time)
    List<Chord> reverseNodes = Lists.newArrayList(chords);
    reverseNodes.sort(Chord.byPositionDescending);
    computeNodes(reverseNodeMap, key, reverseNodes);
  }

  /**
   Compute all possible chord markov nodes given a set of chords (e.g. from a Phase or Audio)

   @param key           relative to which each chord's root will be computed in semitones modulo
   @param orderedChords to compute all possible markov nodes of -- THE ORDER IS IMPORTANT: any node's precedent state is a snapshot of the nodes preceding it.
   */
  private void computeNodes(Map<String, ChordMarkovNode> nodeMap, Key key, List<Chord> orderedChords) {
    if (orderedChords.isEmpty()) return;

    // keep a buffer of the preceding N chords
    List<ChordNode> buffer = Lists.newArrayList();

    // "beginning of phase" marker (null bookend)
    buffer.add(new ChordNode());

    // for each chord in sequence, compute and store all possible orders preceding it.
    for (Chord chord : orderedChords) {

      // the observation is transposed to the key of the phase/pattern
      ChordNode chordNode = new ChordNode(key, chord);

      // all the observation to all subsets
      addObservationToAllSubsets(nodeMap, buffer, chordNode);

      // add the just-added chord to the buffer; if buffer is longer than the markov order, shift items from the top until it's the right size
      buffer.add(chordNode);
      if (buffer.size() > markovOrder) buffer.remove(0);
    }

    // "end of phase" marker (null bookend)
    addObservationToAllSubsets(nodeMap, buffer, new ChordNode());
  }


  /**
   Add an observation to all subsets of a precedent state (a 3-order chain requires 3 passes, the 1-order pass, 2-order pass, and 3-order pass)
   */
  private void addObservationToAllSubsets(Map<String, ChordMarkovNode> nodeMap, List<ChordNode> fromBuffer, ChordNode observation) {
    List<ChordNode> buffer = Lists.newArrayList(fromBuffer);
    boolean more = !buffer.isEmpty();
    while (more) {
      addObservation(nodeMap, buffer, observation);
      buffer.remove(0);
      more = !buffer.isEmpty();
    }
  }

  /**
   Put a Chord markov into the in-memory store. Keyed by precedent state (the chord leading up to these observations).
   If no observations have been added with the given precedent state, a new chord markov node will be instantiated.

   @param precedentState the chord progression preceding this observation
   @param observation    observation to add.
   */
  private void addObservation(Map<String, ChordMarkovNode> nodeMap, List<ChordNode> precedentState, ChordNode observation) {
    String key = new ChordMarkovNode(precedentState).precedentStateDescriptor();
    if (!nodeMap.containsKey(key))
      nodeMap.put(key, new ChordMarkovNode(precedentState));

    nodeMap.get(key).addObservation(observation);
  }

}

