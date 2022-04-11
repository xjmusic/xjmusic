// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.fabricator;

import com.google.common.collect.ImmutableList;
import io.xj.hub.tables.pojos.ProgramMeme;
import io.xj.lib.meme.MemeTaxonomy;
import org.junit.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class MemeIsometryTest {
  private MemeIsometry subject;

  @Test
  public void of_List() {
    subject = MemeIsometry.ofMemes(MemeTaxonomy.empty(), ImmutableList.of(
      "Smooth",
      "Catlike"
    ));

    assertArrayEquals(new String[]{"CATLIKE", "SMOOTH"}, subject.getSources().stream().sorted().toArray());
  }

  @Test
  public void add() {
    subject = MemeIsometry.ofMemes(MemeTaxonomy.empty(), ImmutableList.of(
      "Smooth"
    ));
    var meme = new ProgramMeme();
    meme.setProgramId(UUID.randomUUID());
    meme.setName("Catlike");
    subject.add(meme);

    assertArrayEquals(new String[]{"CATLIKE", "SMOOTH"}, subject.getSources().stream().sorted().toArray());
  }

  @Test
  public void doNotMutate() {
    Set<String> subject = MemeIsometry.ofMemes(MemeTaxonomy.empty(), ImmutableList.of(
      "Intensity",
      "Cool",
      "Dark"
    )).getSources();

    assertArrayEquals(new String[]{"COOL", "DARK", "INTENSITY"}, subject.stream().sorted().toArray());
  }

  @Test
  public void score() {
    subject = MemeIsometry.ofMemes(MemeTaxonomy.empty(), ImmutableList.of(
      "Smooth",
      "Catlike"
    ));

    assertEquals(1.0, subject.score(ImmutableList.of("Smooth")), 0.1);
    assertEquals(1.0, subject.score(ImmutableList.of("Catlike")), 0.1);
    assertEquals(2.0, subject.score(ImmutableList.of("Smooth", "Catlike")), 0.1);
  }

  @Test
  public void score_eliminatesDuplicates() {
    subject = MemeIsometry.ofMemes(MemeTaxonomy.empty(), ImmutableList.of(
      "Smooth",
      "Smooth",
      "Catlike"
    ));

    assertEquals(1.0, subject.score(ImmutableList.of("Smooth")), 0.1);
    assertEquals(1.0, subject.score(ImmutableList.of("Catlike")), 0.1);
    assertEquals(2.0, subject.score(ImmutableList.of("Smooth", "Catlike")), 0.1);
  }


}
