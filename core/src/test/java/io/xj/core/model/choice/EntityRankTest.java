// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.choice;

import io.xj.core.model.entity.EntityRank;
import io.xj.core.model.sequence.Sequence;

import com.google.common.collect.ImmutableList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EntityRankTest {
  private EntityRank<Sequence> entityRank;

  private Sequence sequenceB;
  private Sequence sequenceC;
  private Sequence sequenceD;
  private Sequence sequenceE;

  @Before
  public void setUp() throws Exception {
    sequenceB = new Sequence();
    sequenceB.setId(BigInteger.valueOf(5L));
    sequenceB.setName("Super Jam");

    sequenceC = new Sequence();
    sequenceC.setId(BigInteger.valueOf(12L));
    sequenceC.setName("Dope Beat");

    sequenceD = new Sequence();
    sequenceD.setId(BigInteger.valueOf(15L));
    sequenceD.setName("The Illest");

    sequenceE = new Sequence();
    sequenceE.setId(BigInteger.valueOf(22L));
    sequenceE.setName("Good News");

    entityRank = new EntityRank<>();

    entityRank.add(sequenceB, 0.75);
    entityRank.add(sequenceC, 0.25);
    entityRank.add(sequenceD, 0.5);
  }

  @After
  public void tearDown() throws Exception {
    entityRank = null;
    sequenceB = null;
    sequenceC = null;
    sequenceD = null;
    sequenceE = null;
  }

  @Test
  public void add() throws Exception {
    entityRank.add(sequenceE);

    assertTrue(entityRank.getAll().contains(sequenceE));
  }

  @Test
  public void addAll() throws Exception {
    EntityRank<Sequence> result = new EntityRank<>();
    result.addAll(
      ImmutableList.of(sequenceB, sequenceC, sequenceD));

    assertTrue(entityRank.getAll().contains(sequenceB));
    assertTrue(entityRank.getAll().contains(sequenceC));
    assertTrue(entityRank.getAll().contains(sequenceD));
  }

  @Test
  public void addWithScore() throws Exception {
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
  public void score() throws Exception {
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
  public void scoreById() throws Exception {
    entityRank.score(BigInteger.valueOf(12L), 2.0);

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
  public void getAll() throws Exception {
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
  public void score_adjustExisting() throws Exception {
    entityRank.score(sequenceC, 2.0);

    Map<BigInteger, Double> result = entityRank.getScores();
    assertEquals(Double.valueOf(2.25), result.get(BigInteger.valueOf(12L)));
  }


  @Test
  public void getScores() throws Exception {
    Map<BigInteger, Double> result = entityRank.getScores();

    assertEquals(Double.valueOf(0.75), result.get(BigInteger.valueOf(5L)));
    assertEquals(Double.valueOf(0.25), result.get(BigInteger.valueOf(12L)));
    assertEquals(Double.valueOf(0.5), result.get(BigInteger.valueOf(15L)));
  }

  @Test
  public void getTop() throws Exception {
    assertEquals(sequenceB, entityRank.getTop());
  }

  @Test
  public void getScored() throws Exception {
    assertArrayEquals(
      new Sequence[]{
        sequenceB,
        sequenceD
      },
      entityRank.getScored(2).toArray()
    );
  }

  @Test
  public void getAllScored() throws Exception {
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
  public void size() throws Exception {
    assertEquals(3L, (long) entityRank.size());
  }

}
