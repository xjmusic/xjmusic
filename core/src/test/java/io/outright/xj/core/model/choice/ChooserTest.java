// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.model.choice;

import io.outright.xj.core.model.idea.Idea;
import io.outright.xj.core.model.choice.Chooser;

import org.jooq.types.ULong;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.util.HashMap;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ChooserTest {
  private Chooser<Idea> chooser;

  private Idea ideaB;
  private Idea ideaC;
  private Idea ideaD;
  private Idea ideaE;

  @Before
  public void setUp() throws Exception {
    ideaB = new Idea();
    ideaB.setId(BigInteger.valueOf(5));
    ideaB.setName("Super Jam");

    ideaC = new Idea();
    ideaC.setId(BigInteger.valueOf(12));
    ideaC.setName("Dope Beat");

    ideaD = new Idea();
    ideaD.setId(BigInteger.valueOf(15));
    ideaD.setName("The Illest");

    ideaE = new Idea();
    ideaE.setId(BigInteger.valueOf(22));
    ideaE.setName("Good News");

    chooser = new Chooser<>();

    chooser.add(ideaB, 0.75);
    chooser.add(ideaC, 0.25);
    chooser.add(ideaD, 0.5);
  }

  @After
  public void tearDown() throws Exception {
    chooser = null;
    ideaB = null;
    ideaC = null;
    ideaD = null;
    ideaE = null;
  }

  @Test
  public void add() throws Exception {
    chooser.add(ideaE);

    assert chooser.getAll().contains(ideaE);
  }

  @Test
  public void addWithScore() throws Exception {
    chooser.add(ideaE, 2.0);

    assertArrayEquals(
      new Idea[]{
        ideaB,
        ideaC,
        ideaD,
        ideaE
      },
      chooser.getAll().toArray()
    );
  }

  @Test
  public void score() throws Exception {
    chooser.score(ideaC, 2.0);

    assertArrayEquals(
      new Idea[]{
        ideaC,
        ideaB,
        ideaD
      },
      chooser.getAllScored().toArray()
    );
  }

  @Test
  public void scoreById() throws Exception {
    chooser.score(ULong.valueOf(12), 2.0);

    assertArrayEquals(
      new Idea[]{
        ideaC,
        ideaB,
        ideaD
      },
      chooser.getAllScored().toArray()
    );
  }

  @Test
  public void getAll() throws Exception {
    assertArrayEquals(
      new Idea[]{
        ideaB,
        ideaC,
        ideaD
      },
      chooser.getAll().toArray()
    );
  }

  @Test
  public void getScores() throws Exception {
    HashMap<ULong, Double> result = chooser.getScores();

    assertEquals(Double.valueOf(0.75), result.get(ULong.valueOf(5)));
    assertEquals(Double.valueOf(0.25), result.get(ULong.valueOf(12)));
    assertEquals(Double.valueOf(0.5), result.get(ULong.valueOf(15)));
  }

  @Test
  public void getTop() throws Exception {
    assertEquals(ideaB, chooser.getTop());
  }

  @Test
  public void getScored() throws Exception {
    assertArrayEquals(
      new Idea[]{
        ideaB,
        ideaD
      },
      chooser.getScored(2).toArray()
    );
  }

  @Test
  public void getAllScored() throws Exception {
    assertArrayEquals(
      new Idea[]{
        ideaB,
        ideaD,
        ideaC
      },
      chooser.getAllScored().toArray()
    );
  }

  @Test
  public void size() throws Exception {
    assertEquals(3, chooser.size());
  }

}
