// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.music;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.*;
import java.util.function.Consumer;

/**
 Chord in a particular key
 */
public class Chord extends IntervalPitchGroup {
  public static final String NO_CHORD_NAME = "NC";
  private static final double semitonesModulo = 12;
  private static final double semitonesModuloDeltaMax = semitonesModulo / 2;
  private List<ChordForm> forms;

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

  /**
   Get the mean of a collection of values

   @param values to get mean of
   @return mean
   */
  private static <N extends Number> Double mean(Collection<N> values) {
    double sum = 0.0;
    double count = 0;
    for (N value : values) {
      sum += value.doubleValue();
      count++;
    }
    return sum / count;
  }

  /**
   Assumes a modulo-N system,
   such that the greatest possible delta is N/2,
   ergo returns a ratio from 0 to 1 (no match to full match).
   corresponding to a delta of N/2 to 0.

   @param t1 semitone value 1
   @param t2 semitone value 2
   @return similarity of semitone value 1 and value 2
   */
  private static Double similarity(PitchClass t1, PitchClass t2) {
    return 1 - Math.abs(t1.delta(t2)) / semitonesModuloDeltaMax;
  }

  /**
   Get forms

   @return forms
   */
  List<ChordForm> getForms() {
    return Collections.unmodifiableList(forms);
  }

  /**
   Set forms

   @param forms to set
   @return chord after forms are set
   */
  Chord setForms(List<ChordForm> forms) {
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
  String formString() {
    int size = forms.size();
    String[] formStrings = new String[size];
    for (int i = 0; i < size; i++) {
      formStrings[i] = forms.get(i).getName();
    }
    return String.join(" ", formStrings);
  }

  /**
   Set the tones of the chord.

   @param tones to set
   @return Chord after setting tones
   */
  private Chord setTones(SortedMap<Interval, Integer> tones) {
    this.tones = tones;
    return this;
  }

  /**
   Set the original description of the chord.

   @param description to set
   @return Chord after setting description
   */
  private Chord setOriginalDescription(String description) {
    this.description = description;
    return this;
  }

  /**
   Set the root pitch class of the chord.

   @param root pitch class to set
   @return Chord after setting root pitch class
   */
  private Chord setRootPitchClass(PitchClass root) {
    this.root = root;
    return this;
  }

  /**
   Set the adjustment symbol of the chord.

   @param adjSymbol to set
   @return Chord after setting adjustment symbol
   */
  private Chord setAdjSymbol(AdjSymbol adjSymbol) {
    this.adjSymbol = adjSymbol;
    return this;
  }

  /**
   Get the official description of the chord.

   @return official description
   */
  String officialDescription() {
    return root.toString(adjSymbol) + " " + formString();
  }

  /**
   [#154985948] Architect wants to determine tonal similarity (% of shared pitch classes) between two Chords, in order to perform fuzzy matching operations.

   @param other chord to compare with
   @return ratio (0 to 1) of similarity.
   */
  public Double similarity(Chord other) {
    return (similarityAtSameIntervals(other) + similarityAtAnyIntervals(other)) / 2;
  }

  /**
   [#154985948] Architect wants to determine tonal similarity (% of shared pitch classes) between two Chords, in order to perform fuzzy matching operations.

   @param other chord to compare with
   @return ratio (0 to 1) of similarity.
   */
  private Double similarityAtSameIntervals(Chord other) {
    Map<Interval, Double> pcDeltas = Maps.newHashMap();
    getPitchClasses().forEach(((interval, pitchClass) -> {
      if (other.getPitchClasses().containsKey(interval))
        pcDeltas.put(interval, similarity(pitchClass, other.getPitchClasses().get(interval)));
      else
        pcDeltas.put(interval, 0.0);
    }));
    return mean(pcDeltas.values());
  }

  /**
   [#154985948] Architect wants to determine tonal similarity (% of shared pitch classes) between two Chords, in order to perform fuzzy matching operations.

   @param other chord to compare with
   @return ratio (0 to 1) of similarity.
   */
  private Double similarityAtAnyIntervals(Chord other) {
    Map<PitchClass, Integer> pcDeltas = Maps.newHashMap();
    getPitchClasses().values().forEach((pitchClass -> {
      if (other.getPitchClasses().containsValue(pitchClass))
        pcDeltas.put(pitchClass, 1);
      else
        pcDeltas.put(pitchClass, 0);
    }));
    other.getPitchClasses().values().forEach(pitchClass -> {
      if (!pcDeltas.containsKey(pitchClass))
        pcDeltas.put(pitchClass, 0);
    });
    return mean(pcDeltas.values());
  }

  /**
   Retrieve the colloquial name of a form, if exists

   @return colloquial name of form
   */
  public String colloquialFormName() {
    return ChordForms.colloquialFormNames.getOrDefault(formString(), formString());
  }

  /**
   Whether this is a No Chord instance

   @return true if No Chord
   */
  public Boolean isNoChord() {
    return Objects.equals(root, PitchClass.None);
  }
}
