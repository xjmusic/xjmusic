// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <sstream>

#include "xjmusic/entities/music/Tuning.h"

namespace XJ {

  Tuning::Tuning() : rootNote(Note::of("A4")), rootPitch(440) {
    validate();
  }

  Tuning::Tuning(const Note &rootNote, const float &rootPitch) : rootNote(rootNote), rootPitch(rootPitch) {
    validate();
  }

  Tuning Tuning::atA4(float a4) {
    return {Note::of("A4"), a4};
  }

  Tuning Tuning::at(Note note, float pitch) {
    return {note, pitch};
  }

  float Tuning::pitch(Note note) {
    int octave = note.octave;
    PitchClass pitchClass = note.pitchClass;

    if (_notePitches.find(octave) == _notePitches.end())
      _notePitches[octave] = std::map<PitchClass, float>();

    if (_notePitches[octave].find(pitchClass) == _notePitches[octave].end())
      _notePitches[octave][pitchClass] = pitchAtDelta(rootNote.delta(note));

    return _notePitches[octave][pitchClass];
  }

  Note Tuning::getTones(float pitch) {
    if (_pitchNotes.find(pitch) == _pitchNotes.end())
      _pitchNotes.insert({pitch, rootNote.shift(deltaFromRootPitch(pitch))});

    return _pitchNotes.at(pitch);
  }

  int Tuning::deltaFromRootPitch(float pitch) {
    if (_deltaFromRootPitch.find(pitch) == _deltaFromRootPitch.end())
      _deltaFromRootPitch.insert(
          {pitch, static_cast<int>(std::log(pitch / rootPitch) / NATURAL_LOGARITHM_OF_TWELFTH_ROOT_OF_TWO)});

    return _deltaFromRootPitch[pitch];
  }

  float Tuning::pitchAtDelta(int delta) const {
    return static_cast<float>(rootPitch * std::pow(TWELFTH_ROOT_OF_TWO, delta));
  }

  void Tuning::validate() const {
    if (!(ROOT_PITCH_MINIMUM <= rootPitch && ROOT_PITCH_MAXIMUM >= rootPitch)) {
      std::stringstream ss;
      ss << "Root pitch must be between " << ROOT_PITCH_MINIMUM << " and " << ROOT_PITCH_MAXIMUM << " (Hz)";
      throw std::runtime_error(ss.str());
    }

    if (rootNote.pitchClass == PitchClass::Atonal)
      throw std::runtime_error("Root note must have a pitch class (e.g. 'C')");

    if (!(ROOT_OCTAVE_MINIMUM <= rootNote.octave && ROOT_OCTAVE_MAXIMUM >= rootNote.octave)) {
      std::stringstream ss;
      ss << "Root note octave must be between " << ROOT_OCTAVE_MINIMUM << " and " << ROOT_OCTAVE_MAXIMUM;
      throw std::runtime_error(ss.str());
    }
  }

}