// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.music;

import com.google.api.client.util.Lists;
import io.xj.lib.util.TremendouslyRandom;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 Segment has metadata for XJ to persist "notes in the margin" of the composition for itself to read https://www.pivotaltracker.com/story/show/183135787
 */
public class StickyBun {
  private static final String META_KEY_TEMPLATE = "StickyBun_%s";
  private static final int MAX_VALUE = 99;
  private List<Integer> values;
  private UUID eventId;

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
   Prepare a sticky bun with only an event id

   @param eventId to persist
   */
  public StickyBun(UUID eventId) {
    this.values = List.of();
    this.eventId = eventId;
  }

  /**
   Compute a meta key based on the given event id
   <p>
   Segment has metadata for XJ to persist "notes in the margin" of the composition for itself to read https://www.pivotaltracker.com/story/show/183135787

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
    var voicingRange = NoteRange.ofNotes(voicingNotes);
    if (values.isEmpty()) return source;
    if (voicingRange.getSpan().isEmpty()) return source;

    List<Note> notes = Lists.newArrayList(source);

    for (var i = 0; i < notes.size(); i++)
      if (notes.get(i).isAtonal()) {
        var v = values.get(Math.min(i, values.size() - 1)) / MAX_VALUE;
        var t = voicingRange.getLow().orElseThrow().shift(v * voicingRange.getSpan().orElseThrow());
        var n = voicingNotes.stream().min(Comparator.comparingInt(a -> a.delta(t)));
        if (n.isPresent())
          notes.set(i, n.get());
      }

    return notes;
  }

  public String computeMetaKey() {
    return StickyBun.computeMetaKey(eventId);
  }
}
