// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.craft.chord;

import io.xj.core.model.phase_chord.PhaseChord;
import io.xj.music.AdjSymbol;
import io.xj.music.PitchClass;

import com.google.common.collect.Lists;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 Sequence of chords
 likely related to [#154234716] ingest of library contents includes sequences of chords.
 <p>
 A regular ChordProgression has no actual time or pitch classes, but a PhaseChordProgression has all those things because it's made of real actual chords in a phase.
 <p>
 FUTURE: evaluate audio chords. As of this message, there is no actual implementation of fabrication based on audio chords.
 FUTURE: implementation of AudioChordProgression will examine opportunity to extend a common ChordProgression interface.
 */
public class PhaseChordProgression {
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
   @param spacing          # beats spacing in between chords
   */
  public PhaseChordProgression(ChordProgression chordProgression, BigInteger phaseId, PitchClass rootPitchClass, Double spacing) {
    chords = Lists.newArrayList();
    parentId = phaseId;
    List<ChordNode> units = chordProgression.getChordNodes();
    double cursor = 0.0;
    for (ChordNode unit : units)
      if (Objects.nonNull(unit.getDelta()) && Objects.nonNull(unit.getForm())) {
        chords.add(buildPhaseChord(phaseId, cursor,
          rootPitchClass.step(unit.getDelta()).getPitchClass(),
          unit.getForm()));
        cursor += spacing;
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
  private static PhaseChord buildPhaseChord(BigInteger phaseId, Double position, PitchClass rootPitchClass, String form) {
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

}
