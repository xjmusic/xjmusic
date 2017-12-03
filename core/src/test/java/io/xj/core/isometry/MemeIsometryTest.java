// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.isometry;

import io.xj.core.integration.IntegrationTestService;
import io.xj.core.model.MemeEntity;
import io.xj.core.model.link_meme.LinkMeme;
import io.xj.core.tables.records.PatternMemeRecord;

import org.jooq.Result;
import org.jooq.types.ULong;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import java.math.BigInteger;
import java.util.List;

import static io.xj.core.tables.PatternMeme.PATTERN_MEME;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class MemeIsometryTest {
  private final List<MemeEntity> testMemesA = ImmutableList.of(
    new LinkMeme().setLinkId(BigInteger.valueOf(12)).setName("Smooth"),
    new LinkMeme().setLinkId(BigInteger.valueOf(14)).setName("Catlike")
  );

  private final List<MemeEntity> testMemesB = ImmutableList.of(
    new LinkMeme().setLinkId(BigInteger.valueOf(8)).setName("Intensity"),
    new LinkMeme().setLinkId(BigInteger.valueOf(21)).setName("Cool"),
    new LinkMeme().setLinkId(BigInteger.valueOf(45)).setName("Dark")
  );

  private final List<MemeEntity> testMemesC = ImmutableList.of(
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
    Result<PatternMemeRecord> input = IntegrationTestService.getDb().newResult(PATTERN_MEME);
    // meme one
    PatternMemeRecord one = new PatternMemeRecord();
    one.setId(ULong.valueOf(12));
    one.setName("Smooth");
    input.add(one);
    // meme two
    PatternMemeRecord two = new PatternMemeRecord();
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

    assertEquals(0.0, test.scoreCSV("jam,bun"), .001);
    assertEquals(0.333, test.scoreCSV("jam,bun,intense"), .001);
    assertEquals(0.333, test.scoreCSV("jam,intense,bun"), .001);
    assertEquals(1.0, test.scoreCSV("coolness,intense,darkness"), .001);
    assertEquals(0.666, test.scoreCSV("warmth,intense,darkness"), .001);
  }


}
