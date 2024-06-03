// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.engine.fabricator;

import io.xj.model.enums.InstrumentMode;
import io.xj.model.enums.InstrumentType;
import io.xj.model.enums.ProgramType;
import io.xj.engine.FabricationException;
import io.xj.model.pojos.Segment;
import io.xj.model.pojos.SegmentChoice;
import io.xj.model.pojos.SegmentChoiceArrangement;
import io.xj.model.pojos.SegmentChoiceArrangementPick;
import io.xj.model.pojos.SegmentChord;
import io.xj.model.pojos.SegmentMeta;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 Digest segments of the previous main program
 <p>
 NextMain/NextMacro-type: Retrospective of the previous main choice, primary choices only
 REF https://github.com/xjmusic/workstation/issues/242
 <p>
 Continue-type: Retrospective of all segments in this main program
 REF https://github.com/xjmusic/workstation/issues/242
 */
public interface SegmentRetrospective {

  /**
   Get the arrangement for the given pick

   @param pick for which to get arrangement
   @return arrangement
   @throws FabricationException on failure to retrieve
   */
  SegmentChoiceArrangement getArrangement(SegmentChoiceArrangementPick pick) throws FabricationException;

  /**
   @return all choices
   */
  Collection<SegmentChoice> getChoices();

  /**
   Get the choice for the given arrangement

   @param arrangement for which to get choice
   @return choice
   @throws FabricationException on failure to retrieve
   */
  SegmentChoice getChoice(SegmentChoiceArrangement arrangement) throws FabricationException;

  /**
   Get the instrument type for a given pick

   @param pick for which to get instrument type
   @return instrument type of pick
   */
  InstrumentType getInstrumentType(SegmentChoiceArrangementPick pick) throws FabricationException;

  /**
   Get the meta from the previous segment with the given key
   <p>
   Segment has metadata for XJ to persist "notes in the margin" of the composition for itself to read https://github.com/xjmusic/workstation/issues/222

   @param key to search for meta
   @return meta if found
   */
  Optional<SegmentMeta> getPreviousMeta(String key);

  /**
   Get the previous segment choices for the given instrument
   (although there should only be one previous segment choice for each instrument)

   @param instrumentId for which to get choice
   @return previous segment choice
   */
  Collection<SegmentChoice> getPreviousChoicesForInstrument(UUID instrumentId);

  /**
   Get the previous arrangements for the given instrument id

   @param instrumentId for which to get arrangements
   @return segment choice arrangements
   */
  Collection<SegmentChoiceArrangement> getPreviousArrangementsForInstrument(UUID instrumentId);

  /**
   Get the picks of any previous segments which selected the same main sequence
   <p>
   Artist writing detail program expects 'X' note value to result in random part creation from available Voicings https://github.com/xjmusic/workstation/issues/251

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
   Get the previous-segment choices of a given instrument mode

   @param instrumentMode for which to get previous-segment choices
   @return choices
   */
  List<SegmentChoice> getPreviousChoicesOfMode(InstrumentMode instrumentMode);

  /**
   Get the previous-segment choices of a given instrument type and mode

   @param instrumentType  for which to get previous-segment choices
   @param instrumentModes for which to get previous-segment choices
   @return choices
   */
  List<SegmentChoice> getPreviousChoicesOfTypeMode(InstrumentType instrumentType, InstrumentMode instrumentModes);

  /**
   Get the previous-segment choices of a given instrument type

   @param instrumentType for which to get previous-segment choices
   @return choices
   */
  Optional<SegmentChoice> getPreviousChoiceOfType(InstrumentType instrumentType);

  /**
   Get the previous picks for the given instrument id

   @param instrumentId for which to get picks
   @return segment choice picks
   */
  Collection<SegmentChoiceArrangementPick> getPreviousPicksForInstrument(UUID instrumentId);

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
   Get all segment chords for the given segment id

   @param segmentId for which to get chords
   @return chords
   */
  List<SegmentChord> getSegmentChords(int segmentId);

}
