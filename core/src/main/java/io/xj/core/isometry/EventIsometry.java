//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.isometry;

import com.google.common.collect.Lists;
import io.xj.core.model.entity.Event;
import io.xj.core.model.program.sub.PatternEvent;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import org.apache.commons.codec.language.DoubleMetaphone;

import java.util.List;
import java.util.Map;

/**
 Determine the isometry between a source and target group of Events
 */
public class EventIsometry extends Isometry {
  private static final double SIMILARITY_SCORE_MATCHING_NAME = 3;

  /**
   Instantiate a new EventIsometry from a group of source Events

   @param sourceEvents to compare from
   @return EventIsometry ready for comparison to target Events
   */
  public static <V extends Event> EventIsometry ofEvents(Iterable<V> sourceEvents) {
    EventIsometry result = new EventIsometry();
    sourceEvents.forEach(event ->
      result.addPhonetic(event.getName()));
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
      new PatternEvent().setName(record.getName())
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

    // score includes double-metaphone phonetic fuzzy-match of name
    score += SIMILARITY_SCORE_MATCHING_NAME * FuzzySearch.ratio(
      dm.doubleMetaphone(event1.getName()),
      dm.doubleMetaphone(event2.getName())
    );

    return score;
  }

  /**
   Add an event for isometry comparison

   @param source to add
   */
  public <V extends Event> void add(V source) {
    addPhonetic(source.getName());
  }
}
