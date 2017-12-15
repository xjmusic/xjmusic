// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.isometry;

import io.xj.core.model.link_meme.LinkMeme;
import io.xj.core.model.meme.Meme;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;

public class MemeIsometryTest {
  private final Collection<Meme> testMemesA = ImmutableList.of(
    new LinkMeme().setLinkId(BigInteger.valueOf(12)).setName("Smooth"),
    new LinkMeme().setLinkId(BigInteger.valueOf(14)).setName("Catlike")
  );

  private final Collection<Meme> testMemesB = ImmutableList.of(
    new LinkMeme().setLinkId(BigInteger.valueOf(8)).setName("Intensity"),
    new LinkMeme().setLinkId(BigInteger.valueOf(21)).setName("Cool"),
    new LinkMeme().setLinkId(BigInteger.valueOf(45)).setName("Dark")
  );

  @Test
  public void of_List() throws Exception {
    MemeIsometry result = MemeIsometry.of(testMemesA);

    assertArrayEquals(new String[]{"smooth", "catlik"}, result.getSourceStems().toArray());
  }

  @Test
  public void getSourceStems() throws Exception {
    MemeIsometry test = MemeIsometry.of(testMemesB);

    List<String> result = test.getSourceStems();

    assertArrayEquals(new String[]{"intens", "cool", "dark"}, result.toArray());
  }


}
