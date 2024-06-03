// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.engine.fabricator;

import io.xj.model.meme.MemeTaxonomy;
import io.xj.model.pojos.ProgramMeme;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MemeIsometryTest {
  MemeIsometry subject;

  @Test
  public void of_List() {
    subject = MemeIsometry.of(MemeTaxonomy.empty(), List.of(
      "Smooth",
      "Catlike"
    ));

    assertArrayEquals(new String[]{"CATLIKE", "SMOOTH"}, subject.getSources().stream().sorted().toArray());
  }

  @Test
  public void add() {
    subject = MemeIsometry.of(MemeTaxonomy.empty(), List.of(
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
    Set<String> subject = MemeIsometry.of(MemeTaxonomy.empty(), List.of(
      "Intensity",
      "Cool",
      "Dark"
    )).getSources();

    assertArrayEquals(new String[]{"COOL", "DARK", "INTENSITY"}, subject.stream().sorted().toArray());
  }

  @Test
  public void score() {
    subject = MemeIsometry.of(MemeTaxonomy.empty(), List.of(
      "Smooth",
      "Catlike"
    ));

    assertEquals(1.0, subject.score(List.of("Smooth")), 0.1);
    assertEquals(1.0, subject.score(List.of("Catlike")), 0.1);
    assertEquals(2.0, subject.score(List.of("Smooth", "Catlike")), 0.1);
  }

  @Test
  public void score_eliminatesDuplicates() {
    subject = MemeIsometry.of(MemeTaxonomy.empty(), List.of(
      "Smooth",
      "Smooth",
      "Catlike"
    ));

    assertEquals(1.0, subject.score(List.of("Smooth")), 0.1);
    assertEquals(1.0, subject.score(List.of("Catlike")), 0.1);
    assertEquals(2.0, subject.score(List.of("Smooth", "Catlike")), 0.1);
  }


}
