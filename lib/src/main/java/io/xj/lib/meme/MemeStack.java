// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.meme;

import com.google.api.client.util.Lists;
import io.xj.lib.util.Text;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 Meme Stack is a theorem which tests various axioms for validity when a new member is pending introduction.

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
    return isAllowed(memes, targets);
  }

  /**
   Test whether an incoming set of memes is allowed by this meme

   @param targets memes to test
   @return true if the specified set of memes is allowed into this meme stack
   */
  public Boolean isAllowed(Collection<String> sources, Collection<String> targets) {
    // this axiom is applied from source to target
    for (var source : sources)
      if (
        !MmAnti.fromString(source).isAllowed(targets.stream().map(MmAnti::fromString).toList())
          ||
          !MmNumeric.fromString(source).isAllowed(targets.stream().map(MmNumeric::fromString).toList())
          ||
          !MmUnique.fromString(source).isAllowed(targets.stream().map(MmUnique::fromString).toList())
      ) return false;

    // this axiom is applied from target to source
    for (var target : targets)
      if (
          !MmStrong.fromString(target).isAllowed(sources.stream().map(MmStrong::fromString).toList())
      ) return false;

    return true;
  }

  /**
   Test whether all of our own memes are allowed, while avoiding testing any meme against itself
   <p>
   Refuse to make a choice that violates the meme stack #181466514

   @return true if the theorem is valid
   */
  public boolean isValid() {
    List<String> targets = memes.stream().toList();
    List<String> subTargets;

    for (var a = 0; a < targets.size(); a++) {
      subTargets = Lists.newArrayList(targets);
      //noinspection SuspiciousListRemoveInLoop
      subTargets.remove(a);
      for (var b = 0; b < memes.size(); b++)
        if (!isAllowed(subTargets, List.of(targets.get(a))))
          return false;
    }

    return true;
  }
}
