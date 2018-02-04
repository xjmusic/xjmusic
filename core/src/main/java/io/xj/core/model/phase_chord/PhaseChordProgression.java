// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.phase_chord;

import com.google.common.collect.Lists;

import io.xj.core.model.chord.Chord;
import io.xj.core.model.chord.ChordNode;
import io.xj.core.model.chord.ChordProgression;
import io.xj.music.AdjSymbol;
import io.xj.music.PitchClass;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 Sequence of chords
 likely related to [#154234716] ingest of library contents includes sequences of chords.
 <p>
 FUTURE: evaluate audio chords. As of this message, there is no actual implementation of fabrication based on audio chords.
 FUTURE: implementation of AudioChordProgression will examine opportunity to extend a common ChordProgression interface.
 */
public class PhaseChordProgression {
  private static final int DEFAULT_PHASE_CHORD_POSITION_ADVANCE = 4; // # beats to advance between phase chords by default
  private final List<PhaseChord> chords;
  private final BigInteger parentId;

  /**
   Constructor with parent id and list of chords

   @param parentId of sequence
   @param chords   to store in sequence
   */
  public PhaseChordProgression(BigInteger parentId, List<PhaseChord> chords) {
    this.parentId = parentId;
    this.chords = Lists.newArrayList(chords);
  }

  /**
   Construct a phase chord progression from a chord progression, a phase id, and the initial chord root pitch class
   NOTE: each Chord change delta is considered to be semitones from the root pitch class provided.
   <p>
   // FUTURE: use more colloquial name than descriptor when creating phase chord name, e.g. "C7b9" not "C MajorSevenFlatNine"

   @param chordProgression to construct phase chord progression from
   @param phaseId          of phase to create phase chords in
   @param rootPitchClass   of initial chord
   */
  public PhaseChordProgression(ChordProgression chordProgression, BigInteger phaseId, PitchClass rootPitchClass) {
    chords = Lists.newArrayList();
    parentId = phaseId;
    List<ChordNode> units = chordProgression.getChordNodes();
    int n = 0;
    for (ChordNode unit : units)
      if (Objects.nonNull(unit.getDelta()) && Objects.nonNull(unit.getForm())) {
        chords.add(buildPhaseChord(phaseId, n,
          rootPitchClass.step(unit.getDelta()).getPitchClass(),
          unit.getForm()));
        n += DEFAULT_PHASE_CHORD_POSITION_ADVANCE;
      }
  }

  /**
   Create a new phase chord in a sequence, from a root pitch class and form

   @param phaseId        of phase to create chord in
   @param position       to create chord at
   @param rootPitchClass of chord
   @param form           of chord
   @return new phase chord
   */
  private static PhaseChord buildPhaseChord(BigInteger phaseId, Integer position, PitchClass rootPitchClass, String form) {
    return new PhaseChord(phaseId, position,
      String.format("%s %s",
        rootPitchClass.toString(AdjSymbol.of(form)),
        form));
  }

  /**
   Get parent id

   @return id
   */
  public BigInteger getParentId() {
    return parentId;
  }

  /**
   Get root chord

   @return root chord
   */
  PhaseChord getRootChord() {
    return chords.get(0);
  }

  /**
   Get a list of all chords in the sequence

   @return list of chords
   */
  public List<PhaseChord> getChords() {
    return Collections.unmodifiableList(chords);
  }

  /**
   compute the standard descriptor for this sequence of chords

   @return standard descriptor
   */
  public ChordProgression getChordProgression() {
    return ChordProgression.of(chords);
  }

  /**
   Estimate the phase total based on the chords in this sequence

   @return phase total (estimated)
   */
  public Integer estimatePhaseTotal() {
    if (chords.isEmpty()) return 0;
    return chords.get(chords.size() - 1).getPosition() + DEFAULT_PHASE_CHORD_POSITION_ADVANCE;
  }

}
