package io.xj.lib.entity;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
  public void mostSevereType() {
    assertEquals(MessageType.Debug, MessageType.mostSevereType(ImmutableList.of(
      MockMessageEntity.create(MessageType.Debug, "Debug message"),
      MockMessageEntity.create(MessageType.Debug, "Debug message")
    )));
    assertEquals(MessageType.Info, MessageType.mostSevereType(ImmutableList.of(
      MockMessageEntity.create(MessageType.Debug, "Debug message"),
      MockMessageEntity.create(MessageType.Info, "Info message"),
      MockMessageEntity.create(MessageType.Debug, "Debug message")
    )));
    assertEquals(MessageType.Warning, MessageType.mostSevereType(ImmutableList.of(
      MockMessageEntity.create(MessageType.Debug, "Debug message"),
      MockMessageEntity.create(MessageType.Warning, "Warning message"),
      MockMessageEntity.create(MessageType.Info, "Info message"),
      MockMessageEntity.create(MessageType.Debug, "Debug message")
    )));
    assertEquals(MessageType.Error, MessageType.mostSevereType(ImmutableList.of(
      MockMessageEntity.create(MessageType.Error, "Error message"),
      MockMessageEntity.create(MessageType.Debug, "Debug message"),
      MockMessageEntity.create(MessageType.Info, "Info message"),
      MockMessageEntity.create(MessageType.Warning, "Warning message"),
      MockMessageEntity.create(MessageType.Debug, "Debug message")
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
