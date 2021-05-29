// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.ingest;

import io.xj.Instrument;
import io.xj.InstrumentAudio;
import io.xj.InstrumentAudioChord;
import io.xj.InstrumentMeme;
import io.xj.Program;
import io.xj.ProgramMeme;
import io.xj.ProgramSequence;
import io.xj.ProgramSequenceBinding;
import io.xj.ProgramSequenceBindingMeme;
import io.xj.ProgramSequenceChord;
import io.xj.hub.access.HubAccess;

import java.util.Collection;

/**
 [#154350346] Architect wants a universal HubIngest Factory, to modularize graph mathematics used during craft to ingest any combination of Library, Sequence, and Instrument for any purpose.
 <p>
 # Component
 <p>
 - **HubIngest**
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
  Collection<Program> getAllPrograms() throws HubIngestException;

  /**
   get all cached InstrumentAudioChords

   @return cached InstrumentAudioChords
   */
  Collection<InstrumentAudioChord> getAllInstrumentAudioChords() throws HubIngestException;

  /**
   get all cached InstrumentMemes

   @return cached InstrumentMemes
   */
  Collection<InstrumentMeme> getAllInstrumentMemes() throws HubIngestException;

  /**
   get all cached ProgramMemes

   @return cached ProgramMemes
   */
  Collection<ProgramMeme> getAllProgramMemes() throws HubIngestException;

  /**
   get all cached ProgramSequences

   @return cached ProgramSequences
   */
  Collection<ProgramSequence> getAllProgramSequences() throws HubIngestException;

  /**
   get all cached ProgramSequenceBindings

   @return cached ProgramSequenceBindings
   */
  Collection<ProgramSequenceBinding> getAllProgramSequenceBindings() throws HubIngestException;

  /**
   get all cached ProgramSequenceBindingMemes

   @return cached ProgramSequenceBindingMemes
   */
  Collection<ProgramSequenceBindingMeme> getAllProgramSequenceBindingMemes() throws HubIngestException;

  /**
   get all cached ProgramSequenceChords

   @return cached ProgramSequenceChords
   */
  Collection<ProgramSequenceChord> getAllProgramSequenceChords() throws HubIngestException;

  /**
   get all cached Instruments

   @return cached Instruments
   */
  Collection<Instrument> getAllInstruments() throws HubIngestException;

  /**
   get cached Instrument by id

   @param id of Instrument to get
   @return Instrument
   */
  Instrument getInstrument(String id) throws HubIngestException;

  /**
   get cached Program by id

   @param id of Program to get
   @return Program
   */
  Program getProgram(String id) throws HubIngestException;

  /**
   Get a collection of all sequences of a particular type for ingest

   @return collection of sequences
   */
  Collection<Program> getProgramsOfType(Program.Type type) throws HubIngestException;

  /**
   Get the access with which this HubIngest was instantiated.

   @return access
   */
  HubAccess getHubAccess();

  /**
   Get a collection of all instruments of a particular type for ingest

   @return collection of instruments
   */
  Collection<Instrument> getInstrumentsOfType(Instrument.Type type) throws HubIngestException;

  /**
   Get a collection of all entities

   @return collection of all entities
   */
  Collection<Object> getAllEntities();


  /**
   Get a string representation of the ingest
   */
  String toString();

  /**
   Get memes of program

   @param program to get memes for
   @return memes of program
   */
  Collection<ProgramMeme> getMemes(Program program) throws HubIngestException;

  /**
   Get memes of instrument

   @param instrument to get memes for
   @return memes of instrument
   */
  Collection<InstrumentMeme> getMemes(Instrument instrument) throws HubIngestException;

  /**
   Get all AudioChords for a given Audio

   @param audio to get chords for
   @return chords of audio
   */
  Collection<InstrumentAudioChord> getChords(InstrumentAudio audio) throws HubIngestException;

  /**
   Get all ProgramSequenceChords for a given Sequence

   @param sequence to get chords for
   @return chords of sequence
   */
  Collection<ProgramSequenceChord> getChords(ProgramSequence sequence) throws HubIngestException;

  /**
   Get all program sequence binding memes for program sequence binding

   @param programSequenceBinding to get memes for
   @return memes
   */
  Collection<ProgramSequenceBindingMeme> getMemes(ProgramSequenceBinding programSequenceBinding) throws HubIngestException;

  /**
   Get all program sequence bindings for a given program

   @param program to get sequence bindings for
   @return all sequence bindings for given program
   */
  Collection<ProgramSequenceBinding> getSequenceBindings(Program program) throws HubIngestException;

  /**
   Get all program sequences for a given program

   @param program to get program sequences for
   @return all program sequences for given program
   */
  Collection<ProgramSequence> getSequences(Program program) throws HubIngestException;

}
