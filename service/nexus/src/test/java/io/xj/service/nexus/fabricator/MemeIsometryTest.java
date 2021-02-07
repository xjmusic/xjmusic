// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.fabricator;

import com.google.common.collect.ImmutableList;
import io.xj.ProgramMeme;
import io.xj.lib.entity.EntityException;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertArrayEquals;

public class MemeIsometryTest {

  @Test
  public void of_List() {
    MemeIsometry result = MemeIsometry.ofMemes(ImmutableList.of(
      "Smooth",
      "Catlike"
    ));

    assertArrayEquals(new String[]{"smooth", "catlik"}, result.getSources().toArray());
  }

  @Test
  public void add() {
    MemeIsometry result = MemeIsometry.ofMemes(ImmutableList.of(
      "Smooth"
    ));
    result.add(ProgramMeme.newBuilder().setProgramId(UUID.randomUUID().toString()).setName("Catlike").build());

    assertArrayEquals(new String[]{"smooth", "catlik"}, result.getSources().toArray());
  }

  @Test
  public void getSourceStems() {
    List<String> result = MemeIsometry.ofMemes(ImmutableList.of(
      "Intensity",
      "Cool",
      "Dark"
    )).getSources();

    assertArrayEquals(new String[]{"intens", "cool", "dark"}, result.toArray());
  }

}
