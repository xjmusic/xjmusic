// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.music;

import io.xj.hub.util.TremendouslyRandom;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 Segment has metadata for XJ to persist "notes in the margin" of the composition for itself to read https://github.com/xjmusic/workstation/issues/222
 */
public class StickyBun {
  static final String META_KEY_TEMPLATE = "StickyBun_%s";
  static final int MAX_VALUE = 100;
  List<Integer> values;
  UUID eventId;

  /**
   Prepare empty sticky bun
   */
  public StickyBun() {
    this.values = List.of();
    this.eventId = null;
  }

  /**
   Prepare a sticky bun with event id and values

   @param eventId to persist
   @param size    of bun to generate
   */
  public StickyBun(UUID eventId, int size) {
    this.eventId = eventId;
    this.values = IntStream.rangeClosed(1, size)
      .boxed().map(i -> TremendouslyRandom.zeroToLimit(MAX_VALUE))
      .collect(Collectors.toList());
  }

  /**
   Prepare a sticky bun with event id and values

   @param eventId to persist
   @param values  of bun
   */
  public StickyBun(UUID eventId, List<Integer> values) {
    this.eventId = eventId;
    this.values = values;
  }

  /**
   Compute a meta key based on the given event id
   <p>
   Segment has metadata for XJ to persist "notes in the margin" of the composition for itself to read https://github.com/xjmusic/workstation/issues/222

   @return compute meta key
   */
  public static String computeMetaKey(UUID id) {
    return String.format(META_KEY_TEMPLATE, id.toString());
  }

  /**
   @return values
   */
  public List<Integer> getValues() {
    return values;
  }

  /**
   set values

   @param values to set
   */
  public StickyBun setValues(List<Integer> values) {
    this.values = values;
    return this;
  }

  /**
   @return event id
   */
  public UUID getEventId() {
    return eventId;
  }

  /**
   set event id

   @param eventId to set
   */
  public StickyBun setEventId(UUID eventId) {
    this.eventId = eventId;
    return this;
  }

  /**
   Replace atonal notes in the list with selections based on the sticky bun

   @param source       notes to replace atonal elements
   @param voicingNotes from which to select replacements
   @return notes with atonal elements augmented by sticky bun
   */
  public List<Note> replaceAtonal(List<Note> source, List<Note> voicingNotes) {
    if (values.isEmpty()) return source;

    List<Note> sourceNotes = new ArrayList<>(source);

    for (var i = 0; i < sourceNotes.size(); i++)
      if (sourceNotes.get(i).isAtonal()) {
        sourceNotes.set(i, compute(voicingNotes, i));
      }

    return sourceNotes;
  }

  /**
   Replace atonal notes in the list with selections based on the sticky bun

   @param voicingNotes from which to select replacements
   @return notes with atonal elements augmented by sticky bun
   */
  public Note compute(List<Note> voicingNotes, int index) {
    float valueRatio = (float) values.get(Math.min(index, values.size() - 1)) / MAX_VALUE;
    return voicingNotes.get((int) Math.max(0, Math.min(voicingNotes.size() - 1, Math.floor((voicingNotes.size() - 1) * valueRatio))));
  }

  public String computeMetaKey() {
    return StickyBun.computeMetaKey(eventId);
  }
}
