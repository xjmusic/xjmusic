// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.work;

import io.xj.nexus.OutputMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OutputModeTest {

  @Test
  public void isLocal() {
    assertTrue(OutputMode.PLAYBACK.isLocal());
    assertTrue(OutputMode.FILE.isLocal());
    assertFalse(OutputMode.HLS.isLocal());
  }

  @Test
  public void isSync() {
    assertTrue(OutputMode.HLS.isSync());
    assertTrue(OutputMode.PLAYBACK.isSync());
    assertFalse(OutputMode.FILE.isSync());
  }

  @Test
  public void isAsync() {
    assertFalse(OutputMode.HLS.isAsync());
    assertFalse(OutputMode.PLAYBACK.isAsync());
    assertTrue(OutputMode.FILE.isAsync());
  }
}
