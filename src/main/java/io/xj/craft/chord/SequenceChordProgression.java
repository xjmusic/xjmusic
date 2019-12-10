// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.chord;

import com.google.common.collect.Lists;
import io.xj.core.model.ProgramSequence;
import io.xj.core.model.ProgramSequenceChord;
import io.xj.music.AdjSymbol;
import io.xj.music.PitchClass;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 Sequence of entities
 likely related to [#154234716] ingest of library contents includes sequences of entities.
 <p>
 A regular ChordProgression has no actual time or pitch classes, but a SequenceChordProgression has all those things because it's made of real actual entities in a sequence.
 <p>
 FUTURE: ingest audio entities. As of this message, there is no actual implementation of fabrication based on audio entities.
 FUTURE: implementation of AudioChordProgression will examine opportunity to extend a common ChordProgression interface.
 */
public class SequenceChordProgression {
  private List<ProgramSequenceChord> chords;
  private UUID parentId;

  /**
   Constructor with parent id and list of entities

   @param programSequence of sequence
   @param chords          to store in sequence
   */
  public SequenceChordProgression(ProgramSequence programSequence, List<ProgramSequenceChord> chords) {
    this.parentId = programSequence.getId();
    this.chords = Lists.newArrayList(chords);
  }

  /**
   Construct a sequence chord progression of a chord progression, a sequence id, and the initial chord root pitch class
   NOTE: each ChordEntity change delta is considered to be semitones of the root pitch class provided.
   <p>
   // FUTURE: use more colloquial name than descriptor when creating sequence chord name, e.g. "C7b9" not "C MajorSevenFlatNine"

   @param chordProgression to construct sequence chord progression of
   @param programSequence  of sequence to of sequence entities in
   @param rootPitchClass   of initial chord
   @param spacing          # beats spacing in between entities
   */
  public SequenceChordProgression(ChordProgression chordProgression, ProgramSequence programSequence, PitchClass rootPitchClass, Double spacing) {
    chords = Lists.newArrayList();
    List<ChordNode> units = chordProgression.getChordNodes();
    double cursor = 0.0;
    for (ChordNode unit : units)
      if (Objects.nonNull(unit.getDelta()) && Objects.nonNull(unit.getForm())) {
        chords.add(buildSequenceChord(programSequence, cursor,
          rootPitchClass.step(unit.getDelta()).getPitchClass(),
          unit.getForm()));
        cursor += spacing;
      }
  }

  /**
   Create a new sequence chord in a sequence, of a root pitch class and form

   @param programSequence of sequence to of chord in
   @param position        to of chord at
   @param rootPitchClass  of chord
   @param form            of chord
   @return new sequence chord
   */
  private static ProgramSequenceChord buildSequenceChord(ProgramSequence programSequence, Double position, PitchClass rootPitchClass, String form) {
    return new ProgramSequenceChord(programSequence)
      .setProgramId(programSequence.getProgramId())
      .setPosition(position)
      .setName(String.format("%s %s",
        rootPitchClass.toString(AdjSymbol.of(form)),
        form));
  }

  /**
   Get parent id

   @return id
   */
  public UUID getParentId() {
    return parentId;
  }

  /**
   Get root chord

   @return root chord
   */
  ProgramSequenceChord getRootChord() {
    return chords.get(0);
  }

  /**
   Get a list of all entities in the sequence

   @return list of entities
   */
  public List<ProgramSequenceChord> getChords() {
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
