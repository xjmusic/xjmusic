// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.model.choice;

import io.outright.xj.core.model.Entity;

import org.jooq.types.ULong;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 Chooser is a collection of one type of Entity (e.g. Idea or Instrument),
 stored in a HashMap using its id as an index.
 <p>
 A separate quality-score (Q-score) index uses the entity id as an index
 for a double that represents that entity's score.

 @param <T>  */
public class Chooser<T extends Entity> {
  //  final TypeToken<T> type = new TypeToken<T>(getClass()) {};
  private List<T> entities;
  private HashMap<ULong, Double> scores;

  /**
   Constructor instantiates a new inner hash map
   */
  public Chooser() {
    this.entities = Lists.newArrayList();
    this.scores = Maps.newHashMap();
  }

  /**
   Add an entity

   @param entity to add
   */
  public void add(T entity) {
    this.entities.add(entity);
  }

  /**
   Add an entity, with a Q-score

   @param entity to add
   */
  public void add(T entity, double Q) {
    this.add(entity);
    this.score(entity, Q);
  }

  /**
   Set the Q-score for an entity

   @param entity to add
   */
  public void score(T entity, Double Q) {
    scores.put(entity.getId(), Q);
  }

  /**
   Set the Q-score by entity id

   @param entityId to add
   */
  public void score(ULong entityId, Double Q) {
    scores.put(entityId, Q);
  }

  /**
   Get a collection of all the stored entities

   @return all entities
   */
  public List<T> getAll() {
    return entities;
  }

  /**
   Get a collection of all the stored entities

   @return all entities
   */
  public HashMap<ULong, Double> getScores() {
    return scores;
  }

  /**
   Get the top entity by Q-score

   @return T
   */
  @Nullable
  public T getTop() {
    List<T> allScored = getAllScored();
    if (Objects.nonNull(allScored) && allScored.size() >= 1)
      return allScored.get(0);
    else
      return null;
  }

  /**
   Get the top entities by Q-score

   @param total quantity to return
   @return T
   */
  public List<T> getScored(int total) {
    return getAllScored().subList(0, total);
  }

  /**
   Get all entities sorted by Q-score

   @return T
   */
  public List<T> getAllScored() {
    entities.sort(
      Comparator.comparing(
        e -> -scores.get(e.getId())
      )
    );
    return entities;
  }

  /**
   Total number of entities stored

   @return total number of entities
   */
  public int size() {
    return entities.size();
  }

  /**
   assemble report of choices and scores

   @return report
   */
  public String report() {
    if (Objects.isNull(entities) || entities.size() == 0)
      return "(empty)";
    String name = entities.get(0).getClass().getSimpleName();
    List<String> reports = Lists.newArrayList();
    scores.forEach((id, score) -> {
      reports.add(String.format("%s:%f", id, score));
    });
    return "(" + name + ") " + String.join(", ", reports);
  }
}
