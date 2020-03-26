// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.isometry;

import com.google.common.collect.ImmutableList;
import io.xj.service.hub.model.SegmentMeme;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertArrayEquals;

public class MemeIsometryTest {

  @Test
  public void of_List() {
    MemeIsometry result = MemeIsometry.ofMemes(ImmutableList.of(
      new SegmentMeme().setSegmentId(UUID.randomUUID()).setName("Smooth"),
      new SegmentMeme().setSegmentId(UUID.randomUUID()).setName("Catlike")
    ));

    assertArrayEquals(new String[]{"smooth", "catlik"}, result.getSources().toArray());
  }

  @Test
  public void add() {
    MemeIsometry result = MemeIsometry.ofMemes(ImmutableList.of(
      new SegmentMeme().setSegmentId(UUID.randomUUID()).setName("Smooth")
    ));
    result.add(new SegmentMeme().setSegmentId(UUID.randomUUID()).setName("Catlike"));

    assertArrayEquals(new String[]{"smooth", "catlik"}, result.getSources().toArray());
  }

  @Test
  public void getSourceStems() {
    List<String> result = MemeIsometry.ofMemes(ImmutableList.of(
      new SegmentMeme().setSegmentId(UUID.randomUUID()).setName("Intensity"),
      new SegmentMeme().setSegmentId(UUID.randomUUID()).setName("Cool"),
      new SegmentMeme().setSegmentId(UUID.randomUUID()).setName("Dark")
    )).getSources();

    assertArrayEquals(new String[]{"intens", "cool", "dark"}, result.toArray());
  }

}
