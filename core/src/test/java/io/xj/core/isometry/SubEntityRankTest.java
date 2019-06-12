// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.isometry;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;
import io.xj.core.model.program.sub.Sequence;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SubEntityRankTest {
  private SubEntityRank<Sequence> entityRank;

  private Sequence sequenceB;
  private Sequence sequenceC;
  private Sequence sequenceD;
  private Sequence sequenceE;

  @Before
  public void setUp() {
    sequenceB = new Sequence();
    sequenceB.setId(UUID.randomUUID());
    sequenceB.setName("Super Jam");

    sequenceC = new Sequence();
    sequenceC.setId(UUID.randomUUID());
    sequenceC.setName("Dope Beat");

    sequenceD = new Sequence();
    sequenceD.setId(UUID.randomUUID());
    sequenceD.setName("The Illest");

    sequenceE = new Sequence();
    sequenceE.setId(UUID.randomUUID());
    sequenceE.setName("Good News");

    entityRank = new SubEntityRank<>();

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
    SubEntityRank<Sequence> result = new SubEntityRank<>();
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
      new Sequence[]{
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
      new Sequence[]{
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
      new Sequence[]{
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
      new Sequence[]{
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
  public void getTop() throws CoreException {
    assertEquals(sequenceB, entityRank.getTop());
  }

  @Test
  public void getScored() {
    assertArrayEquals(
      new Sequence[]{
        sequenceB,
        sequenceD
      },
      entityRank.getScored(2).toArray()
    );
  }

  @Test
  public void getAllScored() {
    assertArrayEquals(
      new Sequence[]{
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
