// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <cmath>

#include "xjmusic/music/Note.h"
#include "xjmusic/music/Octave.h"

namespace XJ {

  std::regex Note::rgxValidNote("^([ABCDEFGX][#b]*[0-9]*)$");

  // this max is only for extreme-case infinite loop prevention
  int Note::MAX_DELTA_SEMITONES = 1000;

  bool Note::operator==(const Note &other) const {
    return octave == other.octave && pitchClass == other.pitchClass;
  }

  bool Note::operator<(const Note &other) const {
    return octave < other.octave || (octave == other.octave && pitchClass < other.pitchClass);
  }

  bool Note::operator>(const Note &other) const {
    return octave > other.octave || (octave == other.octave && pitchClass > other.pitchClass);
  }

  bool Note::operator<=(const Note &other) const {
    return *this < other || *this == other;
  }

  bool Note::operator>=(const Note &other) const {
    return *this > other || *this == other;
  }

  Note::Note() : pitchClass(PitchClass::Atonal), octave(0) {}// Default constructor

  Note::Note(const std::string &name) : pitchClass(pitchClassOf(name)), octave(octaveOf(name)) {}

  Note::Note(PitchClass pitchClass, int octave) : pitchClass(pitchClass), octave(octave) {}

  Note Note::of(const std::string &name) {
    return name.empty()
           ? Note::atonal()
           : Note(name);
  }

  Note Note::of(PitchClass pitchClass, int octave) {
    return {pitchClass, octave};
  }

  Note Note::atonal() {
    return {PitchClass::Atonal, 0};
  }

  std::optional<Note> Note::ifValid(const std::string &name) {
    return isValid(name)
           ? std::optional<Note>(Note(name))
           : std::nullopt;
  }

  std::optional<Note> Note::ifTonal(const std::string &name) {
    if (!isValid(name)) return std::nullopt;
    auto note = Note::of(name);
    if (note.isAtonal()) return std::nullopt;
    return note;
  }

  bool Note::isValid(const std::string &name) {
    return std::regex_match(accidentalNormalized(name), rgxValidNote);
  }

  bool Note::containsAnyValidNotes(const std::string &noteCsv) {
    std::vector<std::string> notes = StringUtils::split(noteCsv, ',');
    return std::any_of(notes.begin(), notes.end(), isValid);
  }

  std::optional<Note> Note::median(std::optional<Note> n1, std::optional<Note> n2) {
    if (!n1.has_value() && !n2.has_value()) return std::nullopt;
    if (!n1.has_value()) return {*n2};
    if (!n2.has_value()) return {*n1};
    return {n1.value().shift(n1.value().delta(n2.value()) / 2)};
  }

  int Note::delta(const Note &target) const {
    if (*this == target)
      return 0;

    int delta = 0;
    Note noteUp = copy();
    Note noteDown = copy();
    while (delta < MAX_DELTA_SEMITONES) {
      delta++;

      noteUp = noteUp.shift(1);
      if (noteUp == target)
        return delta;

      noteDown = noteDown.shift(-1);
      if (noteDown == target)
        return -delta;
    }
    return 0;
  }

  std::string Note::toString(Accidental accidental) const {
    return stringOf(pitchClass, accidental) + std::to_string(octave);
  }

  Note Note::shift(int inc) const {
    Step step = Step::step(pitchClass, inc);
    return {step.pitchClass, octave + step.deltaOctave};
  }

  Note Note::shiftOctave(int inc) const {
    return shift(12 * inc);
  }

  Note Note::copy() const {
    return {pitchClass, octave};
  }

  Note Note::setOctaveNearest(Note fromNote) {
    if (fromNote.pitchClass == Atonal)
      return *this;
    Note toNote = fromNote.shift(-6);
    while (toNote.pitchClass != pitchClass)
      toNote = toNote.shift(1);
    octave = toNote.octave;
    return *this;
  }

  bool Note::isAtonal() const {
    return pitchClass == Atonal;
  }

  Note Note::nextUp(PitchClass target) {
    return next(target, 1);
  }

  Note Note::nextDown(PitchClass target) {
    return next(target, -1);
  }

  Note Note::next(PitchClass target, int delta) {
    if (isAtonal() || pitchClass == target)
      return *this;
    Note n = *this;
    for (int i = 1; std::abs(i) < MAX_DELTA_SEMITONES; i += delta) {
      n = shift(i);
      if (n.pitchClass == target)
        return n;
    }
    throw std::runtime_error(
        "Unable to determine first occurrence of " + stringOf(target, Accidental::Natural) + "from here!");
  }

}// namespace XJ