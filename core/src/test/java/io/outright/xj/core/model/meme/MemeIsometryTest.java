// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.model.meme;

import io.outright.xj.core.integration.IntegrationTestService;
import io.outright.xj.core.model.link_meme.LinkMeme;
import io.outright.xj.core.tables.records.IdeaMemeRecord;

import org.jooq.Result;
import org.jooq.types.ULong;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import java.math.BigInteger;
import java.util.List;

import static io.outright.xj.core.tables.IdeaMeme.IDEA_MEME;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class MemeIsometryTest {
  private final List<Meme> testMemesA = ImmutableList.of(
    new LinkMeme().setLinkId(BigInteger.valueOf(12)).setName("Smooth"),
    new LinkMeme().setLinkId(BigInteger.valueOf(14)).setName("Catlike")
  );

  private final List<Meme> testMemesB = ImmutableList.of(
    new LinkMeme().setLinkId(BigInteger.valueOf(8)).setName("Intensity"),
    new LinkMeme().setLinkId(BigInteger.valueOf(21)).setName("Cool"),
    new LinkMeme().setLinkId(BigInteger.valueOf(45)).setName("Dark")
  );

  private final List<Meme> testMemesC = ImmutableList.of(
    new LinkMeme().setLinkId(BigInteger.valueOf(57)).setName("Funk"),
    new LinkMeme().setLinkId(BigInteger.valueOf(80)).setName("Hard")
  );

  @Test
  public void of_List() throws Exception {
    MemeIsometry result = MemeIsometry.of(testMemesA);

    assertArrayEquals(new String[]{"smooth", "catlik"}, result.getSourceStems().toArray());
  }

  @Test
  public void of_Result() throws Exception {
    Result<IdeaMemeRecord> input = IntegrationTestService.getDb().newResult(IDEA_MEME);
    // meme one
    IdeaMemeRecord one = new IdeaMemeRecord();
    one.setId(ULong.valueOf(12));
    one.setName("Smooth");
    input.add(one);
    // meme two
    IdeaMemeRecord two = new IdeaMemeRecord();
    two.setId(ULong.valueOf(14));
    two.setName("Fast");
    input.add(two);

    MemeIsometry result = MemeIsometry.of(input);

    assertArrayEquals(new String[]{"smooth", "fast"}, result.getSourceStems().toArray());
  }

  @Test
  public void getSourceStems() throws Exception {
    MemeIsometry test = MemeIsometry.of(testMemesB);

    List<String> result = test.getSourceStems();

    assertArrayEquals(new String[]{"intens", "cool", "dark"}, result.toArray());
  }

  @Test
  public void scoreCSV() throws Exception {
    MemeIsometry test = MemeIsometry.of(testMemesB);

    assertEquals(0.0, test.scoreCSV("jam,bun"), 0);
    assertEquals(0.25, test.scoreCSV("jam,bun,intense"), 0);
    assertEquals(0.25, test.scoreCSV("jam,intense,bun"), 0);
    assertEquals(0.75, test.scoreCSV("coolness,intense,darkness"), 0);
    assertEquals(0.5, test.scoreCSV("warmth,intense,darkness"), 0);
  }


}
