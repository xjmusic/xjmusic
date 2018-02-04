// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.craft.ingest;

import io.xj.core.access.impl.Access;
import io.xj.core.model.audio.Audio;
import io.xj.core.model.audio_chord.AudioChord;
import io.xj.core.model.audio_event.AudioEvent;
import io.xj.core.model.entity.Entity;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.instrument_meme.InstrumentMeme;
import io.xj.core.model.library.Library;
import io.xj.core.model.meme.Meme;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.pattern_meme.PatternMeme;
import io.xj.core.model.phase.Phase;
import io.xj.core.model.phase.PhaseType;
import io.xj.core.model.phase_chord.PhaseChord;
import io.xj.core.model.phase_event.PhaseEvent;
import io.xj.core.model.phase_meme.PhaseMeme;
import io.xj.core.model.voice.Voice;
import io.xj.music.Key;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;

/**
 [#154350346] Architect wants a universal Ingest Factory, to modularize graph mathematics used during craft to evaluate any combination of Library, Pattern, and Instrument for any purpose.
 <p>
 # Component
 <p>
 - **Ingest**
 - **Digest**
 <p>
 # Entity
 <p>
 - **Assembly**
 - **Catalog**
 - **Census**
 - **Hash**
 - **Inventory**
 - **Sum**
 - **Summary**
 <p>
 # Action
 <p>
 - **Absorb** (to take up; to drink in; to penetrate into the material) / **Adsorb** (to attract and bind so as to form a thin layer on the surface)
 - **Aggregate**
 - **Consider**
 - **Deem**
 - **Ingest** (to take in) / **Digest** (to distribute or arrange methodically; to work over and
 classify; to reduce to portions for ready use or
 application)
 - **Extract**
 - **Fetch**
 - **Gather**
 - **Glean**
 - **Induce** (to lead in; to introduce) / **Deduce** (to take away; to deduct; to subtract; as, to deduce a part from the whole)
 - **Infer**
 - **Obtain**
 - **Place**
 - **Rank**
 - **Reckon**
 - **Summon**
 <p>
 -ize:
 <p>
 - **Generalize**
 - **Hypothesize**
 - **Summarize**
 - **Synthesize**
 - **Theorize**
 */
public interface Ingest {
  String KEY_ONE = "ingest";
  String KEY_MANY = "evaluations";

  /**
   Get the internal map of id to pattern entities

   @return map of pattern id to pattern
   */
  Map<BigInteger, Pattern> patternMap();

  /**
   Get the internal map of id to phase entities

   @return map of phase id to phase
   */
  Map<BigInteger, Phase> phaseMap();

  /**
   Get the internal map of id to instrument entities

   @return map of instrument id to instrument
   */
  Map<BigInteger, Instrument> instrumentMap();

  /**
   Get a collection of all patterns for ingest

   @return collection of patterns
   */
  Collection<Pattern> patterns();

  /**
   Get a collection of all patterns of a particular type for ingest

   @return collection of patterns
   */
  Collection<Pattern> patterns(PatternType type);

  /**
   Get the access with which this Ingest was instantiated.

   @return access
   */
  Access access();

  /**
   Get a Pattern by id, ideally in the original entity map, and if not, from a cached read of the DAO

   @param id of pattern to read
   @return pattern
   */
  Pattern pattern(BigInteger id);

  /**
   Get a collection of all pattern memes for ingest

   @return collection of pattern memes
   */
  Collection<PatternMeme> patternMemes();

  /**
   Get all Pattern Memes for a Pattern
   CACHE readAll()

   @param patternId to get pattern memes for
   @return pattern memes
   */
  Collection<PatternMeme> patternMemes(BigInteger patternId);

  /**
   Fetch all memes for a given pattern and phaseOffset
   (caches results)

   @param patternId   to get memes for
   @param phaseOffset within pattern
   @param phaseTypes  to match
   @return collection of pattern memes
   @throws Exception on failure
   */
  Collection<Meme> patternAndPhaseMemes(BigInteger patternId, BigInteger phaseOffset, PhaseType... phaseTypes) throws Exception;

  /**
   Get a collection of all phases for ingest

   @return collection of phases
   */
  Collection<Phase> phases();

  /**
   Get a collection of phases for a pattern

   @param patternId to get phases for
   @return collection of phases
   */
  Collection<Phase> phases(BigInteger patternId);

  /**
   Get a collection of all phase memes for ingest

   @return collection of phase memes
   */
  Collection<PhaseMeme> phaseMemes();

  /**
   Get all Phase Memes for a Phase
   CACHE readAll()

   @param phaseId to get phase memes for
   @return phase memes
   */
  Collection<PhaseMeme> phaseMemes(BigInteger phaseId);

  /**
   Selects one (at random) from all available phases an at offset of a pattern.
   Caches the selection, so it will always return the same output for any given input.

   @param patternId   of phase
   @param phaseOffset within pattern
   @param phaseType   to match
   @return phase record
   @throws Exception on failure
   */
  @Nullable
  Phase phaseAtOffset(BigInteger patternId, BigInteger phaseOffset, PhaseType phaseType) throws Exception;

  /**
   Selects one (at random) from all available phases an at offset of a pattern.
   DOES NOT CACHE the selection, so it will (potentially) return a different output, given the same input.

   @param patternId   of phase
   @param phaseOffset within pattern
   @param phaseType   to match
   @return phase record
   @throws Exception on failure
   */
  Phase phaseRandomAtOffset(BigInteger patternId, BigInteger phaseOffset, PhaseType phaseType) throws Exception;

  /**
   Fetch all phases an at offset of a pattern
   (caches results)

   @param patternId   of phase
   @param phaseOffset within pattern
   @return phase record
   @throws Exception on failure
   */
  Collection<Phase> phasesAtOffset(BigInteger patternId, BigInteger phaseOffset) throws Exception;

  /**
   Get a collection of all instruments for ingest

   @return collection of instruments
   */
  Collection<Instrument> instruments();

  /**
   Get a collection of all instruments of a particular type for ingest

   @return collection of instruments
   */
  Collection<Instrument> instruments(InstrumentType type);

  /**
   Get a collection of all instrument memes for ingest

   @return collection of instrument memes
   */
  Collection<InstrumentMeme> instrumentMemes();

  /**
   Get all Instrument Memes for a Instrument

   @param instrumentId to get instrument memes for
   @return instrument memes
   */
  Collection<InstrumentMeme> instrumentMemes(BigInteger instrumentId);

  /**
   Get a collection of all Libraries for ingest

   @return collection of Libraries
   */
  Collection<Library> libraries();

  /**
   Get a collection of all Audios for ingest

   @return collection of Audios
   */
  Collection<Audio> audios();

  /**
   Get a collection of Audios for a Instrument

   @param instrumentId to get audios for
   @return audios
   */
  Collection<Audio> audios(BigInteger instrumentId);

  /**
   Get a collection of all AudioChords for ingest

   @return collection of AudioChords
   */
  Collection<AudioChord> audioChords();

  /**
   Get a collection of all AudioEvents for ingest

   @return collection of AudioEvents
   */
  Collection<AudioEvent> audioEvents();

  /**
   Get a collection of all PhaseChords for ingest

   @return collection of PhaseChords
   */
  Collection<PhaseChord> phaseChords();

  /**
   Get a collection of PhaseChord for a particular Phase

   @param phaseId to get chord for
   @return collection of PhaseChord
   */
  Collection<PhaseChord> phaseChords(BigInteger phaseId);

  /**
   Get a collection of all Voices for ingest

   @return collection of Voices
   */
  Collection<Voice> voices();

  /**
   Get a collection of Voices for a Pattern

   @param patternId to get voices for
   @return voices
   */
  Collection<Voice> voices(BigInteger patternId);

  /**
   Get a collection of all PhaseEvents for ingest

   @return collection of PhaseEvents
   */
  Collection<PhaseEvent> phaseEvents();

  /**
   Get a collection of PhaseEvents from a particular phase and voice

   @param phaseId to get events of
   @param voiceId to get events of
   @return collection of PhaseEvents
   */
  Collection<PhaseEvent> phaseVoiceEvents(BigInteger phaseId, BigInteger voiceId);

  /**
   Get the internal map of id to audio entities

   @return map of audio id to audio
   */
  Map<BigInteger, Audio> audioMap();

  /**
   Get a collection of all entities

   @return collection of all entities
   */
  Collection<Entity> all();

  /**
   Get a Instrument by id, ideally in the original entity map, and if not, from a cached read of the DAO

   @param id of instrument to read
   @return instrument
   */
  Instrument instrument(BigInteger id);

  /**
   Read all AudioEvent that are first in an audio, for all audio in an Instrument

   @param instrumentId to get audio for
   @return audio events
   @throws Exception on failure
   */
  Collection<AudioEvent> instrumentAudioFirstEvents(BigInteger instrumentId) throws Exception;

  /**
   Get a Audio by id, ideally in the original entity map, and if not, from a cached read of the DAO

   @param id of audio to read
   @return audio
   */
  Audio audio(BigInteger id);

  /**
   Get the key of any phase-- if the phase has no key, get the phase of its pattern

   @param id of phase to get key of
   @return key of phase
   */
  Key phaseKey(BigInteger id);

}
