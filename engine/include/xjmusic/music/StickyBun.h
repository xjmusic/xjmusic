// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

#ifndef XJMUSIC_MUSIC_STICKY_BUN_H
#define XJMUSIC_MUSIC_STICKY_BUN_H

#include <string>
#include <vector>
#include <random>

#include "xjmusic/content/Entity.h"
#include "Note.h"

using namespace Content;

namespace Music {

  /**
   * Segment has metadata for XJ to persist "notes in the margin" of the composition for itself to read https://www.pivotaltracker.com/story/show/183135787
   */
  class StickyBun {
  private:
    static std::random_device rd;
    static std::mt19937 gen;
    static int MAX_VALUE;
    static std::uniform_int_distribution<> distrib;
    static std::string META_KEY_TEMPLATE;

  public:
    std::vector<int> values;
    UUID eventId;

    /**
     * Prepare a sticky bun with event id and values
     *
     * @param eventId to persist
     * @param size    of bun to generate
     */
    StickyBun(UUID eventId, int size);

    /**
     * Prepare a sticky bun with event id and values
     *
     * @param eventId to persist
     * @param values  of bun
     */
    StickyBun(UUID eventId, std::vector<int> values);

    /**
     * Compute a meta key based on the given event id
     * <p>
     * Segment has metadata for XJ to persist "notes in the margin" of the composition for itself to read https://www.pivotaltracker.com/story/show/183135787
     *
     * @return compute meta key
     */
    static std::string computeMetaKey(const UUID& id);

    /**
     * Replace atonal notes in the list with selections based on the sticky bun
     *
     * @param source       notes to replace atonal elements
     * @param voicingNotes from which to select replacements
     * @return notes with atonal elements augmented by sticky bun
     */
    [[nodiscard]] std::vector<Note> replaceAtonal(std::vector<Note> source, const std::vector<Note>& voicingNotes) const;

    /**
     * Replace atonal notes in the list with selections based on the sticky bun
     *
     * @param voicingNotes from which to select replacements
     * @return notes with atonal elements augmented by sticky bun
     */
    [[nodiscard]] Note compute(std::vector<Note> voicingNotes, int index) const;

    /**
     * Compute a meta key based on the event id
     * @return  meta key
     */
    [[nodiscard]] std::string computeMetaKey() const;
  };

}// namespace Music

#endif// XJMUSIC_MUSIC_STICKY_BUN_H