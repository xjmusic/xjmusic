// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

#ifndef XJMUSIC_MEME_STACK_H
#define XJMUSIC_MEME_STACK_H

#include <string>
#include <set>

#include "MemeTaxonomy.h"

namespace XJ {

/**
 Meme Stack is a theorem which tests various axioms for validity when a new member is pending introduction.
 <p>
 Concretely exclude meme combinations in violation of the given axioms:
 - Anti-Memes
 - Numeric Memes
 - Unique Memes
 <p>
 @see MemeTaxonomy for how we parse categories of exclusive memes
 */
class MemeStack {
  std::set<std::string> memes;
  MemeTaxonomy taxonomy;

  /**
   Constructor from taxonomy and memes

   @param from from which to create stack
   */
  explicit MemeStack(MemeTaxonomy taxonomy, std::vector<std::string> from);

  /**
   * Construct a meme stack from a set of memes
   */
  static MemeStack from(MemeTaxonomy taxonomy, std::vector<std::string> memes) {
    return MemeStack(taxonomy, memes);
  }

  /**
   Test whether an incoming set of memes is allowed by this meme

   @param targets memes to test
   @return true if the specified set of memes is allowed into this meme stack
   */
  bool isAllowed(std::vector<std::string> targets) {
    return isAllowed(memes, targets);
  }

  /**
   Test whether an incoming set of memes is allowed by this meme

   @param targets memes to test
   @return true if the specified set of memes is allowed into this meme stack
   */
  bool isAllowed(std::vector<std::string> sources, std::vector<std::string> targets) {
    // this axiom is applied from source to target
    for (auto source : sources)
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

    // meme categories https://github.com/xjmusic/workstation/issues/209
    return taxonomy.isAllowed(Stream.concat(sources.stream(), targets.stream()).toList());
  }

  /**
   Test whether all of our own memes are allowed, while avoiding testing any meme against itself
   <p>
   Refuse to make a choice that violates the meme stack https://github.com/xjmusic/workstation/issues/211

   @return true if the theorem is valid
   */
  bool isValid() {
    std::vector<std::string> targets = memes.stream().toList();
    std::vector<std::string> subTargets;

    for (var a = 0; a < targets.size(); a++) {
      subTargets = new ArrayList<>(targets);
      //noinspection SuspiciousListRemoveInLoop
      subTargets.remove(a);
      for (var b = 0; b < memes.size(); b++)
        if (!isAllowed(subTargets, std::vector.of(targets.get(a))))
          return false;
    }

    // meme categories https://github.com/xjmusic/workstation/issues/209
    return taxonomy.isAllowed(targets);
  }

  /**
   Constellations report https://github.com/xjmusic/workstation/issues/212

   @return normalized string representation of an unordered set of memes
   */
  std::string getConstellation() {
    return Isometry.of(memes).getConstellation(); // get around circular dependency that made us originally create a separate subclass Isometry
  }
};

} // namespace XJ

#endif // XJMUSIC_MEME_STACK_H