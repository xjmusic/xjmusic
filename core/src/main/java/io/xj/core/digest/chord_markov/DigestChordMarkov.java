// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.digest.chord_markov;

import io.xj.core.digest.Digest;
import io.xj.core.model.chord.ChordMarkovNode;

import java.util.Map;

public interface DigestChordMarkov extends Digest {

  Map<String, ChordMarkovNode> getChordMarkovNodeMap();

  Integer getMarkovOrder();
}
