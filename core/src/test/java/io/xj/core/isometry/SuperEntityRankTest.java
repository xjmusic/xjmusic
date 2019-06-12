//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.isometry;

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.xj.core.CoreModule;
import io.xj.core.exception.CoreException;
import io.xj.core.model.program.Program;
import io.xj.core.model.program.ProgramFactory;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SuperEntityRankTest {
  private SuperEntityRank<Program> superEntityRank;
  private final Injector injector = Guice.createInjector(new CoreModule());
  private final ProgramFactory programFactory = injector.getInstance(ProgramFactory.class);

  private Program programB;
  private Program programC;
  private Program programD;
  private Program programE;

  @Before
  public void setUp() {
    programB = programFactory.newProgram(BigInteger.valueOf(5L));
    programB.setName("Super Jam");

    programC = programFactory.newProgram(BigInteger.valueOf(12L));
    programC.setName("Dope Beat");

    programD = programFactory.newProgram(BigInteger.valueOf(15L));
    programD.setName("The Illest");

    programE = programFactory.newProgram(BigInteger.valueOf(22L));
    programE.setName("Good News");

    superEntityRank = new SuperEntityRank<>();

    superEntityRank.add(programB, 0.75);
    superEntityRank.add(programC, 0.25);
    superEntityRank.add(programD, 0.5);
  }

  @Test
  public void add() {
    superEntityRank.add(programE);

    assertTrue(superEntityRank.getAll().contains(programE));
  }

  @Test
  public void addAll() {
    SuperEntityRank<Program> result = new SuperEntityRank<>();
    result.addAll(
      ImmutableList.of(programB, programC, programD));

    assertTrue(superEntityRank.getAll().contains(programB));
    assertTrue(superEntityRank.getAll().contains(programC));
    assertTrue(superEntityRank.getAll().contains(programD));
  }

  @Test
  public void addWithScore() {
    superEntityRank.add(programE, 2.0);

    assertArrayEquals(
      new Program[]{
        programB,
        programC,
        programD,
        programE
      },
      superEntityRank.getAll().toArray()
    );
  }

  @Test
  public void score() {
    superEntityRank.score(programC, 2.0);

    assertArrayEquals(
      new Program[]{
        programC,
        programB,
        programD
      },
      superEntityRank.getAllScored().toArray()
    );
  }

  @Test
  public void scoreById() {
    superEntityRank.score(BigInteger.valueOf(12L), 2.0);

    assertArrayEquals(
      new Program[]{
        programC,
        programB,
        programD
      },
      superEntityRank.getAllScored().toArray()
    );
  }

  @Test
  public void getAll() {
    assertArrayEquals(
      new Program[]{
        programB,
        programC,
        programD
      },
      superEntityRank.getAll().toArray()
    );
  }

  @Test
  public void score_adjustExisting() {
    superEntityRank.score(programC, 2.0);

    Map<BigInteger, Double> result = superEntityRank.getScores();
    assertEquals(Double.valueOf(2.25), result.get(BigInteger.valueOf(12L)));
  }


  @Test
  public void getScores() {
    Map<BigInteger, Double> result = superEntityRank.getScores();

    assertEquals(Double.valueOf(0.75), result.get(BigInteger.valueOf(5L)));
    assertEquals(Double.valueOf(0.25), result.get(BigInteger.valueOf(12L)));
    assertEquals(Double.valueOf(0.5), result.get(BigInteger.valueOf(15L)));
  }

  @Test
  public void getTop() throws CoreException {
    assertEquals(programB, superEntityRank.getTop());
  }

  @Test
  public void getScored() {
    assertArrayEquals(
      new Program[]{
        programB,
        programD
      },
      superEntityRank.getScored(2).toArray()
    );
  }

  @Test
  public void getAllScored() {
    assertArrayEquals(
      new Program[]{
        programB,
        programD,
        programC
      },
      superEntityRank.getAllScored().toArray()
    );
  }

  @Test
  public void size() {
    assertEquals(3L, superEntityRank.size());
  }

}
