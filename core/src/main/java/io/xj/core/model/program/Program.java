//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model.program;

import io.xj.core.exception.CoreException;
import io.xj.core.model.entity.MemeEntity;
import io.xj.core.model.entity.SubEntity;
import io.xj.core.model.entity.SuperEntity;
import io.xj.core.model.program.impl.ProgramContent;
import io.xj.core.model.program.sub.Event;
import io.xj.core.model.program.sub.Pattern;
import io.xj.core.model.program.sub.ProgramMeme;
import io.xj.core.model.program.sub.Sequence;
import io.xj.core.model.program.sub.SequenceBinding;
import io.xj.core.model.program.sub.SequenceBindingMeme;
import io.xj.core.model.program.sub.SequenceChord;
import io.xj.core.model.program.sub.Voice;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface Program extends SuperEntity {
  /**
   Add an Pattern to Program
   + If there are any exceptions, store them in the SuperEntity errors

   @param pattern to add
   @return Pattern with newly added unique id
   */
  Pattern add(Pattern pattern);

  /**
   Add a Event to Program
   + If there are any exceptions, store them in the SuperEntity errors

   @param event to add
   @return Event with newly added unique id
   */
  Event add(Event event);

  /**
   Add a ProgramMeme to Program
   + If there are any exceptions, store them in the SuperEntity errors

   @param meme to add
   @return ProgramMeme with newly added unique id
   */
  ProgramMeme add(ProgramMeme meme);

  /**
   Add a Sequence to Program
   + If there are any exceptions, store them in the SuperEntity errors

   @param sequence to add
   @return Sequence with newly added unique id
   */
  Sequence add(Sequence sequence);

  /**
   Add a Sequence Binding to Program
   + If there are any exceptions, store them in the SuperEntity errors

   @param sequenceBinding to add
   @return SequenceBinding with newly added unique id
   */
  SequenceBinding add(SequenceBinding sequenceBinding);

  /**
   Add a Sequence Binding MemeEntity to Program
   + If there are any exceptions, store them in the SuperEntity errors

   @param sequenceBindingMeme to add
   @return SequenceBindingMeme with newly added unique id
   */
  SequenceBindingMeme add(SequenceBindingMeme sequenceBindingMeme);

  /**
   Add a SequenceChord to Program
   + If there are any exceptions, store them in the SuperEntity errors

   @param chord to add
   @return SequenceChord with newly added unique id
   */
  SequenceChord add(SequenceChord chord);

  /**
   Add an Voice to Program
   + If there are any exceptions, store them in the SuperEntity errors

   @param voice to add
   @return Voice with newly added unique id
   */
  Voice add(Voice voice);

  /**
   Get all entities

   @return collection of entities
   */
  Collection<SubEntity> getAllSubEntities();

  /**
   Get all available sequence pattern offsets of a given sequence

   @param sequenceBinding to get available sequence pattern offsets for
   @return collection of available sequence pattern offsets
   */
  Collection<Long> getAvailableOffsets(SequenceBinding sequenceBinding);

  /**
   Get all Chords for a given sequence

   @param sequence to get chords for
   @return chords of given sequence
   */
  Collection<SequenceChord> getChords(Sequence sequence);

  /**
   Convenience method to get all SequenceChords for a given Sequence, by sequence UUID

   @param sequenceId to get all SequenceChords for
   @return Sequence Chords
   */
  Collection<SequenceChord> getChordsOfSequence(UUID sequenceId);

  /**
   [#162361525] read content of Program
   */
  ProgramContent getContent();

  /**
   Get (computed) density of program
   Program doesn't actually have density property; it's computed by averaging the density of all its sub entities

   @return density
   */
  Double getDensity();

  /**
   Get events for a specified pattern

   @param pattern to get events for
   @return events
   */
  Collection<Event> getEventsForPattern(Pattern pattern);

  /**
   Get key for the program

   @return key
   */
  String getKey();

  /**
   Get Library ID

   @return library id
   */
  BigInteger getLibraryId();

  /**
   Get Memes

   @return Memes
   */
  Collection<ProgramMeme> getMemes();

  /**
   Get all Sequence Binding Memes for a given Sequence Binding

   @param sequenceBinding to get memes for
   @return sequence binding memes
   */
  Collection<SequenceBindingMeme> getMemes(SequenceBinding sequenceBinding);

  /**
   Fetch all memes for a given program at sequence binding offset 0

   @return collection of sequence memes
   @throws CoreException on failure
   */
  Collection<MemeEntity> getMemesAtBeginning() throws CoreException;

  /**
   Get name for the program

   @return name
   */
  String getName();

  /**
   Get a Pattern by its UUID

   @param id of pattern to get
   @return Pattern
   @throws CoreException if no pattern is found with that id
   */
  Pattern getPattern(UUID id) throws CoreException;

  /**
   Get PatternEvents

   @return PatternEvents
   */
  Collection<Event> getPatternEvents();

  /**
   Get Patterns

   @return Patterns
   */
  Collection<Pattern> getPatterns();

  /**
   Get a sequence by its UUID

   @param id to get
   @return Sequence
   */
  Sequence getSequence(UUID id) throws CoreException;

  /**
   Get Sequence Binding Memes

   @return Sequence Binding Memes
   */
  Collection<SequenceBindingMeme> getSequenceBindingMemes();

  /**
   Get Sequence Bindings

   @return Sequence Bindings
   */
  Collection<SequenceBinding> getSequenceBindings();

  /**
   Get sequence bindings at a specified offset

   @param offset to get sequence bindings at
   @return sequence bindings at offset
   */
  Collection<SequenceBinding> getSequenceBindingsAtOffset(Long offset);

  /**
   Get Chords

   @return Chords
   */
  Collection<SequenceChord> getSequenceChords();

  /**
   Get Sequences

   @return Sequences
   */
  Collection<Sequence> getSequences();

  /**
   Get state of Program

   @return state
   */
  ProgramState getState();

  /**
   Get tempo of program

   @return tempo
   */
  Double getTempo();

  /**
   Get Type

   @return Type
   */
  ProgramType getType();

  /**
   Get User

   @return User
   */
  BigInteger getUserId();

  /**
   Get Voices

   @return Voices
   */
  Collection<Voice> getVoices();

  /**
   [#165954619] Selects one (at random) from all available patterns of a given type within a sequence.
   <p>
   Caches the selection, so it will always return the same output for any given input.
   <p>
   [#166481918] Rhythm fabrication composited from layered Patterns


   @return Pattern model, or null if no pattern of this type is found
   @throws CoreException on failure
   @param sequence    from which to select
   @param voice       from which to select
   @param patternType to select
   */
  Optional<Pattern> randomlySelectPatternOfSequenceByVoiceAndType(Sequence sequence, Voice voice, PatternType patternType) throws CoreException;

  /**
   Randomly select any sequence

   @return ranomly selected sequence
   @throws CoreException if failure to select a sequence
   */
  Sequence randomlySelectSequence() throws CoreException;

  /**
   Randomly select any sequence binding at the given offset

   @param macroPatternOffset to get sequence binding at
   @return randomly selected sequence binding
   @throws CoreException on failure to select a sequence binding
   */
  SequenceBinding randomlySelectSequenceBindingAtOffset(Long macroPatternOffset) throws CoreException;

  /**
   [#162361525] persist Program content as JSON
   [#166132897] ProgramContent POJO via gson only (no JSONObject)

   @return Program for chaining methods
   */
  Program setContent(String json) throws CoreException;

  /**
   Set all content of program, cloned from another source program, with all new UUID, preserving relationships.

   @param from program
   @return this Program (for chaining methods)
   */
  Program setContentCloned(Program from) throws CoreException;

  /**
   Set created at time

   @param createdAt time
   @return entity
   */
  Program setCreatedAt(String createdAt);

  /**
   Set created at time

   @param createdAt time
   @return entity
   */
  Program setCreatedAtInstant(Instant createdAt);

  /**
   Set the density for the program

   @param density to set
   @return this Program (for chaining methods)
   */
  Program setDensity(String density);

  /**
   Set the key for the program

   @param key to set
   @return this Program (for chaining methods)
   */
  Program setKey(String key);

  /**
   Set Library ID

   @param libraryId to set
   @return this Program (for chaining methods)
   */
  Program setLibraryId(BigInteger libraryId);

  /**
   Set Memes
   + If there are any exceptions, store them in the SuperEntity errors

   @param memes to set
   @return this Program (for chaining methods)
   */
  Program setMemes(Collection<ProgramMeme> memes);

  /**
   Set name of program

   @param name to set
   @return this Program (for chaining methods)
   */
  Program setName(String name);

  /**
   Set PatternEvents
   + If there are any exceptions, store them in the SuperEntity errors

   @param events to set
   @return this Program (for chaining methods)
   */
  Program setPatternEvents(Collection<Event> events);

  /**
   Set Patterns
   + If there are any exceptions, store them in the SuperEntity errors

   @param patterns to set
   @return this Program (for chaining methods)
   */
  Program setPatterns(Collection<Pattern> patterns);

  /**
   Set all Sequence binding memes
   + If there are any exceptions, store them in the SuperEntity errors

   @param sequenceBindingMemes to set
   @return this Program (for chaining methods)
   */
  Program setSequenceBindingMemes(Collection<SequenceBindingMeme> sequenceBindingMemes);

  /**
   Set all Sequence bindings
   + If there are any exceptions, store them in the SuperEntity errors

   @param sequenceBindings to set
   @return this Program (for chaining methods)
   */
  Program setSequenceBindings(Collection<SequenceBinding> sequenceBindings);

  /**
   Set all Sequence chords
   + If there are any exceptions, store them in the SuperEntity errors

   @param sequenceChords to set
   @return this Program (for chaining methods)
   */
  Program setSequenceChords(Collection<SequenceChord> sequenceChords);

  /**
   Set Sequences; copy in contents, to preserve mutability of data persistent internally for this class.
   + If there are any exceptions, store them in the SuperEntity errors

   @param sequences to set
   @return this Program (for chaining methods)
   */
  Program setSequences(Collection<Sequence> sequences);

  /**
   Set state of Program

   @param value to set
   @return this Program (for chaining methods)
   */
  Program setState(String value);

  /**
   Set state of Program

   @param value to set
   @return this Program (for chaining methods)
   */
  Program setStateEnum(ProgramState value);

  /**
   Set tempo of program

   @param tempo to set
   @return this Program (for chaining methods)
   */
  Program setTempo(Double tempo);

  /**
   Set Type

   @param type to set
   @return this Program (for chaining methods)
   */
  Program setType(String type);

  /**
   Set TypeEnum

   @param type to set
   @return this Program (for chaining methods)
   */
  Program setTypeEnum(ProgramType type);

  /**
   Set updated-at time

   @param updatedAt time
   @return this Program (for chaining methods)
   */
  Program setUpdatedAt(String updatedAt);

  /**
   Set updated-at time

   @param updatedAt time
   @return this Program (for chaining methods)
   */
  Program setUpdatedAtInstant(Instant updatedAt);

  /**
   Set User ID

   @param userId to set
   @return this Program (for chaining methods)
   */
  Program setUserId(BigInteger userId);

  /**
   Set all voices
   + If there are any exceptions, store them in the SuperEntity errors

   @param voices to set
   @return this Program (for chaining methods)
   */
  Program setVoices(Collection<Voice> voices);

}
