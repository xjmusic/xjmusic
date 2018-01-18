// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.choice;

import io.xj.core.model.entity.EntityRank;
import io.xj.core.model.pattern.Pattern;

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
  private EntityRank<Pattern> entityRank;

  private Pattern patternB;
  private Pattern patternC;
  private Pattern patternD;
  private Pattern patternE;

  @Before
  public void setUp() throws Exception {
    patternB = new Pattern();
    patternB.setId(BigInteger.valueOf(5));
    patternB.setName("Super Jam");

    patternC = new Pattern();
    patternC.setId(BigInteger.valueOf(12));
    patternC.setName("Dope Beat");

    patternD = new Pattern();
    patternD.setId(BigInteger.valueOf(15));
    patternD.setName("The Illest");

    patternE = new Pattern();
    patternE.setId(BigInteger.valueOf(22));
    patternE.setName("Good News");

    entityRank = new EntityRank<>();

    entityRank.add(patternB, 0.75);
    entityRank.add(patternC, 0.25);
    entityRank.add(patternD, 0.5);
  }

  @After
  public void tearDown() throws Exception {
    entityRank = null;
    patternB = null;
    patternC = null;
    patternD = null;
    patternE = null;
  }

  @Test
  public void add() throws Exception {
    entityRank.add(patternE);

    assertTrue(entityRank.getAll().contains(patternE));
  }

  @Test
  public void addAll() throws Exception {
    EntityRank<Pattern> result = new EntityRank<>();
    result.addAll(
      ImmutableList.of(patternB, patternC, patternD));

    assertTrue(entityRank.getAll().contains(patternB));
    assertTrue(entityRank.getAll().contains(patternC));
    assertTrue(entityRank.getAll().contains(patternD));
  }

  @Test
  public void addWithScore() throws Exception {
    entityRank.add(patternE, 2.0);

    assertArrayEquals(
      new Pattern[]{
        patternB,
        patternC,
        patternD,
        patternE
      },
      entityRank.getAll().toArray()
    );
  }

  @Test
  public void score() throws Exception {
    entityRank.score(patternC, 2.0);

    assertArrayEquals(
      new Pattern[]{
        patternC,
        patternB,
        patternD
      },
      entityRank.getAllScored().toArray()
    );
  }

  @Test
  public void scoreById() throws Exception {
    entityRank.score(BigInteger.valueOf(12), 2.0);

    assertArrayEquals(
      new Pattern[]{
        patternC,
        patternB,
        patternD
      },
      entityRank.getAllScored().toArray()
    );
  }

  @Test
  public void getAll() throws Exception {
    assertArrayEquals(
      new Pattern[]{
        patternB,
        patternC,
        patternD
      },
      entityRank.getAll().toArray()
    );
  }

  @Test
  public void score_adjustExisting() throws Exception {
    entityRank.score(patternC, 2.0);

    Map<BigInteger, Double> result = entityRank.getScores();
    assertEquals(Double.valueOf(2.25), result.get(BigInteger.valueOf(12)));
  }


  @Test
  public void getScores() throws Exception {
    Map<BigInteger, Double> result = entityRank.getScores();

    assertEquals(Double.valueOf(0.75), result.get(BigInteger.valueOf(5)));
    assertEquals(Double.valueOf(0.25), result.get(BigInteger.valueOf(12)));
    assertEquals(Double.valueOf(0.5), result.get(BigInteger.valueOf(15)));
  }

  @Test
  public void getTop() throws Exception {
    assertEquals(patternB, entityRank.getTop());
  }

  @Test
  public void getScored() throws Exception {
    assertArrayEquals(
      new Pattern[]{
        patternB,
        patternD
      },
      entityRank.getScored(2).toArray()
    );
  }

  @Test
  public void getAllScored() throws Exception {
    assertArrayEquals(
      new Pattern[]{
        patternB,
        patternD,
        patternC
      },
      entityRank.getAllScored().toArray()
    );
  }

  @Test
  public void size() throws Exception {
    assertEquals(3, entityRank.size());
  }

}
