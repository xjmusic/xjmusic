// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/fabricator/NotePicker.h"

using namespace XJ;


NotePicker::NotePicker(const NoteRange& targetRange, const std::set<Note>& voicingNotes, const bool seekInversions)  {
  this->targetRange = NoteRange::copyOf(targetRange);
  this->voicingNotes = std::set(voicingNotes);
  this->voicingRange = NoteRange::ofNotes(voicingNotes);
  this->seekInversions = seekInversions;
}

NotePicker::NotePicker(const NoteRange& targetRange, const std::vector<Note>& voicingNotes, const bool seekInversions)  {
  this->targetRange = NoteRange::copyOf(targetRange);
  this->voicingNotes = std::set(voicingNotes.begin(), voicingNotes.end());
  this->voicingRange = NoteRange::ofNotes(voicingNotes);
  this->seekInversions = seekInversions;
}


Note NotePicker::pick(const Note eventNote) {
  const auto noteInAvailableOctave = voicingRange.toAvailableOctave(eventNote);

  std::optional<Note> picked = std::nullopt;

  if (Atonal == noteInAvailableOctave.pitchClass) {
    picked = pickRandom(voicingNotes);
  } else {
    std::vector<RankedNote> rankedNotes;
    rankedNotes.reserve(voicingNotes.size());
    for (const auto &vN: voicingNotes) {
      rankedNotes.emplace_back(vN, std::abs(vN.delta(noteInAvailableOctave)));
    }

    // Find the minimum delta
    const auto minElementIter = std::min_element(rankedNotes.begin(), rankedNotes.end(), [](const RankedNote &a, const RankedNote &b) {
      return abs(a.getDelta()) < abs(b.getDelta());
    });

    // If a minimum element was found
    if (minElementIter != rankedNotes.end()) {
      const Note voicingNote = minElementIter->getTones();
      picked = {seekInversion(voicingNote, targetRange, voicingNotes)};
    }
  }

  // Pick the note
  if (picked.has_value()) {
    // Keep track of the total range of notes selected, to keep voicing in the tightest possible range
    targetRange.expand(picked.value());
    return removePicked(picked.value());
  }

  return {};
}


NoteRange NotePicker::getTargetRange()  {
  return targetRange;
}


Note NotePicker::removePicked(const Note picked)  {
  const auto it = voicingNotes.find(picked);
  if (it != voicingNotes.end()) {
    voicingNotes.erase(it);
  }
  return picked;
}


Note NotePicker::seekInversion(const Note source, const NoteRange &range, const std::set<Note> &options) const {
  if (!seekInversions) return source;

  if (range.high.has_value() && range.high.value() < source) {
    std::optional<Note> alt = std::nullopt;
    for (const auto &o : options) {
      if (range.high.value() >= o) {
        const auto delta = std::abs(o.delta(range.high.value()));
        if (!alt.has_value() || delta < alt.value().delta(range.high.value())) {
          alt = {o};
        }
      }
    }
    if (alt.has_value()) return alt.value();
  }

  if (range.low.has_value() && range.low.value() > source) {
    std::optional<Note> alt = std::nullopt;
    for (const auto &o : options) {
      if (range.low.value() <= o) {
        const auto delta = std::abs(o.delta(range.low.value()));
        if (!alt.has_value() || delta < alt.value().delta(range.low.value())) {
          alt = {o};
        }
      }
    }
    if (alt.has_value()) return alt.value();
  }

  return source;
}


std::optional<Note> NotePicker::pickRandom(std::set<Note> fromNotes) {
  if (fromNotes.empty()) return std::nullopt;
  std::vector fromNotesVec(fromNotes.begin(), fromNotes.end());
  return {fromNotesVec[MarbleBag::quickPick(fromNotes.size())]};
}

