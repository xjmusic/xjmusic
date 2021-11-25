// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.persistence;

import com.google.common.base.Strings;
import io.xj.api.Segment;
import io.xj.api.SegmentChoice;
import io.xj.api.SegmentChordVoicing;
import io.xj.api.SegmentState;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramType;
import io.xj.lib.mixer.OutputEncoder;
import io.xj.lib.music.Note;
import io.xj.lib.util.CSV;
import io.xj.lib.util.Values;
import io.xj.nexus.NexusException;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 Utilities for working with segments
 */
public enum Segments {
  ;
  public static final int DELTA_UNLIMITED = -1;
  private static final long MILLIS_PER_SECOND = 1000;
  private static final String EXTENSION_SEPARATOR = ".";

  /**
   Find first segment choice of a given type in a collection of segment choices

   @param segmentChoices to filter from
   @param type           to find one of
   @return segment choice of given type
   */
  public static SegmentChoice findFirstOfType(Collection<SegmentChoice> segmentChoices, ProgramType type) throws NexusException {
    Optional<SegmentChoice> found = segmentChoices.stream().filter(c -> c.getProgramType().equals(type.toString())).findFirst();
    if (found.isEmpty()) throw new NexusException(String.format("No %s-type choice found", type));
    return found.get();
  }

  /**
   Find first segment choice of a given type in a collection of segment choices

   @param segmentChoices to filter from
   @param type           to find one of
   @return segment choice of given type
   */
  public static SegmentChoice findFirstOfType(Collection<SegmentChoice> segmentChoices, InstrumentType type) throws NexusException {
    Optional<SegmentChoice> found = segmentChoices.stream().filter(c -> c.getInstrumentType().equals(type.toString())).findFirst();
    if (found.isEmpty()) throw new NexusException(String.format("No %s-type choice found", type));
    return found.get();
  }

  /**
   Get the identifier or a Segment: ship key if available, else ID

   @param segment to get identifier of
   @return ship key if available, else ID
   */
  public static String getIdentifier(@Nullable Segment segment) {
    if (Objects.isNull(segment)) return "N/A";
    return Strings.isNullOrEmpty(segment.getStorageKey()) ? segment.getId().toString() : segment.getStorageKey();
  }


  /**
   Get the last dubbed from any collection of Segments

   @param segments to get last dubbed from
   @return last dubbed segment from collection
   */
  public static Optional<Segment> getLastDubbed(Collection<Segment> segments) {
    return getLast(getDubbed(segments));
  }

  /**
   Get the last from any collection of Segments

   @param segments to get last from
   @return last segment from collection
   */
  public static Optional<Segment> getLast(Collection<Segment> segments) {
    return segments
      .stream()
      .max(Comparator.comparing(Segment::getOffset));
  }

  /**
   Get only the dubbed from any collection of Segments

   @param segments to get dubbed from
   @return dubbed segments from collection
   */
  public static Collection<Segment> getDubbed(Collection<Segment> segments) {
    return segments
      .stream()
      .filter(segment -> SegmentState.DUBBED == segment.getState())
      .collect(Collectors.toList());
  }

  /**
   Get the length of a Segment in seconds

   @param segment for which to get length
   @return length of segment in seconds
   */
  public static float getLengthSeconds(Segment segment) {
    return (float) (Instant.parse(segment.getEndAt()).toEpochMilli() - Instant.parse(segment.getBeginAt()).toEpochMilli()) / MILLIS_PER_SECOND;
  }

  /**
   Get the ship key for a Segment

   @param segmentKey for which to get ship key
   @param extension  of key
   @return segment ship key
   */
  public static String getStorageFilename(String segmentKey, String extension) {
    return String.format("%s%s%s", segmentKey, EXTENSION_SEPARATOR, extension);
  }

  public static boolean containsAnyValidNotes(String notes) {
    return CSV.split(notes).stream().anyMatch(Note::isValid);
  }

  public static boolean containsAnyValidNotes(SegmentChordVoicing voicing) {
    return containsAnyValidNotes(voicing.getNotes());
  }

  /**
   Whether this Segment is before a given threshold, first by end-at if available, else begin-at

   @param eraseBefore threshold to filter before
   @return true if segment is before threshold
   */
  public static boolean isBefore(Segment segment, Instant eraseBefore) {
    return Values.isSet(segment.getEndAt()) ?
      Instant.parse(segment.getEndAt()).isBefore(eraseBefore) :
      Instant.parse(segment.getBeginAt()).isBefore(eraseBefore);
  }

  /**
   Get the full storage key for a segment audio

   @param segment for which to get storage key
   @return storage key for segment
   */
  public static String getStorageFilename(Segment segment) {
    return getStorageFilename(segment.getStorageKey(), segment.getOutputEncoder().toLowerCase(Locale.ENGLISH));
  }

  /**
   Get the full storage key for an uncompressed segment audio

   @param segment for which to get storage key
   @return storage key for segment
   */
  public static String getUncompressedStorageFilename(Segment segment) {
    return getStorageFilename(segment.getStorageKey(), OutputEncoder.WAV.name().toLowerCase(Locale.ENGLISH));
  }
}
