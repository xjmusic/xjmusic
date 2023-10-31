// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.lib.mixer;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class PutImplTest {
  Put testPut;
  UUID audioId;

  @BeforeEach
  public void setUp() throws Exception {
    EnvelopeProvider envelopeProvider = new EnvelopeProviderImpl();
    MixerFactory mixerFactory = new MixerFactoryImpl(envelopeProvider, 1000000);
    audioId = UUID.randomUUID();
    testPut = mixerFactory.createPut(UUID.randomUUID(), audioId, 0, 1000000, 2000000, 1.0, 0, 5);
  }

  @Test
  public void lifecycle() {
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
         1000000 > expectSourceOffsetMicros; // never arrives at the DONE threshold
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
  public void sourceOffsetMicros() {
    assertEquals(0, testPut.sourceOffsetMicros(500000)); // before start
    assertEquals(0, testPut.sourceOffsetMicros(1000001)); // start
    assertEquals(500, testPut.sourceOffsetMicros(1000500)); // after start / before end
    assertEquals(1000001, testPut.sourceOffsetMicros(2000001)); // end
    assertEquals(0, testPut.sourceOffsetMicros(2000001)); // after end
  }

  @Test
  public void isAlive() {
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
  public void isPlaying() {
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
  public void getSourceId() {
    assertEquals(audioId, testPut.getAudioId());
  }

  @Test
  public void getState() {
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
  public void getStartAtMicros() {
    assertEquals(1000000, testPut.getStartAtMicros());
  }

  @Test
  public void getStopAtMicros() {
    assertEquals(2000000, testPut.getStopAtMicros());
  }

  @Test
  public void getVelocity() {
    assertEquals(1.0, testPut.getVelocity(), 0);
  }

  /**
   One-shot fadeout mode https://www.pivotaltracker.com/story/show/183385397
   */
  @Test
  public void getReleaseMillis() {
    assertEquals(5, testPut.getReleaseMillis());
  }

}
