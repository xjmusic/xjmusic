package io.xj.lib.music;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Objects;

/**
 *
 */
public enum ChordDescription {
  ;

  static final List<Form> forms = Lists.newArrayList(
    new Form("", Lists.newArrayList(new Synonym(""), new Synonym("maj"), new Synonym("major"), new Synonym("M"))),
    new Form("-", Lists.newArrayList(new Synonym("min"), new Synonym("minor"), new Synonym("m"), new Synonym("mi"))),
    new Form("maj7", Lists.newArrayList(new Synonym("major7"), new Synonym("M7"), new Synonym("∆"))),
    new Form("-7", Lists.newArrayList(new Synonym("min7"), new Synonym("minor7"), new Synonym("m7"), new Synonym("mi7"))),
    new Form("dim", Lists.newArrayList(new Synonym("diminished"), new Synonym("°"))),
    new Form("dim7", Lists.newArrayList(new Synonym("diminished7"), new Synonym("°7"))),
    new Form("-7b5", Lists.newArrayList(new Synonym("min7b5"), new Synonym("minor7b5"), new Synonym("m7b5"), new Synonym("Ø"), new Synonym("half dim"), new Synonym("half diminished"))),
    new Form("+", Lists.newArrayList(new Synonym("aug"), new Synonym("augmented"), new Synonym("#5"))),
    new Form("+7", Lists.newArrayList(new Synonym("aug7"), new Synonym("augmented7"), new Synonym("7#5"))),
    new Form("7", Lists.newArrayList(new Synonym("dom7"), new Synonym("dominant 7"), new Synonym("dominant"))),
    new Form("7/9", Lists.newArrayList(new Synonym("dom7/9"), new Synonym("dominant 7/9"), new Synonym("9"), new Synonym("7add9"))),
    new Form("7/9/13", Lists.newArrayList(new Synonym("dom 7/9/13"), new Synonym("dominant 7/9/13"), new Synonym("7/6/9"), new Synonym("dom7 6/9"), new Synonym("domininant 7 6/9"))),
    new Form("7/13", Lists.newArrayList(new Synonym("dom 7/13"), new Synonym("dominant 7/13"), new Synonym("7/6"), new Synonym("dom7/6"), new Synonym("dominant 7/6"), new Synonym("13"))),
    new Form("7b9", Lists.newArrayList(new Synonym("dom7b9"), new Synonym("dominant 7b9"))),
    new Form("7b9b13", Lists.newArrayList(new Synonym("dom7b9b13"), new Synonym("dominant 7b9b13"))),
    new Form("7b9/13", Lists.newArrayList(new Synonym("dom7b9/13"), new Synonym("dominant 7 b9/13"))),
    new Form("7#9", Lists.newArrayList(new Synonym("dom7#9"), new Synonym("dominant 7#9"))),
    new Form("7#9b13", Lists.newArrayList(new Synonym("dom7#9b13"), new Synonym("dominant 7#9b13"))),
    new Form("7/9b13", Lists.newArrayList(new Synonym("dom7/9b13"), new Synonym("dominant 7/9b13"))),
    new Form("7#11", Lists.newArrayList(new Synonym("dom7#11"), new Synonym("dominant 7#11"), new Synonym("7b5"), new Synonym("dom7b5"), new Synonym("dominant 7b5"))),
    new Form("7add4", Lists.newArrayList(new Synonym("dom7add4"), new Synonym("dominant 7add4"), new Synonym("7/11"), new Synonym("11"), new Synonym("dom7/11"), new Synonym("dominant7/11"))),
    new Form("7sus4", Lists.newArrayList(new Synonym("dom7sus4"), new Synonym("dominant 7sus4"), new Synonym("7sus"))),
    new Form("sus4", Lists.newArrayList(new Synonym("sus"))),
    new Form("sus2", Lists.newArrayList(new Synonym("5add9"), new Synonym("5add2"))),
    new Form("5", Lists.newArrayList()),
    new Form("add9", Lists.newArrayList(new Synonym("maj add9"), new Synonym("major add9"), new Synonym("Madd2"), new Synonym("Madd9"), new Synonym("add2"), new Synonym("maj add2"), new Synonym("major add2"))),
    new Form("-7/9", Lists.newArrayList(new Synonym("min7/9"), new Synonym("minor7/9"), new Synonym("m7/9"), new Synonym("m7add9"), new Synonym("min7add9"), new Synonym("minor7add9"), new Synonym("mi7/9"), new Synonym("mi7add9"), new Synonym("-7add9"))),
    new Form("maj7/9", Lists.newArrayList(new Synonym("major7/9"), new Synonym("M7/9"), new Synonym("M7add9"), new Synonym("∆9"), new Synonym("maj7add9"), new Synonym("major7add9"), new Synonym("maj7add2"), new Synonym("major7add2"))),
    new Form("6", Lists.newArrayList(new Synonym("maj6"), new Synonym("major6"), new Synonym("M6"))),
    new Form("6/9", Lists.newArrayList(new Synonym("maj6/9"), new Synonym("major6/9"), new Synonym("M6/9"), new Synonym("M6add9"), new Synonym("6add9"), new Synonym("maj6add9"), new Synonym("major6add9"))),
    new Form("-6", Lists.newArrayList(new Synonym("min6"), new Synonym("minor6"), new Synonym("m6"), new Synonym("mi6"))),
    new Form("add4", Lists.newArrayList(new Synonym("maj add4"), new Synonym("major add4"), new Synonym("Madd4"), new Synonym("Madd11"), new Synonym("add11"), new Synonym("maj add11"), new Synonym("major add11"))),
    new Form("-7/11", Lists.newArrayList(new Synonym("min7/11"), new Synonym("minor7/11"), new Synonym("m7/11"), new Synonym("m11"), new Synonym("-11"), new Synonym("min11"), new Synonym("minor11"), new Synonym("mi11"), new Synonym("mi7/11"))),
    new Form("-7b5/11", Lists.newArrayList(new Synonym("-7b5/11"), new Synonym("minor7b5/11"), new Synonym("m7b5/11"), new Synonym("Ø11"), new Synonym("half dim 11"), new Synonym("half dimished 11"))),
    new Form("maj7#11", Lists.newArrayList(new Synonym("major7#11"), new Synonym("M7#11"), new Synonym("∆#11"))),
    new Form("maj7/13", Lists.newArrayList(new Synonym("major7/13"), new Synonym("M7/13"), new Synonym("∆13"))),
    new Form("dim maj7", Lists.newArrayList(new Synonym("diminished major 7"), new Synonym("dimM7"), new Synonym("dim♮7"), new Synonym("°M7"))),
    new Form("min/maj7", Lists.newArrayList(new Synonym("-maj7"), new Synonym("minor/maj7"), new Synonym("mM7"), new Synonym("min∆"), new Synonym("-∆"), new Synonym("mi/maj7"), new Synonym("minor/major7"), new Synonym("minor major 7"))),
    new Form("aug maj7", Lists.newArrayList(new Synonym("+Maj7"), new Synonym("augmented major 7"), new Synonym("+M7"), new Synonym("augM7"), new Synonym("aug∆"), new Synonym("+∆"), new Synonym("maj7#5"), new Synonym("major7#5"), new Synonym("+♮7"), new Synonym("∆#5"))),
    new Form("-b6", Lists.newArrayList(new Synonym("minb6"), new Synonym("minor b6"), new Synonym("mb6"))),
    new Form("-13", Lists.newArrayList(new Synonym("min13"), new Synonym("minor 13"), new Synonym("m13"), new Synonym("-7/13"), new Synonym("min7/13"), new Synonym("minor7/13")))
  );

  /**
   Parse a chord description and return its most basic representation from the form dictionary

   @param input to parse
   @return most basic synonym, or original if already most basic
   */
  public static String normalize(String input) {
    return forms.stream()
      .filter(form -> form.matches(input))
      .findAny()
      .map(form -> form.description)
      .orElse(input);
  }

  /**
   One Chord form has a basic description with many potential synonyms
   */
  record Form(String description, List<Synonym> synonyms) {
    public boolean matches(String input) {
      return synonyms.stream().anyMatch(synonym -> synonym.matches(input));
    }
  }

  /**
   Each potential synonym
   */
  record Synonym(String match, Boolean caseSensitive) {
    Synonym(String match) {
      this(match, false);
    }

    public boolean matches(String input) {
      return Objects.equals(match, input);
    }
  }
}
