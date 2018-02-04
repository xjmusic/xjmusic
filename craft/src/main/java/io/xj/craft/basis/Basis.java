// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.basis;

import io.xj.craft.ingest.Ingest;
import io.xj.core.exception.BusinessException;
import io.xj.craft.isometry.MemeIsometry;
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

import javax.sound.sampled.AudioFormat;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.Collection;
import java.util.Map;

public interface Basis {

  /**
   Output file path

   @return output file path
   */
  String outputFilePath() throws BusinessException;

  /**
   Output Audio Format

   @return output audio format
   */
  AudioFormat outputAudioFormat() throws Exception;

  /**
   Determine type of basis, e.g. initial link in chain, or next macro-pattern

   @return macro-craft type
   */
  BasisType type();

  /**
   An Ingest collection of entities that this chain link fabrication basis will ingest.
   Based on primary chain-bindings, e.g. ChainLibrary, ChainInstrument, and ChainPattern.

   @return Ingest
   */
  Ingest ingest() throws Exception;

  /**
   An Ingest collection of entities that this chain link fabrication basis will ingest.
   Based on tertiary chain-bindings, e.g. ChainLibrary, and Library from any of the original ChainInstrument or ChainPattern.

   @return Ingest
   */
  Ingest libraryIngest() throws Exception;

  /**
   The original link submitted for craft

   @return Link
   */
  Link link();

  /**
   is initial link?

   @return whether this is the initial link in a chain
   */
  Boolean isInitialLink();

  /**
   Chain id, from link

   @return chain id
   */
  BigInteger chainId();

  /**
   Chain configuration, by type
   If no chain config is found for this type, a default config is returned.
   (caches results)

   @param chainConfigType of config to fetch
   @return chain config value
   */
  ChainConfig chainConfig(ChainConfigType chainConfigType) throws Exception;

  /**
   Link begin-at timestamp

   @return begin at
   */
  Timestamp linkBeginAt();

  /**
   fetch the previous link in the chain
   (caches results)

   @return previousLink
   */
  Link previousLink() throws Exception;

  /**
   fetch the macro-type choice for the previous link in the chain

   @return macro-type link choice
   @throws Exception on failure
   */
  Choice previousMacroChoice() throws Exception;

  /**
   fetch the main-type choice for the previous link in the chain

   @return main-type link choice
   @throws Exception on failure
   */
  Choice previousMainChoice() throws Exception;

  /**
   fetch the rhythm-type choice for the previous link in the chain

   @return rhythm-type link choice
   @throws Exception on failure
   */
  Choice previousRhythmChoice() throws Exception;

  /**
   fetch all arrangements for the previous percussive choice

   @return arrangements
   @throws Exception on failure
   */
  Collection<Arrangement> previousPercussiveArrangements() throws Exception;

  /**
   fetch the macro-type choice for the current link in the chain

   @return macro-type link choice
   @throws Exception on failure
   */
  Choice currentMacroChoice() throws Exception;

  /**
   fetch the main-type choice for the current link in the chain

   @return main-type link choice
   @throws Exception on failure
   */
  Choice currentMainChoice() throws Exception;

  /**
   fetch the rhythm-type choice for the current link in the chain

   @return rhythm-type link choice
   @throws Exception on failure
   */
  Choice currentRhythmChoice() throws Exception;

  /**
   macro-type pattern phase in current link
   (caches results)

   @return phase
   @throws Exception on failure
   */
  Phase currentMacroPhase() throws Exception;

  /**
   macro-type pattern phase in previous link
   (caches results)

   @return phase
   @throws Exception on failure
   */
  Phase previousMacroNextPhase() throws Exception;

  /**
   Chain configurations
   (caches results)

   @return map of chain config type to value
   */
  Map<ChainConfigType, ChainConfig> chainConfigs() throws Exception;

  /**
   fetch all arrangements of a choice
   (caches results)

   @param choiceId to fetch arrangements for
   @return arrangements
   @throws Exception on failure
   */
  Collection<Arrangement> choiceArrangements(BigInteger choiceId) throws Exception;

  /**
   Set the cached contents of the choice arrangements array

   @param choiceId     to set content for
   @param arrangements to return for basis choice arrangements
   */
  void setChoiceArrangements(BigInteger choiceId, Collection<Arrangement> arrangements);

  /**
   Get current Chord for any position in Link.
   Defaults to returning a chord based on the link key, if nothing else is found

   @param position in link
   @return Chord
   */
  Chord chordAt(int position) throws Exception;

  /**
   Pitch for any Note, in Hz
   <p>
   [#255] Note pitch is calculated at 32-bit floating point precision, based on root note configured in environment parameters.

   @param note to get pitch for
   @return pitch of note, in Hz
   */
  Double pitch(Note note);

  /**
   Note, for any pitch in Hz

   @param pitch to get octave of
   */
  Note note(Double pitch);

  /**
   Calculate the position in seconds from the beginning of the link, for any position given in beats.
   <p>
   [#256] Velocity of Link meter (beats per minute) increases linearly from the beginning of the Link (at the previous Link's tempo) to the end of the Link (arriving at the current Link's tempo, only at its end)

   @param p in beats
   @return position in seconds
   */
  Double secondsAtPosition(double p) throws Exception;

  /**
   Fetch all memes for a given link
   (caches results)

   @param linkId to get memes for
   @return collection of link memes
   @throws Exception on failure
   */
  Collection<LinkMeme> linkMemes(BigInteger linkId) throws Exception;

  /**
   Read an Audio by id, assumed to be in the set of audio found for all picks in the link

   @param audioId to fetch
   @return Audio
   */
  Audio linkAudio(BigInteger audioId) throws Exception;

  /**
   All Audio picked for current Link

   @return audios
   @throws Exception on failure
   */
  Map<BigInteger, Audio> linkAudios() throws Exception;

  /**
   id of all audio picked for current link

   @return list of audio ids
   @throws Exception on failure
   */
  Collection<BigInteger> linkAudioIds() throws Exception;

  /**
   Fetch all chords for the current link
   (caches results)

   @return link chords
   @throws Exception on failure
   */
  Collection<LinkChord> linkChords() throws Exception;

  /**
   Fetch memes for the current link
   (results can be overridden by caching setLinkMemes, otherwise it's a db lookup)

   @return collection of link memes
   @throws Exception on failure
   */
  Collection<LinkMeme> linkMemes() throws Exception;

  /**
   Cache link memes (instead of relying on writing to db followed by reading)
   Avoid race condition causing [#153888310] During craft, instruments should be chosen based on combined memes of all chosen patterns for that link.

   @param memes memes for the current link
   */
  void setLinkMemes(Collection<LinkMeme> memes);

  /**
   Fetch all memes for the previous link
   (caches results)

   @return previous link memes
   @throws Exception on failure
   */
  Collection<LinkMeme> previousLinkMemes() throws Exception;

  /**
   Add a Pick to the in-memory store
   [#154014731] Ops wants platform to use SQL only for business state persistence, in order to improve performance.

   @param pick to add
   */
  void pick(Pick pick);

  /**
   Fetch all picks for the current link
   (caches results)

   @return link picks
   @throws Exception on failure
   */
  Collection<Pick> picks() throws Exception;

  /**
   Total length of link from beginning to end

   @return total length
   @throws Exception if unable to compute
   */
  Duration linkTotalLength() throws Exception;

  /**
   Fetch a link in a chain, by offset

   @param chainId to fetch link in
   @param offset  of link to fetch
   @return Link
   @throws Exception on failure
   */
  Link linkByOffset(BigInteger chainId, BigInteger offset) throws Exception;

  /**
   Fetch current choice of macro-type link
   (caches results)

   @return choice record
   @throws Exception on failure
   */
  Choice linkChoiceByType(BigInteger linkId, PatternType patternType) throws Exception;

  /**
   Update the original Link submitted for craft,
   cache it in the internal in-memory object, and persisted in the database

   @param link Link to replace current link, and update database with
   */
  void updateLink(Link link) throws Exception;

  /**
   Put a key-value pair into the report

   @param key   to put
   @param value to put
   */
  void report(String key, String value);

  /**
   Send the final report of craft process, as a link message
   build YAML and create Link Message
   */
  void sendReport();

  /**
   Microseconds from seconds

   @param seconds to get microseconds of
   @return microseconds
   */
  Long atMicros(Double seconds);

  /**
   Get MemeIsometry instance for previous macro-choice and phase memes

   @return MemeIsometry for previous macro-choice
   @throws Exception on failure
   */
  MemeIsometry previousMacroNextPhaseMemeIsometry() throws Exception;

  /**
   Get MemeIsometry instance for macro-choice and phase memes

   @return MemeIsometry for macro-choice
   @throws Exception on failure
   */
  MemeIsometry currentMacroMemeIsometry() throws Exception;

  /**
   Get MemeIsometry instance for current link

   @return MemeIsometry for current link
   @throws Exception on failure
   */
  MemeIsometry currentLinkMemeIsometry() throws Exception;

  /**
   Create a new Choice

   @param choice to create
   */
  Choice create(Choice choice) throws Exception;

  /**
   Create a new Arrangement

   @param arrangement to create
   */
  Arrangement create(Arrangement arrangement) throws Exception;

  /**
   Create a new LinkMeme

   @param linkMeme to create
   */
  LinkMeme create(LinkMeme linkMeme) throws Exception;

  /**
   Create a new LinkChord

   @param linkChord to create
   */
  LinkChord create(LinkChord linkChord) throws Exception;

}
