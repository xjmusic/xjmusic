// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.nexus.cache;

import io.xj.service.nexus.dub.AudioCacheItemNumber;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AudioCacheDubDubAudioCacheItemNumberTest {
  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void next() throws Exception {
    assertThat(AudioCacheItemNumber.next(), is(1));
    assertThat(AudioCacheItemNumber.next(), is(2));
    assertThat(AudioCacheItemNumber.next(), is(3));
  }

}
