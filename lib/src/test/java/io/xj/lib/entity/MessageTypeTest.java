// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.lib.entity;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MessageTypeTest {

  @Test
  public void mostSevere() {
    assertEquals(MessageType.Debug, MessageType.mostSevere(List.of(
      MessageType.Debug,
      MessageType.Debug
    )));
    assertEquals(MessageType.Info, MessageType.mostSevere(List.of(
      MessageType.Debug,
      MessageType.Info,
      MessageType.Debug
    )));
    assertEquals(MessageType.Warning, MessageType.mostSevere(List.of(
      MessageType.Debug,
      MessageType.Warning,
      MessageType.Info,
      MessageType.Debug
    )));
    assertEquals(MessageType.Error, MessageType.mostSevere(List.of(
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
