// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.persistence;

import com.google.api.client.util.Lists;
import com.google.common.base.Strings;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramType;
import io.xj.lib.music.Note;
import io.xj.lib.util.CSV;
import io.xj.nexus.NexusException;
import io.xj.nexus.model.Segment;
import io.xj.nexus.model.SegmentChoice;
import io.xj.nexus.model.SegmentChordVoicing;
import io.xj.nexus.model.SegmentState;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Utilities for working with segments
 */
public enum Segments {
  ;
  public static final int DELTA_UNLIMITED = -1;
  private static final String EXTENSION_SEPARATOR = ".";
  private static final String WAV_EXTENSION = "wav";

  /**
   * Find first segment choice of a given type in a collection of segment choices
   *
   * @param segmentChoices to filter from
   * @param type           to find one of
   * @return segment choice of given type
   */
  public static SegmentChoice findFirstOfType(Collection<SegmentChoice> segmentChoices, ProgramType type) throws NexusException {
    Optional<SegmentChoice> found = segmentChoices.stream().filter(c -> c.getProgramType().equals(type)).findFirst();
    if (found.isEmpty()) throw new NexusException(String.format("No %s-type choice found", type));
    return found.get();
  }

  /**
   * Find first segment choice of a given type in a collection of segment choices
   *
   * @param segmentChoices to filter from
   * @param type           to find one of
   * @return segment choice of given type
   */
  public static SegmentChoice findFirstOfType(Collection<SegmentChoice> segmentChoices, InstrumentType type) throws NexusException {
    Optional<SegmentChoice> found = segmentChoices.stream().filter(c -> c.getInstrumentType().equals(type)).findFirst();
    if (found.isEmpty()) throw new NexusException(String.format("No %s-type choice found", type));
    return found.get();
  }

  /**
   * Get the identifier or a Segment: ship key if available, else ID
   *
   * @param segment to get identifier of
   * @return ship key if available, else ID
   */
  public static String getIdentifier(@Nullable Segment segment) {
    if (Objects.isNull(segment)) return "N/A";
    return Strings.isNullOrEmpty(segment.getStorageKey()) ? segment.getId().toString() : segment.getStorageKey();
  }


  /**
   * Get the last dubbed from any collection of Segments
   *
   * @param segments to get last dubbed from
   * @return last dubbed segment from collection
   */
  public static Optional<Segment> getLastCrafted(Collection<Segment> segments) {
    return getLast(getCrafted(segments));
  }

  /**
   * Get the last from any collection of Segments
   *
   * @param segments to get last from
   * @return last segment from collection
   */
  public static Optional<Segment> getLast(Collection<Segment> segments) {
    return segments
      .stream()
      .max(Comparator.comparing(Segment::getOffset));
  }

  /**
   * Get only the dubbed from any collection of Segments
   *
   * @param segments to get dubbed from
   * @return dubbed segments from collection
   */
  public static Collection<Segment> getCrafted(Collection<Segment> segments) {
    return segments
      .stream()
      .filter(segment -> SegmentState.CRAFTED == segment.getState())
      .collect(Collectors.toList());
  }

  /**
   * Whether a segment chord voicing contains any valid notes
   *
   * @param voicing to test
   * @return true if contains any valid notes
   */
  public static boolean containsAnyValidNotes(SegmentChordVoicing voicing) {
    return Note.containsAnyValidNotes(voicing.getNotes());
  }

  /**
   * Whether the segment is spanning a given time
   *
   * @param segment         to test
   * @param fromChainMicros to test
   * @param toChainMicros   to test
   * @return true if segment is spanning time
   */
  public static boolean isSpanning(Segment segment, Long fromChainMicros, Long toChainMicros) {
    return Objects.nonNull(segment.getDurationMicros()) &&
      segment.getBeginAtChainMicros() + segment.getDurationMicros() > fromChainMicros &&
      segment.getBeginAtChainMicros() < toChainMicros;
  }

  /**
   * Get the storage filename for a Segment
   *
   * @param segment   for which to get storage filename
   * @param extension of key
   * @return segment ship key
   */
  public static String getStorageFilename(Segment segment, String extension) {
    return String.format("%s%s%s", segment.getStorageKey(), EXTENSION_SEPARATOR, extension);
  }

  /**
   * Get the full storage key for a segment audio
   *
   * @param segment for which to get storage key
   * @return storage key for segment
   */
  public static String getStorageFilename(Segment segment) {
    return getStorageFilename(segment, WAV_EXTENSION);
  }

  /**
   * @param choice to describe
   * @return description of choice
   */
  public static String describe(SegmentChoice choice) {
    List<String> pieces = Lists.newArrayList();
    if (Objects.nonNull(choice.getInstrumentId())) pieces.add(String.format("instrument:%s", choice.getInstrumentId()));
    if (Objects.nonNull(choice.getInstrumentType()))
      pieces.add(String.format("instrumentType:%s", choice.getInstrumentType()));
    if (Objects.nonNull(choice.getProgramId())) pieces.add(String.format("program:%s", choice.getProgramId()));
    if (Objects.nonNull(choice.getProgramSequenceBindingId()))
      pieces.add(String.format("programSequenceBinding:%s", choice.getProgramSequenceBindingId()));
    if (Objects.nonNull(choice.getProgramSequenceId()))
      pieces.add(String.format("programSequence:%s", choice.getProgramSequenceId()));
    if (Objects.nonNull(choice.getProgramType())) pieces.add(String.format("programType:%s", choice.getProgramType()));
    if (Objects.nonNull(choice.getProgramVoiceId()))
      pieces.add(String.format("programVoice:%s", choice.getProgramVoiceId()));
    return CSV.join(pieces);
  }

  public static long getEndAtChainMicros(Segment segment) {
    return Objects.nonNull(segment.getDurationMicros()) ? segment.getBeginAtChainMicros() + segment.getDurationMicros() : segment.getBeginAtChainMicros();
  }
}
