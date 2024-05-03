// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <algorithm>
#include <sstream>
#include <utility>

#include "xjnexus/music/StickyBun.h"

namespace Music {

  std::random_device StickyBun::rd;
  std::mt19937 StickyBun::gen(rd());
  int StickyBun::MAX_VALUE = 100;
  std::uniform_int_distribution<> StickyBun::distrib(0, static_cast<int>(MAX_VALUE) - 1);
  std::string StickyBun::META_KEY_TEMPLATE = "StickyBun_";

  StickyBun::StickyBun(UUID eventId, int size) : eventId(std::move(eventId)) {
    values.clear();
    for (int i = 1; i <= size; ++i) {
      values.push_back(distrib(gen));
    }
  }

  StickyBun::StickyBun(UUID eventId, std::vector<int> values) : eventId(std::move(eventId)),
                                                                values(std::move(values)) {}

  std::string StickyBun::computeMetaKey(const UUID &id) {
    std::ostringstream oss;
    oss << StickyBun::META_KEY_TEMPLATE << id;
    return oss.str();
  }

  std::vector<Note> StickyBun::replaceAtonal(std::vector<Note> source, const std::vector<Note> &voicingNotes) const {
    if (values.empty()) return source;

    std::vector<Note> sourceNotes = std::move(source);

    for (auto i = 0; i < sourceNotes.size(); i++)
      if (sourceNotes.at(i).isAtonal()) {
        sourceNotes[i] = compute(voicingNotes, i);
      }

    return sourceNotes;
  }

  Note StickyBun::compute(std::vector<Note> voicingNotes, int index) const {
    float valueRatio =
        static_cast<float>(values[std::min(index, static_cast<int>(values.size()) - 1)]) /
        static_cast<float>(MAX_VALUE);
    return voicingNotes[
        static_cast<int>(std::max(0.0f,
                                  std::min(static_cast<float>(voicingNotes.size()) - 1,
                                           std::floor(static_cast<float>((voicingNotes.size() - 1)) * valueRatio))))
    ];
  }

  std::string StickyBun::computeMetaKey() const {
    return computeMetaKey(eventId);
  }

}// namespace Music