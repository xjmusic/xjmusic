// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.digest;

import org.json.JSONObject;

/**
 [#154350346] Architect wants a universal Evaluation Factory, to modularize graph mathematics used during craft to evaluate any combination of Library, Pattern, and Instrument for any purpose.
 */
@FunctionalInterface
public interface Digest {
  String KEY_ONE = "digest";
  String KEY_MANY = "digests";
  String KEY_INSTRUMENTS = "instrumentsWithMeme";
  String KEY_MEME_USAGE = "memeUsage";
  String KEY_PATTERN_HAS_MEME = "patternHasMeme";
  String KEY_PATTERNS = "patternsWithMeme";
  String KEY_PHASE_OFFSET = "phaseOffset";
  String KEY_PHASES_WITH_MEME = "phasesWithMeme";
  String KEY_CHORD_ID = "chordId";
  String KEY_CHORD_NAME = "chordName";
  String KEY_CHORD_POSITION = "chordPosition";
  String KEY_CHORD_PROGRESSIONS = "chordProgressions";
  String KEY_OBSERVATIONS_BY_STATE = "observationsByState";
  String KEY_OBSERVATIONS = "observations";
  String KEY_CHORDS = "chords";
  String KEY_DESCRIPTOR = "descriptor";
  String KEY_PHASE_ID = "phaseId";
  String KEY_PHASE_NAME = "phaseName";
  String KEY_PHASE_TYPE = "phaseType";
  String KEY_PATTERN_ID = "patternId";
  String KEY_PATTERN_NAME = "patternName";
  String KEY_PATTERN_TYPE = "patternType";
  String KEY_INSTRUMENT_ID = "instrumentId";
  String KEY_INSTRUMENT_DESCRIPTION = "instrumentDescription";
  String KEY_INSTRUMENT_TYPE = "instrumentType";

  /**
   Yes, the order of elements in JSON arrays is preserved,
   per RFC 7159 JavaScript Object Notation (JSON) Data Interchange Format (emphasis mine).

   @return JSON object of evaluation report, probably for display in UI
   */
  JSONObject toJSONObject();

}
