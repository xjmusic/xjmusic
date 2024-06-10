// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_MEME_ISOMETRY_H
#define XJMUSIC_MEME_ISOMETRY_H

#include <string>

#include "MemeTaxonomy.h"

namespace XJ {

/**
 Determine the isometry between a source and target group of Memes
 */
  class MemeIsometry {
    static const std::string KEY_NAME;
    MemeStack stack;

    /**
     Construct a meme isometry from source memes

     @param sourceMemes from which to construct isometry
     */
    MemeIsometry(MemeTaxonomy taxonomy, std::vector <std::string> sourceMemes) {
      for (std::string meme: sourceMemes) add(StringUtils.toMeme(meme));
      stack = MemeStack.from(taxonomy, getSources());
    }

    /**
     Instantiate a new MemeIsometry of a group of source Memes

     @param taxonomy    context within which to measure isometry
     @param sourceMemes to compare of
     @return MemeIsometry ready for comparison to target Memes
     */
    static MemeIsometry of(MemeTaxonomy taxonomy, std::vector <std::string> sourceMemes) {
      return new MemeIsometry(taxonomy, sourceMemes);
    }

    /**
     Instantiate a new MemeIsometry representing having no memes

     @return an empty MemeIsometry
     */
    static MemeIsometry none() {
      return new MemeIsometry(MemeTaxonomy.empty(), std::vector.of());
    }

    /**
     Score a CSV list of memes based on isometry to source memes

     @param targets comma-separated values to score against source meme names
     @return score is between 0 (no matches) and the number of matching memes
     */
    int score(std::vector <std::string> targets) {
      if (!isAllowed(targets)) return 0;
      return targets.stream()
          .map(StringUtils::toMeme)
          .flatMap(target->sources.stream().map(source->
              Objects.equals(source, target) ? 1 : 0))
          .reduce(0, Integer::sum);
    }

    /**
     Add a meme for isometry comparison
     */
    <R> void add(R meme) {
      try {
        EntityUtils.get(meme, KEY_NAME)
            .ifPresent(name->add(StringUtils.toMeme(std::string.valueOf(name))));
      } catch (EntityException ignored) {
      }
    }

    /**
     * Whether a list of memes is allowed because no more than one matches the category's memes
     * @param memes  The list of memes to check
     * @return       True if the list is allowed
     */
    bool isAllowed(std::vector <std::string> memes) {
      return stack.isAllowed(memes);
    }

    /**
     Get the source Memes

     @return source memes
     */
    std::set <std::string> getSources() {
      return Collections.unmodifiableSet(sources);
    }

    /**
     Compute normalized string representation of an unordered set of memes
     for the purpose of identifying unique constellations.
     <p>
     for each unique sequence-pattern-meme constellation within the main sequence https://github.com/xjmusic/workstation/issues/208

     @return unique constellation for this set of strings.
     */
    std::string getConstellation() {
      return MemeConstellation.fromNames(sources);
    }

  };

} // namespace XJ

#endif//XJMUSIC_MEME_ISOMETRY_H