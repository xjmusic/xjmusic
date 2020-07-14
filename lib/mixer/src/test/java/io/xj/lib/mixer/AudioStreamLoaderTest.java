// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.mixer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import javax.sound.sampled.AudioFormat;
import java.io.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 test the stream loader
 */
@RunWith(MockitoJUnitRunner.class)
public class AudioStreamLoaderTest {
  private InternalResource testResource;
  private InputStream testInputStream;
  private InputStream spyInputStream;
  private static final String testResourceFilePath = "test_audio" + File.separator + "F32LSB_48kHz_Stereo.wav";

  @Before
  public void setUp() throws Exception {
    testResource = new InternalResource(testResourceFilePath);
    testInputStream = new FileInputStream(testResource.getFile());
    spyInputStream = spy(testInputStream);
  }

  @Test
  public void loadFrames() throws Exception {
    AudioStreamLoader testAudioStreamLoader = new AudioStreamLoader(new BufferedInputStream(testInputStream));

    double[][] actualFrames = testAudioStreamLoader.loadFrames();

    double[][] actualFrameSubset = new double[12][2];
    System.arraycopy(actualFrames, 0, actualFrameSubset, 0, 12);
    assertArrayEquals(
      actualFrameSubset, new double[][]{
        new double[]{-9.720623347675428E-5, -7.202712731668726E-5},
        new double[]{-2.7291171136312187E-4, -1.992213656194508E-4},
        new double[]{-4.42245916929096E-4, -3.1373545061796904E-4},
        new double[]{-6.292833713814616E-4, -4.264774324838072E-4},
        new double[]{-8.280076435767114E-4, -5.740883061662316E-4},
        new double[]{-0.001071288250386715, -6.913816905580461E-4},
        new double[]{-0.0012547593796625733, -8.634181576780975E-4},
        new double[]{-0.0014202219899743795, -0.001024863333441317},
        new double[]{-0.0016344233881682158, -0.0011186040937900543},
        new double[]{-0.001824395963922143, -0.0012717066565528512},
        new double[]{-0.0020332459826022387, -0.0014062805566936731},
        new double[]{-0.0022547978442162275, -0.0014804069651290774}
      });
  }

  @Test(expected = FormatException.class)
  public void loadSource_FailsIfUnableToReadInputStream() throws Exception {
    when(spyInputStream.read()).thenThrow(new IOException("Fail! Fail! Fail!"));

    AudioStreamLoader testAudioStreamLoader = new AudioStreamLoader(new BufferedInputStream(testInputStream));

    assertNull(testAudioStreamLoader);
  }

  @Test
  public void getActualFrames() throws Exception {
    AudioStreamLoader testAudioStreamLoader = new AudioStreamLoader(new BufferedInputStream(testInputStream));
    assertEquals(0, testAudioStreamLoader.getActualFrames());
    testAudioStreamLoader.loadFrames();

    assertEquals(17364, testAudioStreamLoader.getActualFrames());
  }

  @Test
  public void getExpectFrames() throws Exception {
    AudioStreamLoader testAudioStreamLoader = new AudioStreamLoader(new BufferedInputStream(testInputStream));

    assertEquals(17364, testAudioStreamLoader.getExpectFrames());
  }

  @Test
  public void getExpectBytes() throws Exception {
    AudioStreamLoader testAudioStreamLoader = new AudioStreamLoader(new BufferedInputStream(testInputStream));

    assertEquals(138912, testAudioStreamLoader.getExpectBytes());
  }

  @Test
  public void getFrameSize() throws Exception {
    AudioStreamLoader testAudioStreamLoader = new AudioStreamLoader(new BufferedInputStream(testInputStream));

    assertEquals(8, testAudioStreamLoader.getFrameSize());
  }

  @Test
  public void getAudioFormat() throws Exception {
    AudioStreamLoader testAudioStreamLoader = new AudioStreamLoader(new BufferedInputStream(testInputStream));

    AudioFormat audioFormat = testAudioStreamLoader.getAudioFormat();
    assertFalse(audioFormat.isBigEndian());
    assertEquals(AudioFormat.Encoding.PCM_FLOAT, audioFormat.getEncoding());
    assertEquals(32, audioFormat.getSampleSizeInBits());
    assertEquals(48000, audioFormat.getFrameRate(), 0);
  }

}
