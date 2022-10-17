// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.music;

import com.google.api.client.util.Strings;
import io.xj.lib.util.Text;

import java.util.Comparator;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 Chord in a particular key
 */
public class Chord implements Comparable<Chord> {
  private static final Pattern rgxStartsWithSlash = Pattern.compile("^/");
  public static final String NO_CHORD_NAME = "NC";
  protected String preSlash;
  protected String description;
  // Root Pitch Class
  protected PitchClass root;
  // Slash Root Pitch Class
  protected PitchClass slashRoot;
  // the (flat/sharp) adjustment symbol, which will be used to express this chord
  protected AdjSymbol adjSymbol;

  public Chord() {
  }

  public Chord(String input) {

    // Don't set values if there's nothing to set
    if (Objects.isNull(input) || input.length() == 0)
      return;

    // store original name
    var name = Text.stripExtraSpaces(input);

    // determine whether the name is "sharps" or "flats"
    adjSymbol = AdjSymbol.of(name);

    // Root utility separates root from remaining text
    Root rooter = Root.of(name);

    // parse the root, and keep the remaining string
    this.root = rooter.getPitchClass();

    // parse the slash root
    this.slashRoot = SlashRoot.of(name).orDefault(this.root);
    this.preSlash = SlashRoot.pre(name);

    // description is everything AFTER the root, in the original name
    description = ChordDescription.normalize(Text.stripExtraSpaces(rooter.getRemainingText()));
  }

  /**
   String expression of interval pitch group, original name

   @return scale as string
   */
  public String toString() {
    return getName();
  }

  /**
   Delta to another Key calculated in +/- semitones

   @param target key to calculate delta to
   @return delta +/- semitones to another key
   */
  public int delta(Chord target) {
    return root.delta(target.getRoot());
  }

  /**
   Compute the name from the root pitch class and description

   @return chord name
   */
  public String getName() {
    if (Strings.isNullOrEmpty(description))
      return root.toString(adjSymbol);
    if (rgxStartsWithSlash.matcher(description).find())
      return String.format("%s%s", root.toString(adjSymbol), description);
    else
      return String.format("%s %s", root.toString(adjSymbol), description);
  }

  public PitchClass getRoot() {
    return root;
  }

  /**
   https://www.pivotaltracker.com/story/show/176728338 XJ understands the root of a slash chord
   */
  public PitchClass getSlashRoot() {
    return slashRoot;
  }

  public AdjSymbol getAdjSymbol() {
    return adjSymbol;
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
   Copies this object to a new Chord

   @return new note
   */
  private Chord copy() {
    return new Chord()
      .setRootPitchClass(root)
      .setAdjSymbol(adjSymbol)
      .setDescription(getDescription());
  }

  /**
   Set the description

   @param description to set
   @return chord
   */
  private Chord setDescription(String description) {
    this.description = description;
    return this;
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
   Whether this is a No Chord instance

   @return true if No Chord
   */
  public Boolean isNoChord() {
    return Objects.equals(root, PitchClass.None);
  }

  /**
   Whether one chord equals another

   @param other chord to test
   @return true if equal
   */
  public boolean isSame(Chord other) {
    return Objects.equals(root, other.root) && Objects.equals(description, other.description);
  }

  /**
   Whether one chord is acceptable as a substitute another

   @param other chord to test
   @return true if acceptable
   */
  public boolean isAcceptable(Chord other) {
    return Objects.equals(other.preSlash, preSlash);
  }

  /**
   Whether this Chord is null

   @return true if non-null
   */
  public boolean isPresent() {
    return Objects.nonNull(root);
  }

  /**
   Get the description portion of the chord

   @return description
   */
  public String getDescription() {
    return description;
  }

  @Override
  public int compareTo(Chord o) {
    return Objects.equals(root, o.root) ? description.compareTo(o.description) : root.compareTo(o.root);
  }
}
