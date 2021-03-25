// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.fabricator;

import com.google.protobuf.MessageLite;
import io.xj.Chain;
import io.xj.Program;
import io.xj.Segment;
import io.xj.SegmentChoice;
import io.xj.SegmentChoiceArrangement;
import io.xj.SegmentChoiceArrangementPick;
import io.xj.SegmentChord;
import io.xj.SegmentChordVoicing;
import io.xj.SegmentMeme;
import io.xj.SegmentMessage;
import io.xj.lib.jsonapi.JsonApiException;
import io.xj.lib.util.ValueException;
import io.xj.service.nexus.NexusException;

import java.util.Collection;
import java.util.Optional;

public interface SegmentWorkbench {

  /**
   Get the segment that's being worked on.
   Modify this object's properties.
   At the end of the fabrication process, this segment is saved via DAO

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
  Collection<SegmentChoiceArrangement> getSegmentArrangements();

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
   Sends added records to segmentDAO batch insert method
   */
  void done() throws NexusException, JsonApiException, ValueException;

  /**
   Get the choice of a given type

   @param type of choice to get
   @return choice of given type
   */
  Optional<SegmentChoice> getChoiceOfType(Program.Type type);

  /**
   Get the choices of a given program and instrument type

   @param type of choice to get
   @return choices of a given type
   */
  Collection<SegmentChoice> getChoicesOfType(Program.Type type);

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
  <N extends MessageLite> N add(N entity) throws NexusException;
}
