// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.choice;

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

public class ChooserTest {
  private Chooser<Pattern> chooser;

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

    chooser = new Chooser<>();

    chooser.add(patternB, 0.75);
    chooser.add(patternC, 0.25);
    chooser.add(patternD, 0.5);
  }

  @After
  public void tearDown() throws Exception {
    chooser = null;
    patternB = null;
    patternC = null;
    patternD = null;
    patternE = null;
  }

  @Test
  public void add() throws Exception {
    chooser.add(patternE);

    assertTrue(chooser.getAll().contains(patternE));
  }

  @Test
  public void addAll() throws Exception {
    Chooser<Pattern> result = new Chooser<>();
    result.addAll(
      ImmutableList.of(patternB, patternC, patternD));

    assertTrue(chooser.getAll().contains(patternB));
    assertTrue(chooser.getAll().contains(patternC));
    assertTrue(chooser.getAll().contains(patternD));
  }

  @Test
  public void addWithScore() throws Exception {
    chooser.add(patternE, 2.0);

    assertArrayEquals(
      new Pattern[]{
        patternB,
        patternC,
        patternD,
        patternE
      },
      chooser.getAll().toArray()
    );
  }

  @Test
  public void score() throws Exception {
    chooser.score(patternC, 2.0);

    assertArrayEquals(
      new Pattern[]{
        patternC,
        patternB,
        patternD
      },
      chooser.getAllScored().toArray()
    );
  }

  @Test
  public void scoreById() throws Exception {
    chooser.score(BigInteger.valueOf(12), 2.0);

    assertArrayEquals(
      new Pattern[]{
        patternC,
        patternB,
        patternD
      },
      chooser.getAllScored().toArray()
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
      chooser.getAll().toArray()
    );
  }

  @Test
  public void score_adjustExisting() throws Exception {
    chooser.score(patternC, 2.0);

    Map<BigInteger, Double> result = chooser.getScores();
    assertEquals(Double.valueOf(2.25), result.get(BigInteger.valueOf(12)));
  }


  @Test
  public void getScores() throws Exception {
    Map<BigInteger, Double> result = chooser.getScores();

    assertEquals(Double.valueOf(0.75), result.get(BigInteger.valueOf(5)));
    assertEquals(Double.valueOf(0.25), result.get(BigInteger.valueOf(12)));
    assertEquals(Double.valueOf(0.5), result.get(BigInteger.valueOf(15)));
  }

  @Test
  public void getTop() throws Exception {
    assertEquals(patternB, chooser.getTop());
  }

  @Test
  public void getScored() throws Exception {
    assertArrayEquals(
      new Pattern[]{
        patternB,
        patternD
      },
      chooser.getScored(2).toArray()
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
      chooser.getAllScored().toArray()
    );
  }

  @Test
  public void size() throws Exception {
    assertEquals(3, chooser.size());
  }

}
