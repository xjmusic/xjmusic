// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.meme;

import io.xj.lib.util.Text;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 Concretely exclude meme combinations in violation of the given axioms:
 - Anti-Memes
 - Numeric Memes
 - Unique Memes
 */
public class MemeStack {
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
   Test whether an incoming set of memes is allowed by this meme

   @param targets memes to test
   @return true if the specified set of memes is allowed into this meme stack
   */
  public Boolean isAllowed(Collection<String> targets) {
    for (var source : memes) {
      if (
        !MmAnti.fromString(source).isAllowed(targets.stream().map(MmAnti::fromString).toList())
          ||
          !MmNumeric.fromString(source).isAllowed(targets.stream().map(MmNumeric::fromString).toList())
          ||
          !MmStrong.fromString(source).isAllowed(targets.stream().map(MmStrong::fromString).toList())
          ||
          !MmUnique.fromString(source).isAllowed(targets.stream().map(MmUnique::fromString).toList())
      ) return false;
    }

    return true;
  }
}
