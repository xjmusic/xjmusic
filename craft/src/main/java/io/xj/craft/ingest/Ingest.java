// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
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
import io.xj.core.model.sequence.Sequence;
import io.xj.core.model.sequence.SequenceType;
import io.xj.core.model.sequence_meme.SequenceMeme;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.pattern_chord.PatternChord;
import io.xj.core.model.pattern_event.PatternEvent;
import io.xj.core.model.pattern_meme.PatternMeme;
import io.xj.core.model.voice.Voice;
import io.xj.music.Key;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;

/**
 [#154350346] Architect wants a universal Ingest Factory, to modularize graph mathematics used during craft to evaluate any combination of Library, Sequence, and Instrument for any purpose.
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
   Get the internal map of id to sequence entities

   @return map of sequence id to sequence
   */
  Map<BigInteger, Sequence> sequenceMap();

  /**
   Get the internal map of id to pattern entities

   @return map of pattern id to pattern
   */
  Map<BigInteger, Pattern> patternMap();

  /**
   Get the internal map of id to instrument entities

   @return map of instrument id to instrument
   */
  Map<BigInteger, Instrument> instrumentMap();

  /**
   Get a collection of all sequences for ingest

   @return collection of sequences
   */
  Collection<Sequence> sequences();

  /**
   Get a collection of all sequences of a particular type for ingest

   @return collection of sequences
   */
  Collection<Sequence> sequences(SequenceType type);

  /**
   Get the access with which this Ingest was instantiated.

   @return access
   */
  Access access();

  /**
   Get a Sequence by id, ideally in the original entity map, and if not, from a cached read of the DAO

   @param id of sequence to read
   @return sequence
   */
  Sequence sequence(BigInteger id);

  /**
   Get a collection of all sequence memes for ingest

   @return collection of sequence memes
   */
  Collection<SequenceMeme> sequenceMemes();

  /**
   Get all Sequence Memes for a Sequence
   CACHE readAll()

   @param sequenceId to get sequence memes for
   @return sequence memes
   */
  Collection<SequenceMeme> sequenceMemes(BigInteger sequenceId);

  /**
   Fetch all memes for a given sequence and patternOffset
   (caches results)

   @param sequenceId   to get memes for
   @param patternOffset within sequence
   @param patternTypes  to match
   @return collection of sequence memes
   @throws Exception on failure
   */
  Collection<Meme> sequenceAndPatternMemes(BigInteger sequenceId, BigInteger patternOffset, PatternType... patternTypes) throws Exception;

  /**
   Get a collection of all patterns for ingest

   @return collection of patterns
   */
  Collection<Pattern> patterns();

  /**
   Get a collection of patterns for a sequence

   @param sequenceId to get patterns for
   @return collection of patterns
   */
  Collection<Pattern> patterns(BigInteger sequenceId);

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
   Selects one (at random) from all available patterns an at offset of a sequence.
   Caches the selection, so it will always return the same output for any given input.

   @param sequenceId   of pattern
   @param patternOffset within sequence
   @param patternType   to match
   @return pattern record
   @throws Exception on failure
   */
  @Nullable
  Pattern patternAtOffset(BigInteger sequenceId, BigInteger patternOffset, PatternType patternType) throws Exception;

  /**
   Selects one (at random) from all available patterns an at offset of a sequence.
   DOES NOT CACHE the selection, so it will (potentially) return a different output, given the same input.

   @param sequenceId   of pattern
   @param patternOffset within sequence
   @param patternType   to match
   @return pattern record
   @throws Exception on failure
   */
  Pattern patternRandomAtOffset(BigInteger sequenceId, BigInteger patternOffset, PatternType patternType) throws Exception;

  /**
   Fetch all patterns an at offset of a sequence
   (caches results)

   @param sequenceId   of pattern
   @param patternOffset within sequence
   @return pattern record
   @throws Exception on failure
   */
  Collection<Pattern> patternsAtOffset(BigInteger sequenceId, BigInteger patternOffset) throws Exception;

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
   Get a collection of all PatternChords for ingest

   @return collection of PatternChords
   */
  Collection<PatternChord> patternChords();

  /**
   Get a collection of PatternChord for a particular Pattern

   @param patternId to get chord for
   @return collection of PatternChord
   */
  Collection<PatternChord> patternChords(BigInteger patternId);

  /**
   Get a collection of all Voices for ingest

   @return collection of Voices
   */
  Collection<Voice> voices();

  /**
   Get a collection of Voices for a Sequence

   @param sequenceId to get voices for
   @return voices
   */
  Collection<Voice> voices(BigInteger sequenceId);

  /**
   Get a collection of all PatternEvents for ingest

   @return collection of PatternEvents
   */
  Collection<PatternEvent> patternEvents();

  /**
   Get a collection of PatternEvents from a particular pattern and voice

   @param patternId to get events of
   @param voiceId to get events of
   @return collection of PatternEvents
   */
  Collection<PatternEvent> patternVoiceEvents(BigInteger patternId, BigInteger voiceId);

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
   Get the key of any pattern-- if the pattern has no key, get the pattern of its sequence

   @param id of pattern to get key of
   @return key of pattern
   */
  Key patternKey(BigInteger id);

}
