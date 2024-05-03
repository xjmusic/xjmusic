package io.xj.hub.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MultisetTest {
  private Multiset<String> subject;

  @BeforeEach
  public void setUp() {
    subject = new Multiset<>();
  }

  @Test
  public void testAdd_contains() {
    subject.add("jello");

    assertTrue(subject.contains("jello"));
    assertFalse(subject.contains("hello"));
  }

  @Test
  public void testAddAll_containsAll() {
    subject.addAll(List.of("jello", "pudding"));

    assertTrue(subject.containsAll(List.of("jello", "pudding")));
    assertFalse(subject.containsAll(List.of("hello", "pudding")));
  }

  @Test
  public void testRemove() {
    subject.addAll(List.of("jello", "pudding"));

    subject.remove("pudding");

    assertTrue(subject.contains("jello"));
    assertFalse(subject.contains("pudding"));
  }

  @Test
  public void testElementSet() {
    subject.addAll(List.of("jello", "pudding"));

    var result = subject.elementSet();

    assertArrayEquals(new String[]{"jello", "pudding"}, result.toArray());
  }

  @Test
  public void testIsEmpty() {
    assertTrue(subject.isEmpty());

    subject.add("jello");

    assertFalse(subject.isEmpty());
  }

  @Test
  public void testSize() {
    subject.addAll(List.of("jello", "pudding"));

    assertEquals(2, subject.size());
  }

  @Test
  public void testCount() {
    subject.addAll(List.of("jello", "jello", "pudding"));

    assertEquals(2, subject.count("jello"));
    assertEquals(1, subject.count("pudding"));
  }

  @Test
  public void testToString() {
    subject.addAll(List.of("jello", "pudding"));

    assertEquals("[jello, pudding]", subject.toString());
  }
}
