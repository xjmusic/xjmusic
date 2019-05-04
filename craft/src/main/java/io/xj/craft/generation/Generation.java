// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.generation;

import io.xj.core.ingest.Ingest;
import org.json.JSONObject;

public interface Generation {
  String KEY_ONE = "digest";
  String KEY_MANY = "digests";
  String KEY_CHORD_SEQUENCE = "chordProgression";
  String KEY_SUPERSEQUENCE = "supersequence";
  String KEY_SEQUENCE_ID = "sequenceId";
  String KEY_SEQUENCE_NAME = "sequenceName";
  String KEY_SEQUENCE_TYPE = "sequenceType";

  /**
   Determine type of generation, e.g. initial segment in chain, or next macro-sequence

   @return macro-craft type
   */
  GenerationType type();

  /**
   An Ingest collection of entities that this chain segment fabrication generation will ingest.
   Based on primary chain-bindings, e.g. ChainLibrary, ChainInstrument, and ChainSequence.

   @return Ingest
   */
  Ingest ingest();

  /**
   Yes, the order of elements in JSON arrays is preserved,
   per RFC 7159 JavaScript Object Notation (JSON) Data Interchange Format (emphasis mine).

   @return json object containing ORDERED ARRAY of evaluated chord progressions
   */
  JSONObject toJSONObject();
}
