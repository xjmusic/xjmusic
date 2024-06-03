// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.engine.mixer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MathUtilTest {
  float[][] buf;

  @BeforeEach
  public void setUp() {
    buf = new float[][]{
      {-0.55183f, 0.63987f},
      {0.97953f, 0.2781f},
      {0.45595f, -0.91939f},
      {-0.19926f, -0.91859f},
      {-0.70917f, 0.51154f},
      {-0.65961f, 0.8641f},
      {0.92381f, -0.26343f},
      {0.17555f, -0.52434f},
      {-0.28254f, 0.44111f},
      {-0.19983f, 0.73959f},
      {0.14844f, -0.68412f},
      {0.83461f, -0.82419f},
      {-0.39756f, 0.27159f},
      {-0.7332f, 0.81737f},
      {0.2740f, -0.76344f},
      {-0.89294f, 0.61383f},
      {0.43288f, -0.42025f},
      {0.31937f, -0.81640f},
      {-0.30681f, -0.56914f},
      {-0.90096f, 0.34464f}
    };
  }

  @Test
  public void logarithmicCompression() {
    assertArrayEquals(new float[]{0.1973815149870411f, 0.5045629484153814f}, MathUtil.logarithmicCompression(new float[]{0.31937f, 0.81640f}), 0.001f);
  }

  @Test
  public void enforceMax_overTheLimit() {
    assertThrows(MixerException.class, () -> MathUtil.enforceMax(25, "wedgies before going postal", 26));
  }

  @Test
  public void enforceMax() throws MixerException {
    MathUtil.enforceMax(25, "wedgies before going postal", 25);
  }

  @Test
  public void enforceMin_underTheLine() {
    assertThrows(MixerException.class, () -> MathUtil.enforceMin(25, "tugs before jam", 24));
  }

  @Test
  public void enforceMin() throws MixerException {
    MathUtil.enforceMin(25, "tugs before jam", 25);
  }

  @Test
  public void maxAbs() {
    assertEquals(0.97953, MathUtil.maxAbs(buf, 0, 10, 1), 0.001);
    assertEquals(0.92381, MathUtil.maxAbs(buf, 5, 17, 1), 0.001);
    assertEquals(0.97953, MathUtil.maxAbs(buf, 1), 0.001);

    // ok to specify end past the buffer, or beginning before it
    assertEquals(0.97953, MathUtil.maxAbs(buf, -27, 774, 1), 0.001);
  }


  @Test
  public void delta() {
    assertEquals(0, MathUtil.delta(0.877f, Float.POSITIVE_INFINITY), 0.001);
    assertEquals(0.104999, MathUtil.delta(0.877f, 0.982f), 0.001);
  }

  @Test
  public void limit() {
    assertEquals(-0.12, MathUtil.limit(-0.12f, 0.54f, -0.8874f), 0.001);
    assertEquals(0.54, MathUtil.limit(-0.12f, 0.54f, 0.997f), 0.001);
    assertEquals(0.23, MathUtil.limit(-0.12f, 0.54f, 0.23f), 0.001);
    assertEquals(-0.112, MathUtil.limit(-0.12f, 0.54f, -0.112f), 0.001);
  }

  @Test
  public void right() {
    assertEquals(0.75, MathUtil.right(-0.25f), 0.001);
    assertEquals(1.0, MathUtil.right(0.345f), 0.001);
    assertEquals(1.0, MathUtil.right(0.9f), 0.001);
    assertEquals(1.0, MathUtil.right(0f), 0.001);
    assertEquals(1.0, MathUtil.right(0.25f), 0.001);
    assertEquals(0.655, MathUtil.right(-0.345f), 0.001);
    assertEquals(0.0999, MathUtil.right(-0.9f), 0.001);
    assertEquals(1.0, MathUtil.right(0f), 0.001);
  }

  @Test
  public void left() {
    assertEquals(1.0, MathUtil.left(-0.25f), 0.001);
    assertEquals(0.655, MathUtil.left(0.345f), 0.001);
    assertEquals(0.0999, MathUtil.left(0.9f), 0.001);
    assertEquals(1.0, MathUtil.left(0f), 0.001);
    assertEquals(0.75, MathUtil.left(0.25f), 0.001);
    assertEquals(1.0, MathUtil.left(-0.345f), 0.001);
    assertEquals(1.0, MathUtil.left(-0.9f), 0.001);
    assertEquals(1.0, MathUtil.left(0f), 0.001);
  }

  @Test
  public void avg() {
    assertEquals(175.6, MathUtil.avg(new float[]{0.5f, 92.4f, 4.2f, 779.4f, 1.5f}), 0.0001);
  }

  @Test
  public void avg_ListOfFloat() {
    assertEquals(175.6, MathUtil.avg(List.of(0.5F, 92.4F, 4.2F, 779.4F, 1.5F)), 0.0001);
  }

}
