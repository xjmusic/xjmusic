// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.ingest;

import io.xj.lib.entity.Entity;
import io.xj.lib.entity.MemeEntity;
import io.xj.service.hub.access.HubAccess;
import io.xj.service.hub.entity.*;

import java.util.Collection;
import java.util.UUID;

/**
 [#154350346] Architect wants a universal HubIngest Factory, to modularize graph mathematics used during craft to ingest any combination of Library, Sequence, and Instrument for any purpose.
 <p>
 # Component
 <p>
 - **HubIngest**
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
 - **HubIngest** (to take in) / **Digest** (to distribute or arrange methodically; to work over and
 classify; to reduce to portions for ready use or
 application)
 - **Extract**
 - **Fetch**
 - **Gather**
 - **Glean**
 - **Induce** (to lead in; to introduce) / **Deduce** (to take away; to deduct; to subtract; as, to deduce a part of the whole)
 - **Infer**
 - **Obtain**
 - **Place**
 - **Rank**
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
public interface HubIngest {

  /**
   get all cached Programs

   @return cached Programs
   */
  Collection<Program> getAllPrograms();

  /**
   get all cached InstrumentAudios

   @return cached InstrumentAudios
   */
  Collection<InstrumentAudio> getAllInstrumentAudios();

  /**
   get all cached InstrumentAudioChords

   @return cached InstrumentAudioChords
   */
  Collection<InstrumentAudioChord> getAllInstrumentAudioChords();

  /**
   get all cached InstrumentAudioEvents

   @return cached InstrumentAudioEvents
   */
  Collection<InstrumentAudioEvent> getAllInstrumentAudioEvents();

  /**
   get all cached InstrumentMemes

   @return cached InstrumentMemes
   */
  Collection<InstrumentMeme> getAllInstrumentMemes();

  /**
   get all cached ProgramEvents

   @return cached ProgramEvents
   */
  Collection<ProgramSequencePatternEvent> getAllProgramEvents();

  /**
   get all cached ProgramMemes

   @return cached ProgramMemes
   */
  Collection<ProgramMeme> getAllProgramMemes();

  /**
   get all cached ProgramPatterns

   @return cached ProgramPatterns
   */
  Collection<ProgramSequencePattern> getAllProgramPatterns();

  /**
   get all cached ProgramSequences

   @return cached ProgramSequences
   */
  Collection<ProgramSequence> getAllProgramSequences();

  /**
   get all cached ProgramSequenceBindings

   @return cached ProgramSequenceBindings
   */
  Collection<ProgramSequenceBinding> getAllProgramSequenceBindings();

  /**
   get all cached ProgramSequenceBindingMemes

   @return cached ProgramSequenceBindingMemes
   */
  Collection<ProgramSequenceBindingMeme> getAllProgramSequenceBindingMemes();

  /**
   get all cached ProgramSequenceChords

   @return cached ProgramSequenceChords
   */
  Collection<ProgramSequenceChord> getAllProgramSequenceChords();

  /**
   get all cached ProgramTracks

   @return cached ProgramTracks
   */
  Collection<ProgramVoiceTrack> getAllProgramTracks();

  /**
   get all cached ProgramVoices

   @return cached ProgramVoices
   */
  Collection<ProgramVoice> getAllProgramVoices();

  /**
   get all cached Instruments

   @return cached Instruments
   */
  Collection<Instrument> getAllInstruments();


  /**
   Get all available sequence pattern offsets of a given sequence

   @param sequenceBinding to get available sequence pattern offsets for
   @return collection of available sequence pattern offsets
   */
  Collection<Long> getAvailableOffsets(ProgramSequenceBinding sequenceBinding);

  /**
   get cached InstrumentAudioChord by id

   @param id of InstrumentAudioChord to get
   @return InstrumentAudioChord
   */
  InstrumentAudioChord getInstrumentAudioChord(UUID id) throws HubIngestException;

  /**
   get cached InstrumentAudio by id

   @param id of InstrumentAudio to get
   @return InstrumentAudio
   */
  InstrumentAudio getInstrumentAudio(UUID id) throws HubIngestException;

  /**
   get cached InstrumentAudioEvent by id

   @param id of InstrumentAudioEvent to get
   @return InstrumentAudioEvent
   */
  InstrumentAudioEvent getInstrumentAudioEvent(UUID id) throws HubIngestException;

  /**
   get cached Instrument by id

   @param id of Instrument to get
   @return Instrument
   */
  Instrument getInstrument(UUID id) throws HubIngestException;

  /**
   get cached InstrumentMeme by id

   @param id of InstrumentMeme to get
   @return InstrumentMeme
   */
  InstrumentMeme getInstrumentMeme(UUID id) throws HubIngestException;

  /**
   get cached Program by id

   @param id of Program to get
   @return Program
   */
  Program getProgram(UUID id) throws HubIngestException;

  /**
   get cached ProgramSequencePatternEvent by id

   @param id of ProgramSequencePatternEvent to get
   @return ProgramSequencePatternEvent
   */
  ProgramSequencePatternEvent getProgramEvent(UUID id) throws HubIngestException;

  /**
   get cached ProgramMeme by id

   @param id of ProgramMeme to get
   @return ProgramMeme
   */
  ProgramMeme getProgramMeme(UUID id) throws HubIngestException;

  /**
   get cached ProgramSequencePattern by id

   @param id of ProgramSequencePattern to get
   @return ProgramSequencePattern
   */
  ProgramSequencePattern getProgramPattern(UUID id) throws HubIngestException;

  /**
   get cached ProgramSequenceBinding by id

   @param id of ProgramSequenceBinding to get
   @return ProgramSequenceBinding
   */
  ProgramSequenceBinding getProgramSequenceBinding(UUID id) throws HubIngestException;

  /**
   get cached ProgramSequenceBindingMeme by id

   @param id of ProgramSequenceBindingMeme to get
   @return ProgramSequenceBindingMeme
   */
  ProgramSequenceBindingMeme getProgramSequenceBindingMeme(UUID id) throws HubIngestException;

  /**
   get cached ProgramSequenceChord by id

   @param id of ProgramSequenceChord to get
   @return ProgramSequenceChord
   */
  ProgramSequenceChord getProgramSequenceChord(UUID id) throws HubIngestException;

  /**
   get cached ProgramSequence by id

   @param id of ProgramSequence to get
   @return ProgramSequence
   */
  ProgramSequence getProgramSequence(UUID id) throws HubIngestException;

  /**
   get cached ProgramVoiceTrack by id

   @param id of ProgramVoiceTrack to get
   @return ProgramVoiceTrack
   */
  ProgramVoiceTrack getProgramTrack(UUID id) throws HubIngestException;

  /**
   get cached ProgramVoice by id

   @param id of ProgramVoice to get
   @return ProgramVoice
   */
  ProgramVoice getProgramVoice(UUID id) throws HubIngestException;

  /**
   Get a collection of all sequences of a particular type for ingest

   @return collection of sequences
   */
  Collection<Program> getProgramsOfType(ProgramType type);

  /**
   Get the access with which this HubIngest was instantiated.

   @return access
   */
  HubAccess getHubAccess();

  /**
   Get a collection of all instruments of a particular type for ingest

   @return collection of instruments
   */
  Collection<Instrument> getInstrumentsOfType(InstrumentType type);

  /**
   Get a collection of all entities

   @return collection of all entities
   */
  Collection<Entity> getAllEntities();


  /**
   Get a string representation of the ingest
   */
  String toString();

  /**
   Get memes of program

   @param program to get memes for
   @return memes of program
   */
  Collection<ProgramMeme> getMemes(Program program);

  /**
   Get events for a given program

   @param program to get events for
   @return events for given program
   */
  Collection<ProgramSequencePatternEvent> getEvents(Program program);

  /**
   Get events for a given program pattern

   @param program pattern to get events for
   @return events for given program pattern
   */
  Collection<ProgramSequencePatternEvent> getEvents(ProgramSequencePattern program);

  /**
   Get memes of instrument

   @param instrument to get memes for
   @return memes of instrument
   */
  Collection<InstrumentMeme> getMemes(Instrument instrument);

  /**
   Get all Audios for a given instrument id

   @param uuid of instrument to get audios for
   @return audios of instrument id
   */
  Collection<InstrumentAudio> getAudiosForInstrumentId(UUID uuid);

  /**
   Get all InstrumentAudios for a given Instrument

   @param instrument to get audios for
   @return audios for instrument
   */
  Collection<InstrumentAudio> getAudios(Instrument instrument);

  /**
   Get all AudioEvents for a given Audio

   @param audio to get events for
   @return events of audio
   */
  Collection<InstrumentAudioEvent> getEvents(InstrumentAudio audio);

  /**
   Get all AudioChords for a given Audio

   @param audio to get chords for
   @return chords of audio
   */
  Collection<InstrumentAudioChord> getChords(InstrumentAudio audio);

  /**
   Get all ProgramSequenceChords for a given Sequence

   @param sequence to get chords for
   @return chords of sequence
   */
  Collection<ProgramSequenceChord> getChords(ProgramSequence sequence);

  /**
   Get all program sequence binding memes for program sequence binding

   @param programSequenceBinding to get memes for
   @return memes
   */
  Collection<ProgramSequenceBindingMeme> getMemes(ProgramSequenceBinding programSequenceBinding);

  /**
   Get all program sequence bindings for a given program

   @param program to get sequence bindings for
   @return all sequence bindings for given program
   */
  Collection<ProgramSequenceBinding> getSequenceBindings(Program program);

  /**
   Get all program sequences for a given program

   @param program to get program sequences for
   @return all program sequences for given program
   */
  Collection<ProgramSequence> getSequences(Program program);

  /**
   Get the program sequence for a given program sequence binding

   @param sequenceBinding to get program sequence for
   @return program sequence for the given program sequence binding
   */
  ProgramSequence getSequence(ProgramSequenceBinding sequenceBinding) throws HubIngestException;

  /**
   Read all AudioEvent that are first in an audio, for all audio in an Instrument

   @param instrument to get audio for
   @return audio events
   @throws HubIngestException on failure
   */
  Collection<InstrumentAudioEvent> getFirstEventsOfAudiosOfInstrument(Instrument instrument) throws HubIngestException;

  /**
   Fetch all memes for a given program at sequence binding offset 0

   @return collection of sequence memes
   @throws HubIngestException on failure
   */
  Collection<MemeEntity> getMemesAtBeginning(Program program) throws HubIngestException;

  /**
   Get sequence bindings at a specified offset

   @param program to get sequence bindings for
   @param offset  to get sequence bindings at
   @return sequence bindings at offset
   */
  Collection<ProgramSequenceBinding> getProgramSequenceBindingsAtOffset(Program program, Long offset);

  /**
   Get Program voice for a given program event

   @param event to get program voice of
   @return Program voice for the given program event
   */
  ProgramVoice getVoice(ProgramSequencePatternEvent event) throws HubIngestException;

  /**
   Get Program track for a given program event

   @param event to get program track of
   @return Program track for the given program event
   */
  ProgramVoiceTrack getTrack(ProgramSequencePatternEvent event) throws HubIngestException;

  /**
   Get all program voices for a given program

   @param program to get program voices for
   @return program voices for the given program
   */
  Collection<ProgramVoice> getVoices(Program program);
}
