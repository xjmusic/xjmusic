// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.fabricator;

import com.google.common.collect.ImmutableList;
import io.xj.hub.tables.pojos.ProgramSequence;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EntityScorePickerTest {
  private EntityScorePicker<ProgramSequence> entityScorePicker;

  private ProgramSequence sequenceB;
  private ProgramSequence sequenceC;
  private ProgramSequence sequenceD;
  private ProgramSequence sequenceE;

  @Before
  public void setUp() {
    sequenceB = new ProgramSequence();
    sequenceB.setId(UUID.randomUUID());
    sequenceB.setName("Super Jam");

    sequenceC = new ProgramSequence();
    sequenceC.setId(UUID.randomUUID());
    sequenceC.setName("Dope Beat");

    sequenceD = new ProgramSequence();
    sequenceD.setId(UUID.randomUUID());
    sequenceD.setName("The Illest");

    sequenceE = new ProgramSequence();
    sequenceE.setId(UUID.randomUUID());
    sequenceE.setName("Good News");

    entityScorePicker = new EntityScorePicker<>();

    entityScorePicker.add(sequenceB, 0.75);
    entityScorePicker.add(sequenceC, 0.25);
    entityScorePicker.add(sequenceD, 0.5);
  }

  @Test
  public void add() {
    entityScorePicker.add(sequenceE);

    assertTrue(entityScorePicker.getAll().contains(sequenceE));
  }

  @Test
  public void addAll() {
    EntityScorePicker<ProgramSequence> result = new EntityScorePicker<>();
    result.addAll(
      ImmutableList.of(sequenceB, sequenceC, sequenceD));

    assertTrue(entityScorePicker.getAll().contains(sequenceB));
    assertTrue(entityScorePicker.getAll().contains(sequenceC));
    assertTrue(entityScorePicker.getAll().contains(sequenceD));
  }

  @Test
  public void addWithScore() {
    entityScorePicker.add(sequenceE, 2.0);

    assertArrayEquals(
      new ProgramSequence[]{
        sequenceB,
        sequenceC,
        sequenceD,
        sequenceE
      },
      entityScorePicker.getAll().toArray());
  }

  @Test
  public void score() {
    entityScorePicker.score(sequenceC, 2.0);

    assertArrayEquals(
      new ProgramSequence[]{
        sequenceC,
        sequenceB,
        sequenceD
      },
      entityScorePicker.getAllScored().toArray());
  }

  @Test
  public void scoreById() {
    entityScorePicker.score(sequenceC.getId(), 2.0);

    assertArrayEquals(
      new ProgramSequence[]{
        sequenceC,
        sequenceB,
        sequenceD
      },
      entityScorePicker.getAllScored().toArray());
  }

  @Test
  public void getAll() {
    assertArrayEquals(
      new ProgramSequence[]{
        sequenceB,
        sequenceC,
        sequenceD
      },
      entityScorePicker.getAll().toArray());
  }

  @Test
  public void score_adjustExisting() {
    entityScorePicker.score(sequenceC, 2.0);

    Map<UUID, Double> result = entityScorePicker.getScores();
    assertEquals(Double.valueOf(2.25), result.get(sequenceC.getId()));
  }


  @Test
  public void getScores() {
    Map<UUID, Double> result = entityScorePicker.getScores();

    assertEquals(Double.valueOf(0.75), result.get(sequenceB.getId()));
    assertEquals(Double.valueOf(0.25), result.get(sequenceC.getId()));
    assertEquals(Double.valueOf(0.5), result.get(sequenceD.getId()));
  }

  @Test
  public void getTop() {
    assertEquals(sequenceB, entityScorePicker.getTop().orElseThrow());
  }

  @Test
  public void getScored() {
    assertArrayEquals(
      new ProgramSequence[]{
        sequenceB,
        sequenceD
      },
      entityScorePicker.getScored(2).toArray());
  }

  @Test
  public void getAllScored() {
    assertArrayEquals(
      new ProgramSequence[]{
        sequenceB,
        sequenceD,
        sequenceC
      },
      entityScorePicker.getAllScored().toArray());
  }

  @Test
  public void size() {
    assertEquals(3L, entityScorePicker.size());
  }

}
