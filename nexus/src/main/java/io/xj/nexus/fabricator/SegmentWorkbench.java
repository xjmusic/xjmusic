// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.fabricator;

import io.xj.api.*;
import io.xj.hub.enums.ProgramType;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.util.ValueException;
import io.xj.nexus.NexusException;

import java.util.Collection;
import java.util.Optional;

public interface SegmentWorkbench {

  /**
   Get the segment that's being worked on.
   Modify this object's properties.
   At the end of the fabrication process, this segment is saved via Manager

   @return current segment
   */
  Segment getSegment();

  /**
   Set the Segment.
   Any modifications to the Segment must be re-written to here
   because protobuf instances are immutable

   @param segment to set
   */
  void setSegment(Segment segment);

  /**
   @return collection of all ChoiceArrangement in Segment
   */
  Collection<SegmentChoiceArrangement> getSegmentChoiceArrangements();

  /**
   @return collection of all Choice in Segment
   */
  Collection<SegmentChoice> getSegmentChoices();

  /**
   @return collection of all Chord in Segment, guaranteed to be in order of position ascending
   */
  Collection<SegmentChord> getSegmentChords();

  /**
   @return collection of all ChordVoicing in Segment
   */
  Collection<SegmentChordVoicing> getSegmentChordVoicings();

  /**
   @return collection of all Meme in Segment
   */
  Collection<SegmentMeme> getSegmentMemes();

  /**
   @return collection of all Message in Segment
   */
  Collection<SegmentMessage> getSegmentMessages();

  /**
   @return collection of all ChoiceArrangementPick in Segment
   */
  Collection<SegmentChoiceArrangementPick> getSegmentChoiceArrangementPicks();

  /**
   Put a key-value pair into the report
   [#162999779] only exports data as a sub-field of the standard content JSON

   @param key   to put
   @param value to put
   */
  void putReport(String key, Object value);

  /***
   Called at the end of Segment fabrication.
   Sends added records to segmentManager batch insert method
   */
  void done() throws NexusException, JsonapiException, ValueException;

  /**
   Get the choice of a given type

   @param type of choice to get
   @return choice of given type
   */
  Optional<SegmentChoice> getChoiceOfType(ProgramType type);

  /**
   Get the choices of a given program and instrument type

   @param type of choice to get
   @return choices of a given type
   */
  Collection<SegmentChoice> getChoicesOfType(ProgramType type);

  /**
   Get the Chain this Segment Workbench is working within

   @return Chain that this this Segment Workbench is working within
   */
  Chain getChain();

  /**
   Add an Entity

   @param entity to add
   @param <N>    type of Entity
   @return entity that was added
   @throws NexusException on failure
   */
  <N> N add(N entity) throws NexusException;

  /**
   Segment has Metadata to inform fabricator of subsequent segments #180059436

   @return all the Segment Metadatas
   */
  Collection<SegmentMetadata> getSegmentMetadatas();
}
