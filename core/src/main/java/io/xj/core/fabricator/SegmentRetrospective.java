package io.xj.core.fabricator;

import io.xj.core.exception.CoreException;
import io.xj.core.model.ProgramType;
import io.xj.core.model.Segment;
import io.xj.core.model.SegmentChoiceArrangement;
import io.xj.core.model.SegmentChoice;
import io.xj.core.model.SegmentChoiceArrangementPick;
import io.xj.core.model.SegmentChord;
import io.xj.core.model.SegmentMeme;

import java.util.Collection;
import java.util.Optional;

public interface SegmentRetrospective {

  /**
   @return entity cache of SegmentChoiceArrangementPick
   @param segment to get picks for
   */
  Collection<SegmentChoiceArrangementPick> getSegmentPicks(Segment segment);

  /**
   @return entity cache of SegmentChoiceArrangement
   */
  Collection<SegmentChoiceArrangement> getSegmentArrangements(SegmentChoice choice);

  /**
   @return entity cache of SegmentChoice
   */
  Collection<SegmentChoice> getSegmentChoices(Segment segment);

  /**
   @return entity cache of SegmentChord
   */
  Collection<SegmentChord> getSegmentChords(Segment segment);

  /**
   @return entity cache of SegmentMeme
   */
  Collection<SegmentMeme> getSegmentMemes(Segment segment);

  /**
   * @return all cached segments
   */
  Collection<Segment> getSegments();

  /**
   Get the choice of a given type

   @param type of choice to get
   @return choice of given type
   @throws CoreException if no such choice type exists
   */
  SegmentChoice getChoiceOfType(Segment segment, ProgramType type) throws CoreException;

  /**
   * Get the arrangements for a given choice
   * @param segmentChoice to get arrangements for
   * @return arrangements for choice
   */
  Collection<SegmentChoiceArrangement> getArrangements(SegmentChoice segmentChoice);

  /**
   * Get the segment immediately previous to the current segment
   * @return previous segment
   */
  Optional<Segment> getPreviousSegment();

  /**
   Get the previous-segment choice of a given type

   @param type of choice to get
   @return choice of given type
   @throws CoreException if no such choice type exists
   */
  SegmentChoice getPreviousChoiceOfType(ProgramType type) throws CoreException;

}
