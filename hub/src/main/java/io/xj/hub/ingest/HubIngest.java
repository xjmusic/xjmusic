// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.ingest;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.xj.hub.access.HubAccess;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.Program;

import java.util.Collection;
import java.util.UUID;

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
 - **Absorb** (to take up; to drink in; to penetrate into the material) / **Adsorb** (to attract and bind as to form a thin layer on the surface)
 - **Aggregate**
 - **Consider**
 - **Deem**
 - **HubIngest** (to take in) / **Digest** (to distribute or arrange methodically; to work over and
 classify; to reduce for ready use or
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
   get all cached Instruments

   @return cached Instruments
   */
  Collection<Instrument> getAllInstruments() throws HubIngestException;

  /**
   get cached Instrument by id

   @param id of Instrument to get
   @return Instrument
   */
  Instrument getInstrument(UUID id) throws HubIngestException;

  /**
   get cached Program by id

   @param id of Program to get
   @return Program
   */
  Program getProgram(UUID id) throws HubIngestException;

  /**
   Get a collection of all sequences of a particular type for ingest

   @return collection of sequences
   */
  Collection<Program> getProgramsOfType(ProgramType type) throws HubIngestException;

  /**
   Get the access with which this HubIngest was instantiated.

   @return access
   */
  HubAccess getHubAccess();

  /**
   Get a collection of all instruments of a particular type for ingest

   @return collection of instruments
   */
  Collection<Instrument> getInstrumentsOfType(InstrumentType type) throws HubIngestException;

  /**
   Get a collection of all entities

   @return collection of all entities
   */
  Collection<Object> getAllEntities();

  /**
   Express all hub content as JSON. At the top level, we have a key for each entity type, having an array of objects.

   @return JSON of all hub content
   */
  String toJSON() throws JsonProcessingException;

  /**
   Get a string representation of ingest
   */
  String toString();

}
