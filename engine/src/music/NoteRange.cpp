// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <sstream>

#include "xjmusic/music/NoteRange.h"

using namespace XJ;


NoteRange::NoteRange() : low(std::nullopt), high(std::nullopt) {}


NoteRange::NoteRange(const std::optional<Note> low, const std::optional<Note> high) : low(low),
                                                                          high(high) {
}


NoteRange::NoteRange(const std::string &low, const std::string &high) : NoteRange(Note::ifTonal(low),
                                                                                  Note::ifTonal(high)) {}


NoteRange NoteRange::from(Note low, Note high) {
  return {
      low.isAtonal() ? std::nullopt : std::optional(low),
      high.isAtonal() ? std::nullopt : std::optional(high)
  };
}


NoteRange NoteRange::from(const std::string &low, const std::string &high) {
  return {low, high};
}


NoteRange NoteRange::copyOf(const NoteRange &range) {
  return {range.low, range.high};
}


NoteRange NoteRange::ofNotes(std::vector<Note> notes) {
  const std::set<Note> properNotes(notes.begin(), notes.end());
  return ofNotes(properNotes);
}


NoteRange NoteRange::ofNotes(const std::set<Note>& notes) {
  const auto minNote = std::min_element(notes.begin(), notes.end());
  const auto maxNote = std::max_element(notes.begin(), notes.end());
  std::optional<Note> low = minNote != notes.end() ? std::optional(*minNote) : std::nullopt;
  std::optional<Note> high = maxNote != notes.end() ? std::optional(*maxNote) : std::nullopt;
  return {low, high};
}


NoteRange NoteRange::ofStrings(const std::vector<std::string> &notes) {
  std::set<Note> properNotes;
  for (const auto &note: notes) {
    auto n = Note::ifValid(note);
    if (n.has_value()) properNotes.emplace(n.value());
  }
  return ofNotes(properNotes);
}


NoteRange NoteRange::median(const NoteRange &r1, const NoteRange &r2) {
  return {Note::median(r1.low, r2.low), Note::median(r1.high, r2.high)};
}


int NoteRange::computeMedianOptimalRangeShiftOctaves(const NoteRange *sourceRange, const NoteRange *targetRange) {
  if (!sourceRange->low.has_value() || !sourceRange->high.has_value() ||
      !targetRange->low.has_value() || !targetRange->high.has_value())
    return 0;

  int shiftOctave = 0;    // search for optimal value
  int baselineDelta = 100;// optimal is the lowest possible integer zero or above

  for (int o = 10; o >= -10; o--) {
    if (!targetRange->low.has_value())
      throw std::runtime_error("Can't find low end of target range");
    if (!sourceRange->low.has_value())
      throw std::runtime_error("Can't find low end of source range");
    const int dLow = targetRange->low->delta(sourceRange->low->shiftOctave(o));

    if (!targetRange->high.has_value())
      throw std::runtime_error("Can't find high end of target range");
    if (!sourceRange->high.has_value())
      throw std::runtime_error("Can't find high end of source range");
    const int dHigh = targetRange->high->delta(sourceRange->high->shiftOctave(o));

    if (0 <= dLow && 0 >= dHigh && std::abs(o) < baselineDelta) {
      baselineDelta = std::abs(o);
      shiftOctave = o;
    }
  }

  return shiftOctave;
}


std::string NoteRange::toString(const Accidental accidental) {
  std::stringstream ss;
  if (low.has_value() && high.has_value()) {
    ss << low->toString(accidental) << "-" << high->toString(accidental);
    return ss.str();
  }
  if (low.has_value()) {
    ss << low->toString(accidental);
    return ss.str();
  }
  if (high.has_value()) {
    ss << high->toString(accidental);
    return ss.str();
  }
  return UNKNOWN;
}


void NoteRange::expand(const std::vector<Note> &notes) {
  for (const auto note: notes) expand(note);
}


void NoteRange::expand(Note note) {
  if (!low.has_value() || note < low) low = note;
  if (!high.has_value() || note > high) high = note;
}


void NoteRange::expand(const NoteRange *range) {
  if (range->low.has_value()) expand(range->low.value());
  if (range->high.has_value()) expand(range->high.value());
}


int NoteRange::getDeltaSemitones(NoteRange target) {
  const auto s = getMedianNote();
  const auto t = target.getMedianNote();
  if (!s.has_value() || !t.has_value()) return 0;
  return s.value().delta(t.value());
}


std::optional<Note> NoteRange::getMedianNote() {
  if (!low.has_value() && !high.has_value()) return std::nullopt;
  if (!low.has_value()) return {high};
  if (!high.has_value()) return {low};
  return {low->shift(low->delta(high.value()) / 2)};
}


NoteRange NoteRange::shifted(const int inc) const {
  return {
      low.has_value() ? std::optional(low->shift(inc)) : std::nullopt,
      high.has_value() ? std::optional(high->shift(inc)) : std::nullopt};
}


bool NoteRange::empty() const {
  return !low.has_value() || !high.has_value() || Atonal == low->pitchClass ||
         Atonal == high->pitchClass;
}


std::optional<Note> NoteRange::getNoteNearestMedian(const PitchClass root) {
  if (Atonal == root) return std::nullopt;
  auto median = getMedianNote();
  if (!median.has_value()) return std::nullopt;
  if (root == median->pitchClass) return median;
  auto up = median->nextUp(root);
  auto down = median->nextDown(root);
  return down.delta(median.value()) < median->delta(up) ? std::optional(down) : std::optional(up);
}


Note NoteRange::toAvailableOctave(const Note note) const {
  if (!low.has_value() || !high.has_value()) return note;

  int d = 0;
  Note x = note;

  while (!includes(x) && d < MAX_SEEK_OCTAVES) {
    if (low > x) {
      x = x.shiftOctave(1);
      d++;
    } else if (high < x) {
      x = x.shiftOctave(-1);
      d++;
    }
  }

  return x;
}


bool NoteRange::includes(const Note note) const {
  if (!low.has_value() && !high.has_value()) return false;
  if (!low.has_value() && high.value() == note) return true;
  if (!high.has_value() && low.value() == note) return true;
  if (!low.has_value() || !high.has_value()) return false;
  return low.value() <= note && high.value() >= note;
}