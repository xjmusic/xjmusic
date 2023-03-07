// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.music;

import com.google.api.client.util.Strings;
import io.xj.lib.util.Text;

import java.util.Objects;

/**
 Chord in a particular key
 */
public class Chord implements Comparable<Chord> {
  public static final String NO_CHORD_NAME = "NC";
  protected final String description;
  // Root Pitch Class
  protected final PitchClass root;
  // Slash Root Pitch Class
  protected final SlashRoot slashRoot;
  // the (flat/sharp) adjustment symbol, which will be used to express this chord
  protected final Accidental accidental;

  public Chord(String input) {

    // Don't set values if there's nothing to set
    if (Objects.isNull(input) || input.length() == 0) {
      description = "";
      root = PitchClass.None;
      slashRoot = SlashRoot.none();
      accidental = Accidental.None;
      return;
    }

    // store original name
    var name = Text.stripExtraSpaces(input);

    // determine whether the name is "sharps" or "flats"
    accidental = Accidental.of(name);

    // Root utility separates root from remaining text
    Root rooter = Root.of(name);

    // parse the root, and keep the remaining string
    this.root = rooter.getPitchClass();

    // parse the description all together, before removing the slash root
    var raw = Text.stripExtraSpaces(rooter.getRemainingText());
    var normalized = ChordForm.normalize(raw);

    // parse the slash root
    slashRoot = SlashRoot.of(normalized);

    // save the description without the slash root, normalizing again in case we missed one because of the slash
    // but if we did make a substitution the first time (raw==normalized) then do not normalize again (this is wrong)
    this.description = Objects.equals(raw, normalized) ? ChordForm.normalize(slashRoot.getPre()) : slashRoot.getPre();
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
    return String.format("%s%s%s",
      root.toString(accidental),
      Strings.isNullOrEmpty(description) ? "" : String.format(" %s", description),
      slashRoot.display(accidental));
  }

  /**
   @return the chord root pitch class
   */
  public PitchClass getRoot() {
    return root;
  }

  /**
   XJ understands the root of a slash chord https://www.pivotaltracker.com/story/show/176728338
   */
  public PitchClass getSlashRoot() {
    return PitchClass.None != slashRoot.getPitchClass() ? slashRoot.getPitchClass() : root;
  }

  /**
   @return the chord adjustment symbol
   */
  public Accidental getAdjSymbol() {
    return accidental;
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
    return Objects.equals(root, other.root)
      && Objects.equals(description, other.description)
      && slashRoot.isSame(other.slashRoot);
  }

  /**
   Whether one chord is acceptable as a substitute another

   @param other chord to test
   @return true if acceptable
   */
  public boolean isAcceptable(Chord other) {
    return Objects.equals(root, other.root) && Objects.equals(description, other.description);
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
