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

import java.util.Collection;

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
  Collection<SegmentChoiceArrangement> getSegmentArrangements() throws FabricationException;

  /**
   @return collection of all Choice in Segment
   */
  Collection<SegmentChoice> getSegmentChoices() throws FabricationException;

  /**
   @return collection of all Chord in Segment
   */
  Collection<SegmentChord> getSegmentChords() throws FabricationException;

  /**
   @return collection of all ChordVoicing in Segment
   */
  Collection<SegmentChordVoicing> getSegmentChordVoicings() throws FabricationException;

  /**
   @return collection of all Meme in Segment
   */
  Collection<SegmentMeme> getSegmentMemes() throws FabricationException;

  /**
   @return collection of all Message in Segment
   */
  Collection<SegmentMessage> getSegmentMessages() throws FabricationException;

  /**
   @return collection of all ChoiceArrangementPick in Segment
   */
  Collection<SegmentChoiceArrangementPick> getSegmentChoiceArrangementPicks() throws FabricationException;

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
  void done() throws FabricationException, JsonApiException, ValueException;

  /**
   Get the choice of a given type

   @param type of choice to get
   @return choice of given type
   @throws FabricationException if no such choice type exists
   */
  SegmentChoice getChoiceOfType(Program.Type type) throws FabricationException;

  /**
   Get the choices of a given program and instrument type

   @param type of choice to get
   @return choices of a given type
   @throws FabricationException if no such choice type exists
   */
  Collection<SegmentChoice> getChoicesOfType(Program.Type type) throws FabricationException;

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
   @throws FabricationException on failure
   */
  <N extends MessageLite> N add(N entity) throws FabricationException;
}
