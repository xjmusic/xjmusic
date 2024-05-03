// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

#ifndef XJNEXUS_MUSIC_SLASH_ROOT_H
#define XJNEXUS_MUSIC_SLASH_ROOT_H

#include <optional>
#include <regex>
#include <string>

#include "PitchClass.h"

namespace Music {

/**
 * Root can be the root of a Chord, Key or Scale.
 */
  class SlashRoot {
  private:
    static std::regex rgxSlashPost;
    static std::regex rgxSlashNote;
    static std::regex rgxSlashNoteModified;
    static std::regex rgxSlashPre;

  public:
    std::optional<PitchClass> pitchClass;
    std::string pre;
    std::string post;

    /**
     * Parse slash root string, using regular expressions
     * @param name to parse slash root
     */
    explicit SlashRoot(const std::string& name);

    /**
     * Instantiate a Root by name
     * <p>
     * XJ understands the root of a slash chord https://www.pivotaltracker.com/story/show/176728338
     *
     * @param name of root
     * @return root
     */
    static SlashRoot of(const std::string &name);

    /**
     * Compare two slash roots for equality
     * @param other  to compare
     * @return       true if equal
     */
    bool operator==(const SlashRoot &other) const;

    /**
     * No slash root
     * @return  no slash root
     */
    static SlashRoot none();

    /**
     * Get the pitch class of a slash root, or a default value
     * @param dpc  default pitch class
     * @return     pitch class or default
     */
    [[nodiscard]] PitchClass orDefault(PitchClass dpc) const;

    /**
     * Returns the pre-slash content, or whole string if no slash is present
     * @param description to search for pre-slash content
     */
    static std::string computePre(const std::string &description);

    /**
     * Return true if a slash is present in the given chord name
     * @param name to test for slash
     * @return true if slash is found
     */
    static bool has_value(const std::string &name);

    /**
     * @return true if any slash info is present
     */
    [[nodiscard]] bool has_value() const;

    /**
     * Display the slash root, with an adjustment symbol if it's a clean note, otherwise as-is
     * @param withOptional adjustment symbol
     * @return displayed slash root
     */
    [[nodiscard]] std::string display(Accidental withOptional) const;
  };

}// namespace Music

#endif// XJNEXUS_MUSIC_SLASH_ROOT_H