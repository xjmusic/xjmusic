// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.engine.fabricator;

import io.xj.hub.entity.EntityException;
import io.xj.hub.entity.EntityUtils;
import io.xj.hub.meme.Isometry;
import io.xj.hub.meme.MemeStack;
import io.xj.hub.meme.MemeTaxonomy;
import io.xj.hub.util.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 Determine the isometry between a source and target group of Memes
 */
public class MemeIsometry extends Isometry {
  static final String KEY_NAME = "name";
  final MemeStack stack;

  /**
   Construct a meme isometry from source memes

   @param sourceMemes from which to construct isometry
   */
  MemeIsometry(MemeTaxonomy taxonomy, Collection<String> sourceMemes) {
    for (String meme : sourceMemes) add(StringUtils.toMeme(meme));
    stack = MemeStack.from(taxonomy, getSources());
  }

  /**
   Instantiate a new MemeIsometry of a group of source Memes

   @param taxonomy    context within which to measure isometry
   @param sourceMemes to compare of
   @return MemeIsometry ready for comparison to target Memes
   */
  public static MemeIsometry of(MemeTaxonomy taxonomy, Collection<String> sourceMemes) {
    return new MemeIsometry(taxonomy, sourceMemes);
  }

  /**
   Instantiate a new MemeIsometry representing having no memes

   @return an empty MemeIsometry
   */
  public static MemeIsometry none() {
    return new MemeIsometry(MemeTaxonomy.empty(), List.of());
  }

  /**
   Score a CSV list of memes based on isometry to source memes

   @param targets comma-separated values to score against source meme names
   @return score is between 0 (no matches) and the number of matching memes
   */
  public int score(Collection<String> targets) {
    if (!isAllowed(targets)) return 0;
    return targets.stream()
      .map(StringUtils::toMeme)
      .flatMap(target -> sources.stream().map(source ->
        Objects.equals(source, target) ? 1 : 0))
      .reduce(0, Integer::sum);
  }

  /**
   Add a meme for isometry comparison
   */
  public <R> void add(R meme) {
    try {
      EntityUtils.get(meme, KEY_NAME)
        .ifPresent(name -> add(StringUtils.toMeme(String.valueOf(name))));
    } catch (EntityException ignored) {
    }
  }

  public boolean isAllowed(Collection<String> memes) {
    return stack.isAllowed(memes);
  }
}
