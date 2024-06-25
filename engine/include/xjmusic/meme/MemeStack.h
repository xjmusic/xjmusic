// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

#ifndef XJMUSIC_ENTITIES_MEME_STACK_H
#define XJMUSIC_ENTITIES_MEME_STACK_H

#include <string>
#include <set>

#include "MemeTaxonomy.h"
#include "ParseAnti.h"
#include "ParseNumeric.h"
#include "ParseUnique.h"
#include "ParseStrong.h"

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
  public:

    /**
     Constructor from taxonomy and memes

     @param from from which to create stack
     */
    explicit MemeStack(MemeTaxonomy taxonomy, const std::set<std::string> &from);

    /**
     * Construct a meme stack from a set of memes
     */
    static MemeStack from(const MemeTaxonomy &taxonomy, const std::set<std::string> &memes);

    /**
     Test whether an incoming set of memes is allowed by this meme

     @param targets memes to test
     @return true if the specified set of memes is allowed into this meme stack
     */
    bool isAllowed(const std::set<std::string> &targets) const;

    /**
     Test whether an incoming set of memes is allowed by this meme

     @param targets memes to test
     @return true if the specified set of memes is allowed into this meme stack
     */
    bool isAllowed(const std::set<std::string> &sources, const std::set<std::string> &targets) const;

    /**
     Test whether all of our own memes are allowed, while avoiding testing any meme against itself
     <p>
     Refuse to make a choice that violates the meme stack https://github.com/xjmusic/xjmusic/issues/211

     @return true if the theorem is valid
     */
    bool isValid();

    /**
     Constellations report https://github.com/xjmusic/xjmusic/issues/212

     @return normalized string representation of an unordered set of memes
     */
    std::string getConstellation() const;

  private:
    std::set<std::string> memes;
    MemeTaxonomy taxonomy;
  };

} // namespace XJ

#endif // XJMUSIC_ENTITIES_MEME_STACK_H