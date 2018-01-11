// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.music;

import io.xj.music.schema.ChordForm;
import io.xj.music.schema.ChordForms;
import io.xj.music.schema.IntervalPitchGroup;

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.function.Consumer;

/**
 Chord in a particular key
 */
public class Chord extends IntervalPitchGroup {
  List<ChordForm> forms;

  /**
   Construct an empty Chord
   */
  public Chord() {
    super("");
  }

  /**
   Construct a new Chord of a particular key, e.g. of("C minor 7")

   @param name of chord
   */
  public Chord(String name) {
    super(name);
  }

  /**
   Chord of a particular key, e.g. of("C minor 7")

   @param name of Chord
   @return new Chord
   */
  public static Chord of(String name) {
    return new Chord(name);
  }

  public List<ChordForm> getForms() {
    return Collections.unmodifiableList(forms);
  }

  public Chord setForms(List<ChordForm> forms) {
    this.forms = Lists.newArrayList(forms);
    return this;
  }

  /**
   Copies this object to a new Chord

   @return new note
   */
  private Chord copy() {
    return new Chord()
      .setRootPitchClass(root)
      .setAdjSymbol(adjSymbol)
      .setTones(tones)
      .setOriginalDescription(getOriginalDescription())
      .setForms(forms);
  }

  /**
   Transpose a chord +/- semitones
   */
  public Chord transpose(int deltaSemitones) {
    return copy()
      .setAdjSymbol(adjSymbol)
      .setRootPitchClass(root.step(deltaSemitones).getPitchClass());
  }

  /**
   Build the chord by processing all Forms against the given name.
   <p>
   We don't delete intervals until the end of the function,
   in case an interval is double-added in the middle of the process
   */
  protected void parseSchema(String text) {
    List<Interval> toDelete = Lists.newArrayList();
    forms = Lists.newArrayList();

    // consumer tests (and applies, if matching) a chord form
    Consumer<? super ChordForm> applyForm = chordForm -> {
      if (chordForm.in(text)) {
        forms.add(chordForm);
        tones.putAll(chordForm.getAdd());
        toDelete.addAll(chordForm.getOmit());
      }
    };

    // Check all chord forms; apply any that match
    ChordForms.forEach(applyForm);

    // finally delete anything that's been set for deletion
    toDelete.forEach(tones::remove);
  }

  /**
   Build a string from the list of chord forms

   @return chord form string
   */
  public String formString() {
    int size = forms.size();
    String[] formStrings = new String[size];
    for (int i = 0; i < size; i++) {
      formStrings[i] = forms.get(i).getName();
    }
    return String.join(" ", formStrings);
  }

  private Chord setTones(SortedMap<Interval, Integer> tones) {
    this.tones = tones;
    return this;
  }

  private Chord setOriginalDescription(String description) {
    this.description = description;
    return this;
  }

  private Chord setRootPitchClass(PitchClass root) {
    this.root = root;
    return this;
  }

  private Chord setAdjSymbol(AdjSymbol adjSymbol) {
    this.adjSymbol = adjSymbol;
    return this;
  }

  public String officialDescription() {
    return root.toString(adjSymbol) + " " + formString();
  }

  /**
   Retrieve the colloquial name of a form, if exists
   @return colloquial name of form
   */
  public String colloquialFormName() {
    return ChordForms.colloquialFormNames.getOrDefault(formString(), formString());
  }
}
