// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.evaluation;

import io.xj.core.model.audio.Audio;
import io.xj.core.model.audio_chord.AudioChord;
import io.xj.core.model.audio_event.AudioEvent;
import io.xj.core.model.chord.ChordSequence;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.instrument_meme.InstrumentMeme;
import io.xj.core.model.library.Library;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.pattern_meme.PatternMeme;
import io.xj.core.model.phase.Phase;
import io.xj.core.model.phase_chord.PhaseChord;
import io.xj.core.model.phase_event.PhaseEvent;
import io.xj.core.model.phase_meme.PhaseMeme;
import io.xj.core.model.voice.Voice;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;

public interface Evaluation {
  String KEY_ONE = "evaluation";
  String KEY_MANY = "evaluations";


  /**
   Get the internal map of id to pattern entities

   @return map of pattern id to pattern
   */
  Map<BigInteger, Pattern> getPatternMap();

  /**
   Get the internal map of id to phase entities

   @return map of phase id to phase
   */
  Map<BigInteger, Phase> getPhaseMap();

  /**
   Get the internal map of id to instrument entities

   @return map of instrument id to instrument
   */
  Map<BigInteger, Instrument> getInstrumentMap();

  /**
   Get a collection of all patterns for evaluation

   @return collection of patterns
   */
  Collection<Pattern> getPatterns();

  /**
   Get a collection of all pattern memes for evaluation

   @return collection of pattern memes
   */
  Collection<PatternMeme> getPatternMemes();

  /**
   Get a collection of all phases for evaluation

   @return collection of phases
   */
  Collection<Phase> getPhases();

  /**
   Get a collection of all phase memes for evaluation

   @return collection of phase memes
   */
  Collection<PhaseMeme> getPhaseMemes();

  /**
   Get a collection of all instruments for evaluation

   @return collection of instruments
   */
  Collection<Instrument> getInstruments();

  /**
   Get a collection of all instrument memes for evaluation

   @return collection of instrument memes
   */
  Collection<InstrumentMeme> getInstrumentMemes();


  /**
   Get a collection of all Libraries for evaluation

   @return collection of Libraries
   */
  Collection<Library> getLibraries();

  /**
   Get a collection of all Audios for evaluation

   @return collection of Audios
   */
  Collection<Audio> getAudios();

  /**
   Get a collection of all AudioChords for evaluation

   @return collection of AudioChords
   */
  Collection<AudioChord> getAudioChords();

  /**
   Get a collection of all AudioEvents for evaluation

   @return collection of AudioEvents
   */
  Collection<AudioEvent> getAudioEvents();

  /**
   Get a collection of all PhaseChords for evaluation

   @return collection of PhaseChords
   */
  Collection<PhaseChord> getPhaseChords();

  /**
   Get a collection of all Voices for evaluation

   @return collection of Voices
   */
  Collection<Voice> getVoices();

  /**
   Get a collection of all PhaseEvents for evaluation

   @return collection of PhaseEvents
   */
  Collection<PhaseEvent> getPhaseEvents();

  /**
   Get a collection of all chord sequences iterated from all available phase or audio chords for evaluation

   @return collection of chord sequences
   */
  Collection<ChordSequence> getChordSequences();

  /**
   Get the internal map of id to audio entities

   @return map of audio id to audio
   */
  Map<BigInteger, Audio> getAudioMap();
}
