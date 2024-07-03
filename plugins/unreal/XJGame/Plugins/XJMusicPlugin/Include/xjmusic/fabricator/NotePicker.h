// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_FABRICATOR_NOTE_PICKER_H
#define XJMUSIC_FABRICATOR_NOTE_PICKER_H

#include <set>

#include "xjmusic/music/Note.h"
#include "xjmusic/music/NoteRange.h"
#include "MarbleBag.h"
#include "RankedNote.h"

namespace XJ {

/**
 In order to pick exactly one optimal voicing note for each of the source event notes.
 */
  class NotePicker {
    NoteRange targetRange;
    std::set<Note> voicingNotes;
    bool seekInversions;
    NoteRange voicingRange;

  public:

    /**
     Build a NotePicker from the given optimal target range set

     @param targetRange    optimally picks will be within
     @param voicingNotes   to pick from, at most once each
     @param seekInversions whether to seek inversions
     */
    NotePicker(const NoteRange& targetRange, const std::set<Note>& voicingNotes, bool seekInversions);

    /**
     Build a NotePicker from the given optimal target range vector

     @param targetRange    optimally picks will be within
     @param voicingNotes   to pick from, at most once each
     @param seekInversions whether to seek inversions
     */
    NotePicker(const NoteRange& targetRange, const std::vector<Note>& voicingNotes, bool seekInversions);

    /**
     Pick all voicing notes for event notes
     */
    Note pick(Note eventNote);

    /**
     @return range of picked notes (updated after picking)
     */
    NoteRange getTargetRange();

    /**
     Pick a note, adding it to picked notes and removing it from voicing notes

     @param picked to pick
     */
    Note removePicked(Note picked);

    /**
     Seek the inversion of the given note that is best contained within the given range

     @param source  for which to seek inversion
     @param range   towards which seeking will optimize
     @param options from which to select better notes
     */
    Note seekInversion(Note source, const NoteRange &range, const std::set<Note> &options) const;

    /**
     Pick a random instrument note from the available notes in the voicing
     <p>
     Artist writing detail program expects 'X' note value to result in random selection from available Voicings https://github.com/xjmusic/xjmusic/issues/251

     @param fromNotes to pick from
     @return a random note from the voicing
     */
    static std::optional<Note> pickRandom(std::set<Note> fromNotes);

  };

}// namespace XJ

#endif //XJMUSIC_FABRICATOR_NOTE_PICKER_H