// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.isometry;

import io.xj.core.model.event.Event;
import io.xj.core.model.pattern_event.PatternEvent;

import com.google.common.collect.Lists;

import me.xdrop.fuzzywuzzy.FuzzySearch;
import org.apache.commons.codec.language.DoubleMetaphone;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 Determine the isometry between a source and target group of Events
 */
public class EventIsometry {
  private static final double SIMILARITY_SCORE_MATCHING_INFLECTION = 3;
  private static final double SIMILARITY_SCORE_VELOCITY = 0.2;
  private static final double SIMILARITY_SCORE_DURATION = 0.3;
  private static final double SIMILARITY_SCORE_TONALITY = 0.6;
  private final List<String> sourceStems;

  /**
   Private constructor

   @param sourceEvents source group of events
   */
  private EventIsometry(Iterable<? extends Event> sourceEvents) {
    sourceStems = Lists.newArrayList();
    sourceEvents.forEach(event ->
      sourceStems.add(phonetic(event.getInflection())));
  }

  /**
   Instantiate a new EventIsometry from a group of source Events

   @param sourceEvents to compare from
   @return EventIsometry ready for comparison to target Events
   */
  public static EventIsometry of(Collection<? extends Event> sourceEvents) {
    return new EventIsometry(sourceEvents);
  }

  /**
   Instantiate a new EventIsometry from a group of source Events,
   as expressed in a a Result of jOOQ records.

   @param sourceEventRecords to compare from
   @return EventIsometry ready for comparison to target Events
   */
  public static <R extends Event> EventIsometry of(Iterable<R> sourceEventRecords) {
    List<Event> sourceEvents = Lists.newArrayList();

    // use Event as a generic event-- we could use any extender of Event
    sourceEventRecords.forEach(record -> sourceEvents.add(
      new PatternEvent().setInflection(String.valueOf(record.getInflection()))
    ));

    return new EventIsometry(sourceEvents);
  }

  /**
   Instantiate a new EventIsometry from a map of source Events

   @param stringEventMap to compare from
   @return EventIsometry ready for comparison to target Events
   */
  public static EventIsometry of(Map<String, Event> stringEventMap) {
    List<Event> sourceEvents = Lists.newArrayList();

    // use Event as a generic event-- we could use any extender of Event
    stringEventMap.forEach((key, record) -> sourceEvents.add(
      new PatternEvent().setInflection(record.getInflection())
    ));

    return new EventIsometry(sourceEvents);
  }

  /*
   Get the source Events

   @return source events

  List<String> getSourceStems() {
    return Collections.unmodifiableList(sourceStems);
  }
  */

  /*
   Score a CSV list of events based on isometry to source events

   @param eventsCSV comma-separated values to score against source event inflections
   @return score is between 0 (no matches) and 1 (all events match)


  public double scoreCSV(String eventsCSV) {
    Collection<Event> events = Lists.newArrayList();
    CSV.split(eventsCSV).forEach((inflection) -> {
      events.add(new Event().setInflection(inflection)); // can use any event impl
    });
    return score(events);
  }
*/

  /*
   Score a CSV list of events based on isometry to source events

   @param targetEvents comma-separated values to score against source event inflections
   @return score is between 0 (no matches) and 1 (all events match)

  public <M extends Event> double score(Iterable<M> targetEvents) {
    double tally = 0;

    // tally each match of source & target phonetic
    for (M targetEvent : targetEvents) {

      String targetStem = phonetic(targetEvent.getInflection());
      for (String sourceStem : sourceStems) {
        if (Objects.equal(sourceStem, targetStem)) {
          tally += 1;
        }
      }
    }
    return tally / sourceStems.size();
  }
  */

  /**
   Double metaphone phonetic of a particular word

   @param raw text to get phonetic of
   @return phonetic
   */
  private static String phonetic(String raw) {
    DoubleMetaphone dm = new DoubleMetaphone();
    return dm.doubleMetaphone(raw);
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

/*
    // FUTURE: score velocity similarity?
    score += (1 - Math.abs(event2.getVelocity() - event1.getVelocity())) * SIMILARITY_SCORE_VELOCITY;

    // FUTURE: score duration similarity?
    score += (1 - Math.abs(event2.getDuration() - event1.getDuration())) * SIMILARITY_SCORE_DURATION;

    // FUTURE: score tonality similarity?
    score += (1 - Math.abs(event2.getTonality() - event1.getTonality())) * SIMILARITY_SCORE_TONALITY;
*/

    return score;
  }
}
