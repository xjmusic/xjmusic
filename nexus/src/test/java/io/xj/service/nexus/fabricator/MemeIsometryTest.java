// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.fabricator;

import com.google.common.collect.ImmutableList;
import io.xj.ProgramMeme;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class MemeIsometryTest {

  @Test
  public void of_List() {
    MemeIsometry result = MemeIsometry.ofMemes(ImmutableList.of(
      "Smooth",
      "Catlike"
    ));

    assertArrayEquals(new String[]{"SMOOTH", "CATLIKE"}, result.getSources().toArray());
  }

  @Test
  public void add() {
    MemeIsometry result = MemeIsometry.ofMemes(ImmutableList.of(
      "Smooth"
    ));
    result.add(ProgramMeme.newBuilder().setProgramId(UUID.randomUUID().toString()).setName("Catlike").build());

    assertArrayEquals(new String[]{"SMOOTH", "CATLIKE"}, result.getSources().toArray());
  }

  @Test
  public void doNotMutate() {
    List<String> result = MemeIsometry.ofMemes(ImmutableList.of(
      "Intensity",
      "Cool",
      "Dark"
    )).getSources();

    assertArrayEquals(new String[]{"INTENSITY", "COOL", "DARK"}, result.toArray());
  }

  @Test
  public void notMemes() {
    MemeIsometry result = MemeIsometry.ofMemes(ImmutableList.of(
      "Smooth",
      "!Busy"
    ));

    assertArrayEquals(new String[]{"SMOOTH", "!BUSY"}, result.getSources().toArray());

    assertEquals(0.5, result.score(ImmutableList.of("Smooth")), 0.1);
    assertEquals(0.0, result.score(ImmutableList.of("Busy")), 0.1);
  }

}
