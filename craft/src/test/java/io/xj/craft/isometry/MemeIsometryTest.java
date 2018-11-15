// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.isometry;

import com.google.common.collect.ImmutableList;
import io.xj.core.model.segment_meme.SegmentMeme;
import org.junit.Test;

import java.math.BigInteger;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class MemeIsometryTest {

  @Test
  public void of_List() {
    MemeIsometry result = MemeIsometry.ofMemes(ImmutableList.of(
      new SegmentMeme().setSegmentId(BigInteger.valueOf(12)).setName("Smooth"),
      new SegmentMeme().setSegmentId(BigInteger.valueOf(14)).setName("Catlike")
    ));

    assertArrayEquals(new String[]{"smooth", "catlik"}, result.getSources().toArray());
  }

  @Test
  public void add() {
    MemeIsometry result = MemeIsometry.ofMemes(ImmutableList.of(
      new SegmentMeme().setSegmentId(BigInteger.valueOf(12)).setName("Smooth")
    ));
    result.add(new SegmentMeme().setSegmentId(BigInteger.valueOf(14)).setName("Catlike"));

    assertArrayEquals(new String[]{"smooth", "catlik"}, result.getSources().toArray());
  }

  @Test
  public void getSourceStems() {
    List<String> result = MemeIsometry.ofMemes(ImmutableList.of(
      new SegmentMeme().setSegmentId(BigInteger.valueOf(6)).setName("Intensity"),
      new SegmentMeme().setSegmentId(BigInteger.valueOf(7)).setName("Cool"),
      new SegmentMeme().setSegmentId(BigInteger.valueOf(8)).setName("Dark")
    )).getSources();

    assertArrayEquals(new String[]{"intens", "cool", "dark"}, result.toArray());
  }

}
