package io.xj.ship.source;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class OGGVorbisDecoderTest {
  private OGGVorbisDecoder subject;

  @Before
  public void setUp() throws Exception {
    var loader = OGGVorbisDecoderTest.class.getClassLoader();
    var input = loader.getResourceAsStream("ogg_decoding/coolair-1633586832900943.ogg");
    subject = OGGVorbisDecoder.decode(input);
  }

  @Test
  public void getInfoAndPcmData() {
    assertEquals(2, subject.getAudioFormat().getChannels());
    assertEquals(48000, subject.getAudioFormat().getSampleRate(), 0.01);
    assertEquals(767552, subject.getPcmData().size());
  }
}
