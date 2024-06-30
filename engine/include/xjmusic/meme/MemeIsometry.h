// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_ENTITIES_MEME_ISOMETRY_H
#define XJMUSIC_ENTITIES_MEME_ISOMETRY_H

#include <string>

#include "xjmusic/content/InstrumentMeme.h"
#include "xjmusic/content/ProgramMeme.h"
#include "xjmusic/content/ProgramSequenceBindingMeme.h"
#include "xjmusic/segment/SegmentMeme.h"

#include "MemeStack.h"
#include "MemeTaxonomy.h"

namespace XJ {

  /**
   Determine the isometry between a source and target group of Memes
   */
  class MemeIsometry {
  public:
    /**
     Construct a meme isometry from source memes

     @param taxonomy    context within which to measure isometry
     @param sourceMemes from which to construct isometry
     */
    explicit MemeIsometry(MemeTaxonomy taxonomy, const std::set<std::string> &sourceMemes);

    /**
     Instantiate a new MemeIsometry of a Taxonomy and group of source Memes

     @param taxonomy    context within which to measure isometry
     @param sourceMemes to compare of
     @return MemeIsometry ready for comparison to target Memes
     */
    static MemeIsometry of(MemeTaxonomy taxonomy, const std::set<std::string> &sourceMemes);

    /**
     Instantiate a new MemeIsometry of a group of source Memes

     @param sourceMemes to compare of
     @return MemeIsometry ready for comparison to target Memes
     */
    static MemeIsometry of(const std::set<std::string> &sourceMemes);

    /**
     Instantiate a new MemeIsometry representing having no memes

     @return an empty MemeIsometry
     */
    static MemeIsometry none();

    /**
     Score a CSV list of memes based on isometry to source memes

     @param targets comma-separated values to score against source meme names
     @return score is between 0 (no matches) and the number of matching memes
     */
    int score(const std::set<std::string> &targets) const;

    /**
     Add a meme for isometry comparison
     */
    void add(const std::string &meme);

    /**
     Add a program meme for isometry comparison
     */
    void add(const ProgramMeme &meme);

    /**
     Add a program sequence binding meme for isometry comparison
     */
    void add(const ProgramSequenceBindingMeme &meme);

    /**
     Add a instrument meme for isometry comparison
     */
    void add(const InstrumentMeme &meme);

    /**
     Add a segment meme for isometry comparison
     */
    void add(const SegmentMeme &meme);

    /**
     * Whether a list of memes is allowed because no more than one matches the category's memes
     * @param memes  The list of memes to check
     * @return       True if the list is allowed
     */
    bool isAllowed(const std::set<std::string> &memes) const;

    /**
     Get the source Memes

     @return source memes
     */
    std::set<std::string> getSources();

    /**
     Compute normalized string representation of an unordered set of memes
     for the purpose of identifying unique constellations.
     <p>
     for each unique sequence-pattern-meme constellation within the main sequence https://github.com/xjmusic/xjmusic/issues/208

     @return unique constellation for this set of strings.
     */
    std::string getConstellation();

  private:
    static const std::string KEY_NAME;
    MemeStack stack;
    std::set<std::string> sources;
  };

}// namespace XJ

#endif//XJMUSIC_ENTITIES_MEME_ISOMETRY_H