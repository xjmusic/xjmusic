// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.fabricator;

import com.google.common.base.Objects;
import io.xj.lib.util.Text;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 Determine the isometry between a source and target group of Memes
 */
public class MemeStack {
  private static final String NOT_PREFIX = "!";
  private static final String UNIQUE_PREFIX = "$";
  private final Set<String> memes;

  /**
   Constructor from memes

   @param from from which to create stack
   */
  private MemeStack(Collection<String> from) {
    memes = from.stream().map(Text::toMeme).collect(Collectors.toSet());
  }

  /**
   Instantiate a new MemeIsometry of a group of source Memes,
   as expressed in a Result of jOOQ records.

   @param memes to compare of
   @return MemeIsometry ready for comparison to target Memes
   */
  public static MemeStack from(Collection<String> memes) {
    return new MemeStack(memes);
  }

  /**
   @param targets memes to test
   @return true if the specified set of memes is allowed into this meme stack
   */
  public Boolean isAllowed(Collection<String> targets) {
    for (var source : memes)
      for (var target : targets) {

        if (Objects.equal(source, target)
          && UNIQUE_PREFIX.equals(source.substring(0, 1))
          && UNIQUE_PREFIX.equals(target.substring(0, 1)))
          return false;

        if (NOT_PREFIX.equals(source.substring(0, 1))
          && source.substring(1).equals(target))
          return false;

        if (NOT_PREFIX.equals(target.substring(0, 1))
          && target.substring(1).equals(source))
          return false;
      }

    return true;
  }
}
