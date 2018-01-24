// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.generation;

import io.xj.core.evaluation.Evaluation;
import io.xj.core.exception.BusinessException;
import io.xj.core.isometry.MemeIsometry;
import io.xj.core.model.arrangement.Arrangement;
import io.xj.core.model.audio.Audio;
import io.xj.core.model.chain_config.ChainConfig;
import io.xj.core.model.chain_config.ChainConfigType;
import io.xj.core.model.choice.Choice;
import io.xj.core.model.link.Link;
import io.xj.core.model.link_chord.LinkChord;
import io.xj.core.model.link_meme.LinkMeme;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.phase.Phase;
import io.xj.core.model.pick.Pick;
import io.xj.music.Chord;
import io.xj.music.Note;
import org.json.JSONObject;

import javax.sound.sampled.AudioFormat;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.Collection;
import java.util.Map;

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
   An Evaluation collection of entities that this chain link fabrication generation will ingest.
   Based on primary chain-bindings, e.g. ChainLibrary, ChainInstrument, and ChainPattern.

   @return Evaluation
   */
  Evaluation evaluation() throws Exception;


  JSONObject toJSONObject();
}
