// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.isometry;

import com.google.common.collect.Lists;
import io.xj.core.model.event.Event;
import io.xj.core.model.pattern_event.PatternEvent;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import org.apache.commons.codec.language.DoubleMetaphone;

import java.util.List;
import java.util.Map;

/**
 Determine the isometry between a source and target group of Events
 */
public class EventIsometry extends Isometry {
  private static final double SIMILARITY_SCORE_MATCHING_INFLECTION = 3;

  /**
   Instantiate a new EventIsometry from a group of source Events

   @param sourceEvents to compare from
   @return EventIsometry ready for comparison to target Events
   */
  public static <R extends Event> EventIsometry ofEvents(Iterable<R> sourceEvents) {
    EventIsometry result = new EventIsometry();
    sourceEvents.forEach(event ->
      result.addPhonetic(event.getInflection()));
    return result;
  }


  /**
   Instantiate a new EventIsometry from a map of source Events

   @param stringEventMap to compare from
   @return EventIsometry ready for comparison to target Events
   */
  public static EventIsometry ofEvents(Map<String, Event> stringEventMap) {
    List<Event> sourceEvents = Lists.newArrayList();

    // use Event as a generic event-- we could use any extender of Event
    stringEventMap.forEach((key, record) -> sourceEvents.add(
      new PatternEvent().setInflection(record.getInflection())
    ));

    return ofEvents(sourceEvents);
  }

  /**
   [#252] Similarity between two events implements Double Metaphone phonetic similarity algorithm

   @param event1 to compare
   @param event2 to compare
   @return score
   */
  public static double similarity(Event event1, Event event2) {
    double score = 0;
    DoubleMetaphone dm = new DoubleMetaphone();

    // score includes double-metaphone phonetic fuzzy-match of inflection
    score += SIMILARITY_SCORE_MATCHING_INFLECTION * FuzzySearch.ratio(
      dm.doubleMetaphone(event1.getInflection()),
      dm.doubleMetaphone(event2.getInflection())
    );

    return score;
  }

  /**
   Add an event for isometry comparison

   @param source to add
   */
  public <R extends Event> void add(R source) {
    addPhonetic(source.getInflection());
  }
}
