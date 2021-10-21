// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.entity;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static org.junit.Assert.*;

public class MessageTypeTest {

  @Test
  public void mostSevere() {
    assertEquals(MessageType.Debug, MessageType.mostSevere(ImmutableList.of(
      MessageType.Debug,
      MessageType.Debug
    )));
    assertEquals(MessageType.Info, MessageType.mostSevere(ImmutableList.of(
      MessageType.Debug,
      MessageType.Info,
      MessageType.Debug
    )));
    assertEquals(MessageType.Warning, MessageType.mostSevere(ImmutableList.of(
      MessageType.Debug,
      MessageType.Warning,
      MessageType.Info,
      MessageType.Debug
    )));
    assertEquals(MessageType.Error, MessageType.mostSevere(ImmutableList.of(
      MessageType.Error,
      MessageType.Debug,
      MessageType.Info,
      MessageType.Warning,
      MessageType.Debug
    )));
  }

  @Test
  public void isMoreSevere() {
    assertFalse(MessageType.isMoreSevere(MessageType.Debug, MessageType.Debug));
    assertFalse(MessageType.isMoreSevere(MessageType.Debug, MessageType.Info));
    assertFalse(MessageType.isMoreSevere(MessageType.Debug, MessageType.Warning));
    assertFalse(MessageType.isMoreSevere(MessageType.Debug, MessageType.Error));
    assertTrue(MessageType.isMoreSevere(MessageType.Info, MessageType.Debug));
    assertFalse(MessageType.isMoreSevere(MessageType.Info, MessageType.Info));
    assertFalse(MessageType.isMoreSevere(MessageType.Info, MessageType.Warning));
    assertFalse(MessageType.isMoreSevere(MessageType.Info, MessageType.Error));
    assertTrue(MessageType.isMoreSevere(MessageType.Warning, MessageType.Debug));
    assertTrue(MessageType.isMoreSevere(MessageType.Warning, MessageType.Info));
    assertFalse(MessageType.isMoreSevere(MessageType.Warning, MessageType.Warning));
    assertFalse(MessageType.isMoreSevere(MessageType.Warning, MessageType.Error));
    assertTrue(MessageType.isMoreSevere(MessageType.Error, MessageType.Debug));
    assertTrue(MessageType.isMoreSevere(MessageType.Error, MessageType.Info));
    assertTrue(MessageType.isMoreSevere(MessageType.Error, MessageType.Warning));
    assertFalse(MessageType.isMoreSevere(MessageType.Error, MessageType.Error));
  }
}
