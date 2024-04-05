// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.meme;

import io.xj.hub.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Meme Stack is a theorem which tests various axioms for validity when a new member is pending introduction.
 * <p>
 * Concretely exclude meme combinations in violation of the given axioms:
 * - Anti-Memes
 * - Numeric Memes
 * - Unique Memes
 *
 * <p>
 *
 * @see MemeTaxonomy for how we parse categories of exclusive memes
 */
public class MemeStack {
  final Set<String> memes;
  final MemeTaxonomy taxonomy;

  /**
   * Constructor from taxonomy and memes
   *
   * @param from from which to create stack
   */
  MemeStack(MemeTaxonomy taxonomy, Collection<String> from) {
    this.taxonomy = taxonomy;
    memes = from.stream().map(StringUtils::toMeme).collect(Collectors.toSet());
  }

  public static MemeStack from(MemeTaxonomy taxonomy, Collection<String> memes) {
    return new MemeStack(taxonomy, memes);
  }

  /**
   * Test whether an incoming set of memes is allowed by this meme
   *
   * @param targets memes to test
   * @return true if the specified set of memes is allowed into this meme stack
   */
  public Boolean isAllowed(Collection<String> targets) {
    return isAllowed(memes, targets);
  }

  /**
   * Test whether an incoming set of memes is allowed by this meme
   *
   * @param targets memes to test
   * @return true if the specified set of memes is allowed into this meme stack
   */
  public Boolean isAllowed(Collection<String> sources, Collection<String> targets) {
    // this axiom is applied from source to target
    for (var source : sources)
      if (
        !ParseAnti.fromString(source).isAllowed(targets.stream().map(ParseAnti::fromString).toList())
          ||
          !ParseNumeric.fromString(source).isAllowed(targets.stream().map(ParseNumeric::fromString).toList())
          ||
          !ParseUnique.fromString(source).isAllowed(targets.stream().map(ParseUnique::fromString).toList())
      ) return false;

    // this axiom is applied from target to source
    for (var target : targets)
      if (
        !ParseStrong.fromString(target).isAllowed(sources.stream().map(ParseStrong::fromString).toList())
      ) return false;

    // meme categories https://www.pivotaltracker.com/story/show/181801646
    return taxonomy.isAllowed(Stream.concat(sources.stream(), targets.stream()).toList());
  }

  /**
   * Test whether all of our own memes are allowed, while avoiding testing any meme against itself
   * <p>
   * Refuse to make a choice that violates the meme stack https://www.pivotaltracker.com/story/show/181466514
   *
   * @return true if the theorem is valid
   */
  public boolean isValid() {
    List<String> targets = memes.stream().toList();
    List<String> subTargets;

    for (var a = 0; a < targets.size(); a++) {
      subTargets = new ArrayList<>(targets);
      //noinspection SuspiciousListRemoveInLoop
      subTargets.remove(a);
      for (var b = 0; b < memes.size(); b++)
        if (!isAllowed(subTargets, List.of(targets.get(a))))
          return false;
    }

    // meme categories https://www.pivotaltracker.com/story/show/181801646
    return taxonomy.isAllowed(targets);
  }

  /**
   * Constellations report https://www.pivotaltracker.com/story/show/182861489
   *
   * @return normalized string representation of an unordered set of memes
   */
  public String getConstellation() {
    return Isometry.of(memes).getConstellation();
  }
}
