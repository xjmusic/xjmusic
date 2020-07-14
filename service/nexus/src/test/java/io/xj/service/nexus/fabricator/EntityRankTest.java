// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.fabricator;

import com.google.common.collect.ImmutableList;
import io.xj.service.hub.entity.ProgramSequence;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

public class EntityRankTest {
  private EntityRank<ProgramSequence> entityRank;

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

    entityRank = new EntityRank<>();

    entityRank.add(sequenceB, 0.75);
    entityRank.add(sequenceC, 0.25);
    entityRank.add(sequenceD, 0.5);
  }

  @Test
  public void add() {
    entityRank.add(sequenceE);

    assertTrue(entityRank.getAll().contains(sequenceE));
  }

  @Test
  public void addAll() {
    EntityRank<ProgramSequence> result = new EntityRank<>();
    result.addAll(
      ImmutableList.of(sequenceB, sequenceC, sequenceD));

    assertTrue(entityRank.getAll().contains(sequenceB));
    assertTrue(entityRank.getAll().contains(sequenceC));
    assertTrue(entityRank.getAll().contains(sequenceD));
  }

  @Test
  public void addWithScore() {
    entityRank.add(sequenceE, 2.0);

    assertArrayEquals(
      new ProgramSequence[]{
        sequenceB,
        sequenceC,
        sequenceD,
        sequenceE
      },
      entityRank.getAll().toArray()
    );
  }

  @Test
  public void score() {
    entityRank.score(sequenceC, 2.0);

    assertArrayEquals(
      new ProgramSequence[]{
        sequenceC,
        sequenceB,
        sequenceD
      },
      entityRank.getAllScored().toArray()
    );
  }

  @Test
  public void scoreById() {
    entityRank.score(sequenceC.getId(), 2.0);

    assertArrayEquals(
      new ProgramSequence[]{
        sequenceC,
        sequenceB,
        sequenceD
      },
      entityRank.getAllScored().toArray()
    );
  }

  @Test
  public void getAll() {
    assertArrayEquals(
      new ProgramSequence[]{
        sequenceB,
        sequenceC,
        sequenceD
      },
      entityRank.getAll().toArray()
    );
  }

  @Test
  public void score_adjustExisting() {
    entityRank.score(sequenceC, 2.0);

    Map<UUID, Double> result = entityRank.getScores();
    assertEquals(Double.valueOf(2.25), result.get(sequenceC.getId()));
  }


  @Test
  public void getScores() {
    Map<UUID, Double> result = entityRank.getScores();

    assertEquals(Double.valueOf(0.75), result.get(sequenceB.getId()));
    assertEquals(Double.valueOf(0.25), result.get(sequenceC.getId()));
    assertEquals(Double.valueOf(0.5), result.get(sequenceD.getId()));
  }

  @Test
  public void getTop() throws FabricationException {
    assertEquals(sequenceB, entityRank.getTop());
  }

  @Test
  public void getScored() {
    assertArrayEquals(
      new ProgramSequence[]{
        sequenceB,
        sequenceD
      },
      entityRank.getScored(2).toArray()
    );
  }

  @Test
  public void getAllScored() {
    assertArrayEquals(
      new ProgramSequence[]{
        sequenceB,
        sequenceD,
        sequenceC
      },
      entityRank.getAllScored().toArray()
    );
  }

  @Test
  public void size() {
    assertEquals(3L, entityRank.size());
  }

}
