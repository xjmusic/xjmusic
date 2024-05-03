// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

#ifndef XJNEXUS_MUSIC_ROOT_H
#define XJNEXUS_MUSIC_ROOT_H

#include <regex>

#include "PitchClass.h"

namespace Music {

/**
 * Root can be the root of a Chord, Key or Scale.
 */
  class Root {
  private:
    static std::regex rgxNote;
    static std::regex rgxNoteModified;

  public:
    PitchClass pitchClass;
    std::string remainingText;

    /**
     * Parse root and remaining string, using regular expressions
     *
     * @param name to parse root and remaining of
     */
    explicit Root(const std::string &name);

    /**
     * Instantiate a Root by name
     * <p>
     * XJ understands the root of a slash chord https://www.pivotaltracker.com/story/show/176728338
     *
     * @param name of root
     * @return root
     */
    static Root of(const std::string &name);

    /**
     * First group matching pattern in text, else null
     * @param pattern to in
     * @param text              to search
     */
    void evaluate(const std::regex& pattern, const std::string& text);

  };

}// namespace Music

#endif// XJNEXUS_MUSIC_ROOT_H