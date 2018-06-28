// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.digest.chord_markov.impl;

import io.xj.core.config.Config;
import io.xj.core.model.chord.Chord;
import io.xj.core.transport.JSON;
import io.xj.core.util.Text;
import io.xj.craft.chord.ChordMarkovNode;
import io.xj.craft.chord.ChordNode;
import io.xj.craft.digest.DigestType;
import io.xj.craft.digest.chord_markov.DigestChordMarkov;
import io.xj.craft.digest.impl.DigestImpl;
import io.xj.craft.ingest.Ingest;
import io.xj.music.Key;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.json.JSONArray;
import org.json.JSONObject;
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
 DigestChordMarkov transposes all of its observations into the Key of the sequence/pattern the entities are being observed in-- so if a Sequence in the Key of GenericChord begins with a Chord D Major, that will be considered a modulo 2 semitones delta at the beginning of the sequence. Later, when generating a super sequence, if the target sequence is in the key of G and this outcome is selected, the first chord in the generated sequence which actually be A.
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
  private static Collection<Chord> chordsOf(Iterable<? extends Chord> chords, Object parentId) {
    Collection<Chord> result = Lists.newArrayList();
    chords.forEach(chord -> {
      if (Objects.equals(parentId, chord.getParentId())) result.add(chord);
    });
    return result;
  }

  /**
   Express a single chord markov node as a JSON object, for digest reporting purposes

   @param markovNode to express
   @return JSON object
   */
  private static JSONObject toJSONObject(ChordMarkovNode markovNode) {
    JSONArray obsArr = new JSONArray();
    markovNode.getNodes().forEach(node -> obsArr.put(JSON.objectFrom(node)));
    JSONObject result = new JSONObject();
    result.put(KEY_OBSERVATIONS, obsArr);
    result.put(KEY_PRECEDENT_STATE, markovNode.precedentStateDescriptor());
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
   Put a Chord markov into the in-memory store. Keyed by precedent state (the chord leading up to these observations).
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
   Represent a node map as JSON object for reporting

   @param markovNodeMap to represent
   @return json object
   */
  private static JSONArray toJSONArray(Map<String, ChordMarkovNode> markovNodeMap) {
    JSONArray result = new JSONArray();
    List<ChordMarkovNode> nodes = Lists.newArrayList(markovNodeMap.values());
    nodes.sort(ChordMarkovNode.byPopularityDescending);
    nodes.forEach(markovNode -> result.put(toJSONObject(markovNode)));
    return result;
  }

  /**
   Digest entities from ingest
   */
  private void digest() {
    ingest.patterns().forEach(pattern ->
      computeAllNodes(ingest.patternKey(pattern.getId()),
        chordsOf(ingest.patternChords(), pattern.getId())));
  }

  /**
   Compute all possible chord markov nodes given a set of entities (e.g. from a Pattern or Audio)

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
   Compute all possible chord markov nodes given a set of entities (e.g. from a Pattern or Audio)

   @param markovNodeMap to store computed nodes
   @param key           relative to which each chord's root will be computed in semitones modulo
   @param orderedChords to compute all possible markov nodes of -- THE ORDER IS IMPORTANT: any node's precedent state is a snapshot of the nodes preceding it.
   */
  private void computeNodes(Map<String, ChordMarkovNode> markovNodeMap, Key key, List<Chord> orderedChords) {
    if (orderedChords.isEmpty()) return;

    // keep a buffer of the preceding N entities
    List<ChordNode> buffer = Lists.newArrayList();

    // "beginning of pattern" marker (null bookend)
    buffer.add(new ChordNode());

    // for each chord in sequence, compute and store all possible orders preceding it.
    for (Chord chord : orderedChords) {
      computeNode(buffer, markovNodeMap, key, chord);
    }

    // "end of pattern" marker (null bookend)
    addObservationToAllSubsets(markovNodeMap, buffer, new ChordNode());

    log.debug("totaled {} nodes in map, after computing nodes for {} entities: {}", markovNodeMap.size(), orderedChords.size(), Text.entities(orderedChords));
  }

  /**
   Compute a possible chord markov node given an entity (e.g. from a Pattern or Audio)

   @param buffer        to keep the preceding N entities
   @param markovNodeMap to store computed nodes
   @param key           relative to which each chord's root will be computed in semitones modulo
   @param chord         to compute markov node of -- THE ORDER IS IMPORTANT: any node's precedent state is a snapshot of the nodes preceding it.
   */
  private void computeNode(List<ChordNode> buffer, Map<String, ChordMarkovNode> markovNodeMap, Key key, Chord chord) {
    // the observation is transposed to the key of the pattern/sequence
    ChordNode chordNode = new ChordNode(key, chord);

    // all the observation to all subsets
    addObservationToAllSubsets(markovNodeMap, buffer, chordNode);

    // add the just-added chord to the buffer; if buffer is longer than the markov order, shift items from the top until it's the right size
    buffer.add(chordNode);
    if (buffer.size() > markovOrder) buffer.remove(0);
  }

  @Override
  public JSONObject toJSONObject() {
    JSONObject result = new JSONObject();
    result.put(KEY_OBSERVATIONS_FORWARD, toJSONArray(forwardNodeMap));
    result.put(KEY_OBSERVATIONS_REVERSE, toJSONArray(reverseNodeMap));
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

}

