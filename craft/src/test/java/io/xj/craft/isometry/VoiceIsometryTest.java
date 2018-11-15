// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.isometry;

import io.xj.core.model.voice.Voice;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import java.math.BigInteger;
import java.util.Collection;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class VoiceIsometryTest {
  private final Collection<Voice> testVoicesA = ImmutableList.of(
    new Voice(BigInteger.valueOf(12)).setDescription("Super Cool"),
    new Voice(BigInteger.valueOf(14)).setDescription("Very Interesting")
  );

  @Test
  public void of() throws Exception {
    VoiceIsometry result = VoiceIsometry.ofVoices(testVoicesA);

    assertNotNull(result);
  }

  @Test
  public void find() throws Exception {
    VoiceIsometry result = VoiceIsometry.ofVoices(testVoicesA);

    Voice find1 = result.find(new Voice().setDescription("Sooper Kewl"));
    assertNotNull(find1);
    assertEquals(BigInteger.valueOf(12), find1.getId());

    Voice find2 = result.find(new Voice().setDescription("Vury Anterestin"));
    assertNotNull(find2);
    assertEquals(BigInteger.valueOf(14), find2.getId());
  }



}
