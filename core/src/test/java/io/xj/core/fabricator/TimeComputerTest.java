//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.fabricator;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.xj.core.CoreModule;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 [#153542275] Segment wherein velocity changes expect perfectly smooth sound from previous segment through to following segment
 */
public class TimeComputerTest {
  TimeComputer subject;
  private TimeComputerFactory timeComputerFactory;

  @Before
  public void setUp() {
    Injector injector = Guice.createInjector(new CoreModule());
    timeComputerFactory = injector.getInstance(TimeComputerFactory.class);
  }

  @Test
  public void getSecondsAtPosition() {
    subject = timeComputerFactory.create(16, 60, 60);

    assertEquals(-1, subject.getSecondsAtPosition(-1), 0.000001); // before start of segment
    assertEquals(0, subject.getSecondsAtPosition(0), 0.000001); // start of segment
    assertEquals(1, subject.getSecondsAtPosition(1), 0.000001); // one beat in to segment
    assertEquals(8, subject.getSecondsAtPosition(8), 0.000001); // middle of segment
    assertEquals(16, subject.getSecondsAtPosition(16), 0.000001); // end of segment
    assertEquals(20, subject.getSecondsAtPosition(20), 0.000001); // after end of segment
  }

  @Test
  public void getSecondsAtPosition_tempoAccelerating() {
    subject = timeComputerFactory.create(16, 60, 120);

    assertEquals(-1, subject.getSecondsAtPosition(-1), 0.000001); // before start of segment
    assertEquals(0, subject.getSecondsAtPosition(0), 0.000001); // start of segment
    assertEquals(0.984619, subject.getSecondsAtPosition(1), 0.000001); // one beat in to segment
    assertEquals(7.001953, subject.getSecondsAtPosition(8), 0.000001); // middle of segment
    assertEquals(12.003906, subject.getSecondsAtPosition(16), 0.000001); // end of segment
    assertEquals(14.003906, subject.getSecondsAtPosition(20), 0.000001); // after end of segment
  }

  @Test
  public void getSecondsAtPosition_tempoDecelerating() {
    subject = timeComputerFactory.create(16, 60, 30);

    assertEquals(-1, subject.getSecondsAtPosition(-1), 0.000001); // before start of segment
    assertEquals(0, subject.getSecondsAtPosition(0), 0.000001); // start of segment
    assertEquals(1.030761, subject.getSecondsAtPosition(1), 0.000001); // one beat in to segment
    assertEquals(9.996093, subject.getSecondsAtPosition(8), 0.000001); // middle of segment
    assertEquals(23.992187, subject.getSecondsAtPosition(16), 0.000001); // end of segment
    assertEquals(31.992187, subject.getSecondsAtPosition(20), 0.000001); // after end of segment
  }

}
