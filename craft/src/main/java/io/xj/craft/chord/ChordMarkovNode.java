// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.chord;

import io.xj.core.model.chord.Chord;
import io.xj.core.util.TremendouslyRandom;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 a ChordMarkovNode object is a collection of possible outcomes and their likelihoods. It's expected that ChordMarkovNode be stored in a map keyed by the preceding state descriptor.
 a ChordMarkovNode, once stored at a particular preceding state descriptor, may receive additional observations (Chords), such that when the entire digestion process is complete, many ChordMarkovNode will contain more-than-one possible outcome, hence the neural network that learns Chord progressions from the library it is provided.
 ChordMarkovNode toJSONObject provides a view of the N-order Markov map built from the library chord progressions.
 */
public class ChordMarkovNode {
  public static final Comparator<? super ChordMarkovNode> byPopularityDescending = (Comparator<? super ChordMarkovNode>) (o1, o2) -> Integer.compare(o2.getPopularity(), o1.getPopularity());
  private final List<ChordNode> precedentState;
  private final Map<String, ChordNode> nodeMap = Maps.newConcurrentMap();

  /**
   Construct chord markov node from a list of chords, being the precedent state of all observations (possible outcomes a.k.a. chord change) to be added to this node.

   @param precedentState chord progression preceding these observations
   */
  public ChordMarkovNode(List<ChordNode> precedentState) {
    this.precedentState = ImmutableList.copyOf(precedentState);
  }

  /**
   Get the aggregate popularity, meaning the sum number of all observations at this node.

   @return popularity count
   */
  private int getPopularity() {
    int result = 0;
    for (ChordNode chordNode : nodeMap.values())
      result += chordNode.getWeight();
    return result;
  }

  /**
   @return the state (sequence of chords) that precedes the observations (possible outcomes a.k.a. chord change) of this node.
   */
  List<ChordNode> getPrecedentState() {
    return Collections.unmodifiableList(precedentState);
  }

  /**
   @return the observations (possible outcomes a.k.a. chord change) of this node.
   */
  public Collection<ChordNode> getNodes() {
    return nodeMap.values();
  }

  /**
   Perform a weighted lottery drawing. Each node's key is added to the lottery N times, where N is its weight. A key is drawn from the lottery, and that node is returned.

   @return one observation selected at random from all possible outcomes (a.k.a. chord changes) of this node.
   */
  ChordNode getRandomObservation() {
    List<String> lottery = Lists.newArrayList();
    for (Map.Entry<String, ChordNode> stringChordNodeEntry : nodeMap.entrySet()) {
      ChordNode value = stringChordNodeEntry.getValue();
      Long weight = value.getWeight();
      for (int n = 0; n < weight; n++)
        lottery.add(stringChordNodeEntry.getKey());
    }

    return nodeMap.get(lottery.get(TremendouslyRandom.zeroToLimit(lottery.size())));
  }

  /**
   Add an observation (possible outcome a.k.a. chord change) of this node.
   If the observation already exists (unique by descriptor) then add its weight to the existing observation.
   */
  public void addObservation(ChordNode node) {
    String key = node.toString();
    if (nodeMap.containsKey(key))
      nodeMap.get(key).addWeight(node);
    else
      nodeMap.put(key, node);
  }

  /**
   Get the unique descriptor for any precedent state (list of chord changes)

   @return unique descriptor
   */
  public String precedentStateDescriptor() {
    if (precedentState.isEmpty()) return "";
    List<String> pieces = Lists.newArrayList();
    precedentState.forEach(chordNode -> pieces.add(chordNode.toString()));
    return Joiner.on(Chord.SEPARATOR_DESCRIPTOR).join(pieces);
  }
}
