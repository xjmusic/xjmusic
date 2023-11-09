// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.entity;

import io.xj.nexus.Widget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EntitySchemaTest {
  EntitySchema subject;

  @BeforeEach
  public void setUp() {
    subject = EntitySchema.of("FunkyChicken")
      .createdBy(Widget::new)
      .withAttributes("Weight", "DancingAbility")
      .belongsTo("Barnyard", "LocalUnion")
      .hasMany("Talon", "TailFeather");
  }

  @Test
  public void of() {
    assertEquals("tall-mountains", EntitySchema.of("TallMountain").getType());
  }

  @Test
  public void getType() {
    assertEquals("funky-chickens", subject.getType());
  }

  @Test
  public void createdBy() {
    subject.createdBy(() ->
      new Widget()
        .setName("ThisIsATest")
    );

    Widget created = (Widget) subject.getCreator().get();
    assertEquals("ThisIsATest", created.getName());
  }

  @Test
  public void getCreator() {
    Object result = subject.getCreator().get();

    assertEquals(Widget.class, result.getClass());
  }

  @Test
  public void withAttribute() {
    assertEquals(Set.of("weight", "dancingAbility", "overallFunkiness"),
      subject
        .withAttribute("OverallFunkiness")
        .getAttributes());
  }

  @Test
  public void withAttributes() {
    assertEquals(Set.of("weight", "dancingAbility", "overallFunkiness", "debateAbility"),
      subject
        .withAttributes("overallFunkiness", "DebateAbility")
        .getAttributes());
  }

  @Test
  public void getAttributes() {
    assertEquals(Set.of("weight", "dancingAbility"), subject.getAttributes());
  }

  @Test
  public void hasMany() {
    assertEquals(Set.of("talons", "tailFeathers", "eyeBalls"),
      subject
        .hasMany("EyeBall")
        .getHasMany());
  }

  @Test
  public void hasMany_plural() {
    assertEquals(Set.of("talons", "tailFeathers", "eyeBalls", "shoes"),
      subject
        .hasMany("EyeBall", "Shoe")
        .getHasMany());
  }

  @Test
  public void hasMany_byClass() {
    assertEquals(Set.of("talons", "tailFeathers", "eyeBalls"),
      subject
        .hasMany(EyeBall.class)
        .getHasMany());
  }

  @Test
  public void hasMany_plural_byClass() {
    assertEquals(Set.of("talons", "tailFeathers", "eyeBalls", "shoes"),
      subject
        .hasMany(EyeBall.class, Shoe.class)
        .getHasMany());
  }

  @Test
  public void getHasMany() {
    assertEquals(Set.of("talons", "tailFeathers"), subject.getHasMany());
  }

  @Test
  public void belongsTo() {
    assertEquals(Set.of("barnyard", "localUnion", "city"),
      subject
        .belongsTo("cities")
        .getBelongsTo());
  }

  @Test
  public void belongsTo_plural() {
    assertEquals(Set.of("barnyard", "localUnion", "city", "state"),
      subject
        .belongsTo("Cities", "States")
        .getBelongsTo());
  }

  @Test
  public void belongsTo_byClass() {
    assertEquals(Set.of("barnyard", "localUnion", "city"),
      subject
        .belongsTo(City.class)
        .getBelongsTo());
  }

  @Test
  public void belongsTo_plural_byClass() {
    assertEquals(Set.of("barnyard", "localUnion", "city", "state"),
      subject
        .belongsTo(City.class, State.class)
        .getBelongsTo());
  }

  @Test
  public void getBelongsTo() {
    assertEquals(Set.of("barnyard", "localUnion"), subject.getBelongsTo());
  }


  /**
   To test defining relationships by class
   */
  public static class EyeBall {
    // noop
  }

  /**
   To test defining relationships by class
   */
  public static class Shoe {
    // noop
  }

  /**
   To test defining relationships by class
   */
  public static class City {
    // noop
  }

  /**
   To test defining relationships by class
   */
  public static class State {
    // noop
  }

}
