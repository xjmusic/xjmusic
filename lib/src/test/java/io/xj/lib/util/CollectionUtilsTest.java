package io.xj.lib.util;

import org.junit.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CollectionUtilsTest {

  @Test
  public void reverse() {
    assertArrayEquals(new Integer[]{3, 2, 1}, CollectionUtils.reverse(List.of(1, 2, 3)).toArray());
    assertArrayEquals(new Integer[]{1}, CollectionUtils.reverse(List.of(1)).toArray());
    assertArrayEquals(new Integer[]{}, CollectionUtils.reverse(List.of()).toArray());
  }
}
