// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.fabricator;

import io.xj.hub.enums.ProgramType;
import io.xj.hub.util.ValueException;
import io.xj.nexus.jsonapi.JsonapiException;
import io.xj.nexus.NexusException;
import io.xj.nexus.model.*;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 The SegmentWorkbench is a delegate to manipulate the segment currently in progress during the fabrication.
 The pattern here is that all child entities of this segment are held in memory only within this delegate
 until the very end of the process, when the trigger is pulled and all the entities are written to the database
 using a special segment Manager method that does them all in one transaction.
 <p>
 SegmentWorkbench.done()
 Called at the end of Segment fabrication.
 Sends added records to segmentManager batch insert method
 */
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
  List<SegmentChord> getSegmentChords();

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
   @return collection of all Meta in Segment
   */
  Collection<SegmentMeta> getSegmentMetas();

  /**
   @return collection of all ChoiceArrangementPick in Segment
   */
  Collection<SegmentChoiceArrangementPick> getSegmentChoiceArrangementPicks();

  /**
   Put a key-value pair into the report
   only exports data as a sub-field of the standard content JSON https://www.pivotaltracker.com/story/show/162999779

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

   @return Chain containing this Segment
   */
  Chain getChain();

  /**
   Put an Entity by type and id
   <p>
   Segment meta overwrites existing meta with same key https://www.pivotaltracker.com/story/show/183135787

   @param entity to put
   @param <N>    type of Entity
   @return entity that was added
   @throws NexusException on failure
   */
  <N> N put(N entity) throws NexusException;

  /**
   Remove an Entity by type and id

   @param entity to remove
   @param <N>    type of Entity
   */
  <N> void delete(N entity);

  /**
   Get a segment meta matching the given key
   <p>
   Segment has metadata for XJ to persist "notes in the margin" of the composition for itself to read https://www.pivotaltracker.com/story/show/183135787

   @param key to search for meta
   @return meta if found
   */
  Optional<SegmentMeta> getSegmentMeta(String key);
}
