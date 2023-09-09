// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.fabricator;

import io.xj.hub.enums.InstrumentMode;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramType;
import io.xj.nexus.NexusException;
import io.xj.nexus.model.*;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 Digest segments of the previous main program
 <p>
 NextMain/NextMacro-type: Retrospective of the previous main choice, primary choices only
 REF https://www.pivotaltracker.com/story/show/178442889
 <p>
 Continue-type: Retrospective of all segments in this main program
 REF https://www.pivotaltracker.com/story/show/178442889
 */
public interface SegmentRetrospective {

  /**
   Get the arrangement for the given pick

   @param pick for which to get arrangement
   @return arrangement
   @throws NexusException on failure to retrieve
   */
  SegmentChoiceArrangement getArrangement(SegmentChoiceArrangementPick pick) throws NexusException;

  /**
   @return all choices
   */
  Collection<SegmentChoice> getChoices();

  /**
   Get the choice for the given arrangement

   @param arrangement for which to get choice
   @return choice
   @throws NexusException on failure to retrieve
   */
  SegmentChoice getChoice(SegmentChoiceArrangement arrangement) throws NexusException;

  /**
   Get the instrument type for a given pick

   @param pick for which to get instrument type
   @return instrument type of pick
   */
  InstrumentType getInstrumentType(SegmentChoiceArrangementPick pick) throws NexusException;

  /**
   Get the meta from the previous segment with the given key
   <p>
   Segment has metadata for XJ to persist "notes in the margin" of the composition for itself to read https://www.pivotaltracker.com/story/show/183135787

   @param key to search for meta
   @return meta if found
   */
  Optional<SegmentMeta> getPreviousMeta(String key);

  /**
   Get the previous segment choice for the given instrument

   @param instrumentId for which to get choice
   @return previous segment choice
   */
  Optional<SegmentChoice> getPreviousChoiceForInstrument(UUID instrumentId);

  /**
   Get the previous arrangements for the given instrument id

   @param instrumentId for which to get arrangements
   @return segment choice arrangements
   */
  List<SegmentChoiceArrangement> getPreviousArrangementsForInstrument(UUID instrumentId);

  /**
   Get the picks of any previous segments which selected the same main sequence
   <p>
   Artist writing detail program expects 'X' note value to result in random part creation from available Voicings https://www.pivotaltracker.com/story/show/175947230

   @return map of all previous segment meme constellations (as keys) to a collection of choices made
   */
  Collection<SegmentChoiceArrangementPick> getPicks();

  /**
   Get the choice of a given type

   @param type of choice to get
   @return choice of given type
   */
  Optional<SegmentChoice> getPreviousChoiceOfType(Segment segment, ProgramType type);

  /**
   Get the previous-segment choice of a given type

   @param type of choice to get
   @return choice of given type
   */
  Optional<SegmentChoice> getPreviousChoiceOfType(ProgramType type);

  /**
   Get the choice of a given voice by id

   @param programVoiceId of voice for which to get choice
   @return choice of given type
   */
  Optional<SegmentChoice> getPreviousChoiceOfType(Segment segment, UUID programVoiceId);

  /**
   Get the previous-segment choices of a given instrument mode

   @param instrumentMode for which to get previous-segment choices
   @return choices
   */
  List<SegmentChoice> getPreviousChoicesOfMode(InstrumentMode instrumentMode);

  /**
   Get the previous-segment choices of a given instrument type

   @param instrumentType  for which to get previous-segment choices
   @param instrumentModes for which to get previous-segment choices
   @return choices
   */
  List<SegmentChoice> getPreviousChoicesOfTypeMode(InstrumentType instrumentType, InstrumentMode instrumentModes);

  /**
   Get the previous picks for the given instrument id

   @param instrumentId for which to get picks
   @return segment choice picks
   */
  List<SegmentChoiceArrangementPick> getPreviousPicksForInstrument(UUID instrumentId);

  /**
   Get the segment immediately previous to the current segment

   @return previous segment
   */
  Optional<Segment> getPreviousSegment();

  /**
   @return all cached segments
   <p>
   Always starts with the segment at current offset minus one,
   includes that segment and all others with the same main program, which covers both
   whether this retrospective is looking back on the current main program (Continue segment),
   or the previous one (NextMain/NextMacro segments)
   */
  Collection<Segment> getSegments();

  /**
   Get a segment by id

   @param id for which to get segment
   */
  Optional<Segment> getSegment(int id);

  /**
   Get all segment chords for the given segment id

   @param segmentId for which to get chords
   @return chords
   */
  List<SegmentChord> getSegmentChords(int segmentId);

}
