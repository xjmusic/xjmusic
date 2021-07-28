// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.fabricator;

import io.xj.Program;
import io.xj.Segment;
import io.xj.SegmentChoice;
import io.xj.SegmentChoiceArrangementPick;

import java.util.Collection;
import java.util.Optional;

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
  Optional<SegmentChoice> getPreviousChoiceOfType(Segment segment, Program.Type type);

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
  Optional<SegmentChoice> getPreviousChoiceOfType(Program.Type type);

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

  /**
   @return all inertial choices
   */
  Collection<SegmentChoice> getInertialChoices();

  /**
   @return all primary choices
   */
  Collection<SegmentChoice> getPrimaryChoices();
}
