// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.ingest;

import io.xj.core.access.impl.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.entity.Entity;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.library.Library;
import io.xj.core.model.program.Program;
import io.xj.core.model.program.ProgramType;

import java.math.BigInteger;
import java.util.Collection;

/**
 [#154350346] Architect wants a universal Ingest Factory, to modularize graph mathematics used during craft to ingest any combination of Library, Sequence, and Instrument for any purpose.
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

  /**
   Get a collection of all sequences for ingest

   @return collection of sequences
   */
  Collection<Program> getAllPrograms();

  /**
   Get a collection of all sequences of a particular type for ingest

   @return collection of sequences
   */
  Collection<Program> getProgramsOfType(ProgramType type);

  /**
   Get the access with which this Ingest was instantiated.

   @return access
   */
  Access getAccess();

  /**
   Get a Sequence by id, ideally in the original entity map, and if not, from a cached read of the DAO

   @param id of sequence to read
   @return sequence
   */
  Program getProgram(BigInteger id) throws CoreException;

  /**
   Get a collection of all instruments for ingest

   @return collection of instruments
   */
  Collection<Instrument> getAllInstruments();

  /**
   Get a collection of all instruments of a particular type for ingest

   @return collection of instruments
   */
  Collection<Instrument> getInstrumentsOfType(InstrumentType type);

  /**
   Get a collection of all Libraries for ingest

   @return collection of Libraries
   */
  Collection<Library> getAllLibraries();

  /**
   Get a collection of all entities

   @return collection of all entities
   */
  Collection<Entity> getAllEntities();

  /**
   Get a Instrument by id, ideally in the original entity map, and if not, from a cached read of the DAO

   @param id of instrument to read
   @return instrument
   */
  Instrument getInstrument(BigInteger id) throws CoreException;

  /**
   Get a string representation of the ingest
   */
  String toString();
}
