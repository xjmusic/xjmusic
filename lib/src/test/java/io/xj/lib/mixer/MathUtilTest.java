// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.lib.mixer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MathUtilTest {
  double[][] buf;

  @BeforeEach
  public void setUp() {
    buf = new double[][]{
      {-0.55183d, 0.63987d},
      {0.97953d, 0.2781d},
      {0.45595d, -0.91939d},
      {-0.19926d, -0.91859d},
      {-0.70917d, 0.51154d},
      {-0.65961d, 0.8641d},
      {0.92381d, -0.26343d},
      {0.17555d, -0.52434d},
      {-0.28254d, 0.44111d},
      {-0.19983d, 0.73959d},
      {0.14844d, -0.68412d},
      {0.83461d, -0.82419d},
      {-0.39756d, 0.27159d},
      {-0.7332d, 0.81737d},
      {0.2740d, -0.76344d},
      {-0.89294d, 0.61383d},
      {0.43288d, -0.42025d},
      {0.31937d, -0.81640d},
      {-0.30681d, -0.56914d},
      {-0.90096d, 0.34464d}
    };
  }

  @Test
  public void logarithmicCompression() {
    assertArrayEquals(new double[]{0.1973815149870411, 0.5045629484153814}, MathUtil.logarithmicCompression(new double[]{0.31937d, 0.81640d}), 0.001);
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
    assertEquals(0, MathUtil.delta(0.877, Double.POSITIVE_INFINITY), 0.001);
    assertEquals(0.104999, MathUtil.delta(0.877, 0.982), 0.001);
  }

  @Test
  public void limit() {
    assertEquals(-0.12, MathUtil.limit(-0.12, 0.54, -0.8874), 0.001);
    assertEquals(0.54, MathUtil.limit(-0.12, 0.54, 0.997), 0.001);
    assertEquals(0.23, MathUtil.limit(-0.12, 0.54, 0.23), 0.001);
    assertEquals(-0.112, MathUtil.limit(-0.12, 0.54, -0.112), 0.001);
  }

  @Test
  public void right() {
    assertEquals(0.75, MathUtil.right(-0.25), 0.001);
    assertEquals(1.0, MathUtil.right(0.345), 0.001);
    assertEquals(1.0, MathUtil.right(0.9), 0.001);
    assertEquals(1.0, MathUtil.right(0), 0.001);
    assertEquals(1.0, MathUtil.right(0.25), 0.001);
    assertEquals(0.655, MathUtil.right(-0.345), 0.001);
    assertEquals(0.0999, MathUtil.right(-0.9), 0.001);
    assertEquals(1.0, MathUtil.right(0), 0.001);
  }

  @Test
  public void left() {
    assertEquals(1.0, MathUtil.left(-0.25), 0.001);
    assertEquals(0.655, MathUtil.left(0.345), 0.001);
    assertEquals(0.0999, MathUtil.left(0.9), 0.001);
    assertEquals(1.0, MathUtil.left(0), 0.001);
    assertEquals(0.75, MathUtil.left(0.25), 0.001);
    assertEquals(1.0, MathUtil.left(-0.345), 0.001);
    assertEquals(1.0, MathUtil.left(-0.9), 0.001);
    assertEquals(1.0, MathUtil.left(0), 0.001);
  }

  @Test
  public void avg() {
    assertEquals(175.6, MathUtil.avg(new double[]{0.5, 92.4, 4.2, 779.4, 1.5}), 0.0001);
  }

  @Test
  public void avg_ListOfFloat() {
    assertEquals(175.6, MathUtil.avg(List.of(0.5F, 92.4F, 4.2F, 779.4F, 1.5F)), 0.0001);
  }

}
