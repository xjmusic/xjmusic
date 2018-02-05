// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.generation;

import io.xj.craft.ingest.Ingest;
import org.json.JSONObject;

public interface Generation {
  String KEY_ONE = "digest";
  String KEY_MANY = "digests";
  String KEY_CHORD_SEQUENCE = "chordProgression";
  String KEY_SUPERPATTERN = "superpattern";
  String KEY_PATTERN_ID = "patternId";
  String KEY_PATTERN_NAME = "patternName";
  String KEY_PATTERN_TYPE = "patternType";

  /**
   Determine type of generation, e.g. initial link in chain, or next macro-pattern

   @return macro-craft type
   */
  GenerationType type();

  /**
   An Ingest collection of entities that this chain link fabrication generation will ingest.
   Based on primary chain-bindings, e.g. ChainLibrary, ChainInstrument, and ChainPattern.

   @return Ingest
   */
  Ingest ingest() throws Exception;

  /**
   Yes, the order of elements in JSON arrays is preserved,
   per RFC 7159 JavaScript Object Notation (JSON) Data Interchange Format (emphasis mine).

   @return json object containing ORDERED ARRAY of evaluated chord progressions
   */
  JSONObject toJSONObject();
}
