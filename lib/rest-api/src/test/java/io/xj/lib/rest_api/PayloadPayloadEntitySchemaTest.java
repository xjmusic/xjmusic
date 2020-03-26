// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.rest_api;

import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PayloadPayloadEntitySchemaTest {
  private PayloadEntitySchema subject;

  @Before
  public void setUp() {
    subject = PayloadEntitySchema.of("FunkyChicken")
            .createdBy(MockEntity::new)
            .withAttributes("Weight", "DancingAbility")
            .belongsTo("Barnyard", "LocalUnion")
            .hasMany("Talon", "TailFeather");
  }

  @Test
  public void of() {
    assertEquals("tall-mountains", PayloadEntitySchema.of("TallMountain").getType());
  }

  @Test
  public void getType() {
    assertEquals("funky-chickens", subject.getType());
  }

  @Test
  public void createdBy() {
    subject.createdBy(() -> {
      MockEntity test = new MockEntity();
      test.setName("ThisIsATest");
      return test;
    });

    MockEntity created = (MockEntity) subject.getCreator().get();
    assertEquals("ThisIsATest", created.getName());
  }

  @Test
  public void getCreator() {
    Object result = subject.getCreator().get();

    assertEquals(MockEntity.class, result.getClass());
  }

  @Test
  public void withAttribute() {
    assertEquals(ImmutableSet.of("weight", "dancingAbility", "overallFunkiness"),
            subject
                    .withAttribute("OverallFunkiness")
                    .getAttributes());
  }

  @Test
  public void withAttributes() {
    assertEquals(ImmutableSet.of("weight", "dancingAbility", "overallFunkiness", "debateAbility"),
            subject
                    .withAttributes("overallFunkiness", "DebateAbility")
                    .getAttributes());
  }

  @Test
  public void getAttributes() {
    assertEquals(ImmutableSet.of("weight", "dancingAbility"), subject.getAttributes());
  }

  @Test
  public void hasMany() {
    assertEquals(ImmutableSet.of("talons", "tailFeathers", "eyeBalls"),
            subject
                    .hasMany("EyeBall")
                    .getHasMany());
  }

  @Test
  public void hasMany_plural() {
    assertEquals(ImmutableSet.of("talons", "tailFeathers", "eyeBalls", "shoes"),
            subject
                    .hasMany("EyeBall", "Shoe")
                    .getHasMany());
  }

  @Test
  public void hasMany_byClass() {
    assertEquals(ImmutableSet.of("talons", "tailFeathers", "eyeBalls"),
            subject
                    .hasMany(EyeBall.class)
                    .getHasMany());
  }

  @Test
  public void hasMany_plural_byClass() {
    assertEquals(ImmutableSet.of("talons", "tailFeathers", "eyeBalls", "shoes"),
            subject
                    .hasMany(EyeBall.class, Shoe.class)
                    .getHasMany());
  }

  @Test
  public void getHasMany() {
    assertEquals(ImmutableSet.of("talons", "tailFeathers"), subject.getHasMany());
  }

  @Test
  public void belongsTo() {
    assertEquals(ImmutableSet.of("barnyard", "localUnion", "city"),
            subject
                    .belongsTo("cities")
                    .getBelongsTo());
  }

  @Test
  public void belongsTo_plural() {
    assertEquals(ImmutableSet.of("barnyard", "localUnion", "city", "state"),
            subject
                    .belongsTo("Cities", "States")
                    .getBelongsTo());
  }

  @Test
  public void belongsTo_byClass() {
    assertEquals(ImmutableSet.of("barnyard", "localUnion", "city"),
            subject
                    .belongsTo(City.class)
                    .getBelongsTo());
  }

  @Test
  public void belongsTo_plural_byClass() {
    assertEquals(ImmutableSet.of("barnyard", "localUnion", "city", "state"),
            subject
                    .belongsTo(City.class, State.class)
                    .getBelongsTo());
  }

  @Test
  public void getBelongsTo() {
    assertEquals(ImmutableSet.of("barnyard", "localUnion"), subject.getBelongsTo());
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
