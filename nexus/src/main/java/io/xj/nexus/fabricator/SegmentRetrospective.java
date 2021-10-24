// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.fabricator;

import io.xj.api.Segment;
import io.xj.api.SegmentChoice;
import io.xj.api.SegmentChoiceArrangementPick;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramType;

import java.util.*;

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
   Get the choice of a given type

   @param type of choice to get
   @return choice of given type
   */
  Optional<SegmentChoice> getPreviousChoiceOfType(Segment segment, ProgramType type);

  /**
   Get the choice of a given voice by id

   @param programVoiceId of voice for which to get choice
   @return choice of given type
   */
  Optional<SegmentChoice> getPreviousChoiceOfType(Segment segment, UUID programVoiceId);

  /**
   Get the picks of any previous segments which selected the same main sequence
   <p>
   [#175947230] Artist writing detail program expects 'X' note value to result in random part creation from available Voicings

   @return map of all previous segment meme constellations (as keys) to a collection of choices made
   */
  Collection<SegmentChoiceArrangementPick> getPicks();

  /**
   Get the segment immediately previous to the current segment

   @return previous segment
   */
  Optional<Segment> getPreviousSegment();

  /**
   Get the previous-segment choice of a given type

   @param type of choice to get
   @return choice of given type
   */
  Optional<SegmentChoice> getPreviousChoiceOfType(ProgramType type);

  /**
   Get the value seen in previous segments for the given metadata name
   <p>
   Segment has Metadata to inform fabricator of subsequent segments #180059436

   @return value seen in previous segments
   @param name of metadata for which to search
   */
  Optional<String> getPreviousMetadataValue(String name);

  /**
   Get the previous-segment choices of a given instrument type

   @param instrumentType for which to get previous-segment choices
   @return choices
   */
  List<SegmentChoice> getPreviousChoicesOfType(InstrumentType instrumentType);

  /**
   Get the previous-segment choice of a given voice by id

   @param programVoiceId of voice for which to get choice
   @return choice of given type
   */
  Optional<SegmentChoice> getPreviousChoiceOfVoice(UUID programVoiceId);

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
   @return all choices
   */
  Collection<SegmentChoice> getChoices();
}
