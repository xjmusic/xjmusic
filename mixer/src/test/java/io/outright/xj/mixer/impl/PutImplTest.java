// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.mixer.impl;

import io.outright.xj.mixer.MixerFactory;
import io.outright.xj.mixer.MixerModule;
import io.outright.xj.mixer.Put;

import com.google.inject.Guice;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PutImplTest {

  private MixerFactory mixerFactory = Guice.createInjector(new MixerModule()).getInstance(MixerFactory.class);

  private Put testPut;

  @Before
  public void setUp() throws Exception {
    testPut = mixerFactory.createPut("bun1", 1000000, 2000000, 1.0, 1.0, 0);
  }

  @After
  public void tearDown() throws Exception {
    testPut = null;
  }

  @Test
  public void lifecycle() throws Exception {
    // before start:
    assertEquals(0, testPut.sourceOffsetMicros(5000));
    assertEquals(0, testPut.sourceOffsetMicros(500000));
    assertEquals(Put.READY, testPut.getState());
    assertTrue(testPut.isAlive());

    // start:
    assertEquals(0, testPut.sourceOffsetMicros(1000001));
    assertEquals(Put.PLAY, testPut.getState());
    assertTrue(testPut.isAlive());

    // after start / before end:
    for (int expectSourceOffsetMicros = 2;
         expectSourceOffsetMicros < 1000000; // never arrives at the DONE threshold
         expectSourceOffsetMicros += 1000) {
      assertEquals(expectSourceOffsetMicros,
        testPut.sourceOffsetMicros(1000000 + expectSourceOffsetMicros));
    }
    assertEquals(Put.PLAY, testPut.getState());
    assertTrue(testPut.isAlive());

    // end:
    assertEquals(1000001, testPut.sourceOffsetMicros(2000001));
    assertEquals(Put.DONE, testPut.getState());
    assertFalse(testPut.isAlive());

    // after end:
    assertEquals(0, testPut.sourceOffsetMicros(2000001));
  }

  @Test
  public void sourceOffsetMicros() throws Exception {
    assertEquals(0, testPut.sourceOffsetMicros(500000)); // before start
    assertEquals(0, testPut.sourceOffsetMicros(1000001)); // start
    assertEquals(500, testPut.sourceOffsetMicros(1000500)); // after start / before end
    assertEquals(1000001, testPut.sourceOffsetMicros(2000001)); // end
    assertEquals(0, testPut.sourceOffsetMicros(2000001)); // after end
  }

  @Test
  public void isAlive() throws Exception {
    testPut.sourceOffsetMicros(500000);
    assertTrue(testPut.isAlive()); // before start
    testPut.sourceOffsetMicros(1000001);
    assertTrue(testPut.isAlive()); // start
    testPut.sourceOffsetMicros(1000500);
    assertTrue(testPut.isAlive()); // after start / before end
    testPut.sourceOffsetMicros(2000001);
    assertFalse(testPut.isAlive()); // end
  }

  @Test
  public void isPlaying() throws Exception {
    testPut.sourceOffsetMicros(500000);
    assertFalse(testPut.isPlaying()); // before start
    testPut.sourceOffsetMicros(1000001);
    assertTrue(testPut.isPlaying()); // start
    testPut.sourceOffsetMicros(1000500);
    assertTrue(testPut.isPlaying()); // after start / before end
    testPut.sourceOffsetMicros(2000001);
    assertFalse(testPut.isPlaying()); // end
  }

  @Test
  public void getSourceId() throws Exception {
    assertEquals("bun1", testPut.getSourceId());
  }

  @Test
  public void getState() throws Exception {
    testPut.sourceOffsetMicros(500000);
    assertEquals(Put.READY, testPut.getState()); // before start
    testPut.sourceOffsetMicros(1000001);
    assertEquals(Put.PLAY, testPut.getState()); // start
    testPut.sourceOffsetMicros(1000500);
    assertEquals(Put.PLAY, testPut.getState()); // after start / before end
    testPut.sourceOffsetMicros(2000001);
    assertEquals(Put.DONE, testPut.getState()); // end
  }

  @Test
  public void getStartAtMicros() throws Exception {
    assertEquals(1000000, testPut.getStartAtMicros());
  }

  @Test
  public void getStopAtMicros() throws Exception {
    assertEquals(2000000, testPut.getStopAtMicros());
  }

  @Test
  public void getVelocity() throws Exception {
    assertEquals(1.0, testPut.getVelocity(), 0);
  }

  @Test
  public void getPitchRatio() throws Exception {
    assertEquals(1.0, testPut.getPitchRatio(), 0);
  }

  @Test
  public void getPan() throws Exception {
    assertEquals(0.0, testPut.getPan(), 0);
  }

}
