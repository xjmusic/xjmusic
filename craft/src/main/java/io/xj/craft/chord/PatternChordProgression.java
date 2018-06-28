// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.chord;

import io.xj.core.model.pattern_chord.PatternChord;
import io.xj.music.AdjSymbol;
import io.xj.music.PitchClass;

import com.google.common.collect.Lists;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 Sequence of entities
 likely related to [#154234716] ingest of library contents includes sequences of entities.
 <p>
 A regular ChordProgression has no actual time or pitch classes, but a PatternChordProgression has all those things because it's made of real actual entities in a pattern.
 <p>
 FUTURE: evaluate audio entities. As of this message, there is no actual implementation of fabrication based on audio entities.
 FUTURE: implementation of AudioChordProgression will examine opportunity to extend a common ChordProgression interface.
 */
public class PatternChordProgression {
  private final List<PatternChord> chords;
  private final BigInteger parentId;

  /**
   Constructor with parent id and list of entities

   @param parentId of sequence
   @param chords   to store in sequence
   */
  public PatternChordProgression(BigInteger parentId, List<PatternChord> chords) {
    this.parentId = parentId;
    this.chords = Lists.newArrayList(chords);
  }

  /**
   Construct a pattern chord progression from a chord progression, a pattern id, and the initial chord root pitch class
   NOTE: each Chord change delta is considered to be semitones from the root pitch class provided.
   <p>
   // FUTURE: use more colloquial name than descriptor when creating pattern chord name, e.g. "C7b9" not "C MajorSevenFlatNine"

   @param chordProgression to construct pattern chord progression from
   @param patternId          of pattern to create pattern entities in
   @param rootPitchClass   of initial chord
   @param spacing          # beats spacing in between entities
   */
  public PatternChordProgression(ChordProgression chordProgression, BigInteger patternId, PitchClass rootPitchClass, Double spacing) {
    chords = Lists.newArrayList();
    parentId = patternId;
    List<ChordNode> units = chordProgression.getChordNodes();
    double cursor = 0.0;
    for (ChordNode unit : units)
      if (Objects.nonNull(unit.getDelta()) && Objects.nonNull(unit.getForm())) {
        chords.add(buildPatternChord(patternId, cursor,
          rootPitchClass.step(unit.getDelta()).getPitchClass(),
          unit.getForm()));
        cursor += spacing;
      }
  }

  /**
   Create a new pattern chord in a sequence, from a root pitch class and form

   @param patternId        of pattern to create chord in
   @param position       to create chord at
   @param rootPitchClass of chord
   @param form           of chord
   @return new pattern chord
   */
  private static PatternChord buildPatternChord(BigInteger patternId, Double position, PitchClass rootPitchClass, String form) {
    return new PatternChord(patternId, position,
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
  PatternChord getRootChord() {
    return chords.get(0);
  }

  /**
   Get a list of all entities in the sequence

   @return list of entities
   */
  public List<PatternChord> getChords() {
    return Collections.unmodifiableList(chords);
  }

  /**
   compute the standard descriptor for this sequence of entities

   @return standard descriptor
   */
  public ChordProgression getChordProgression() {
    return ChordProgression.of(chords);
  }

}
