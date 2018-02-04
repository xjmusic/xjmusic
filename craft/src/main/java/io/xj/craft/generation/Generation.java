// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.generation;

import io.xj.craft.ingest.Ingest;
import org.json.JSONObject;

public interface Generation {
  String KEY_ONE = "digest";
  String KEY_MANY = "digests";
  String KEY_AUDIO_ID = "audioId";
  String KEY_AUDIO_NAME = "audioName";
  String KEY_CHORD_ID = "chordId";
  String KEY_CHORD_NAME = "chordName";
  String KEY_CHORD_POSITION = "chordPosition";
  String KEY_CHORD_SEQUENCE = "chordProgression";
  String KEY_PHASE_ID = "phaseId";
  String KEY_PHASE_NAME = "phaseName";
  String KEY_PHASE_TYPE = "phaseType";
  String KEY_CHORD_SEQUENCE_TYPE = "chordSequenceType";
  String KEY_SUPERPATTERN = "superpattern";
  String KEY_PATTERN_ID = "patternId";
  String KEY_PATTERN_NAME = "patternName";
  String KEY_PATTERN_TYPE = "patternType";
  String KEY_INSTRUMENT_ID = "instrumentId";
  String KEY_INSTRUMENT_DESCRIPTION = "instrumentDescription";
  String KEY_INSTRUMENT_TYPE = "instrumentType";

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


  JSONObject toJSONObject();
}
