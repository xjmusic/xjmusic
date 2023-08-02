package io.xj.lib.music;

import io.xj.lib.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 *
 */
public enum ChordForm {
  ;

  static final List<Form> forms = List.of(
    new Form("", List.of(new Synonym("maj"), new Synonym("major"), new Synonym("M", true))),
    new Form("-", List.of(new Synonym("min"), new Synonym("minor"), new Synonym("m", true), new Synonym("mi"))),
    new Form("maj7", List.of(new Synonym("major7"), new Synonym("M7", true), new Synonym("∆"))),
    new Form("-7", List.of(new Synonym("min7"), new Synonym("minor7"), new Synonym("m7", true), new Synonym("mi7"))),
    new Form("dim", List.of(new Synonym("diminished"), new Synonym("°"))),
    new Form("dim7", List.of(new Synonym("diminished7"), new Synonym("°7"))),
    new Form("-7b5", List.of(new Synonym("min7b5"), new Synonym("minor7b5"), new Synonym("m7b5", true), new Synonym("Ø"), new Synonym("half dim"), new Synonym("half diminished"))),
    new Form("+", List.of(new Synonym("aug"), new Synonym("augmented"), new Synonym("#5"))),
    new Form("+7", List.of(new Synonym("aug7"), new Synonym("augmented7"), new Synonym("7#5"))),
    new Form("7", List.of(new Synonym("dom7"), new Synonym("dominant 7"), new Synonym("dominant"))),
    new Form("7/9", List.of(new Synonym("dom7/9"), new Synonym("dominant 7/9"), new Synonym("9"), new Synonym("7add9"))),
    new Form("7/9/13", List.of(new Synonym("dom 7/9/13"), new Synonym("dominant 7/9/13"), new Synonym("7/6/9"), new Synonym("dom7 6/9"), new Synonym("dominant 7 6/9"))),
    new Form("7/13", List.of(new Synonym("dom 7/13"), new Synonym("dominant 7/13"), new Synonym("7/6"), new Synonym("dom7/6"), new Synonym("dominant 7/6"), new Synonym("13"))),
    new Form("7b9", List.of(new Synonym("dom7b9"), new Synonym("dominant 7b9"))),
    new Form("7b9b13", List.of(new Synonym("dom7b9b13"), new Synonym("dominant 7b9b13"))),
    new Form("7b9/13", List.of(new Synonym("dom7b9/13"), new Synonym("dominant 7 b9/13"))),
    new Form("7#9", List.of(new Synonym("dom7#9"), new Synonym("dominant 7#9"))),
    new Form("7#9b13", List.of(new Synonym("dom7#9b13"), new Synonym("dominant 7#9b13"))),
    new Form("7/9b13", List.of(new Synonym("dom7/9b13"), new Synonym("dominant 7/9b13"))),
    new Form("7#11", List.of(new Synonym("dom7#11"), new Synonym("dominant 7#11"), new Synonym("7b5"), new Synonym("dom7b5"), new Synonym("dominant 7b5"))),
    new Form("7add4", List.of(new Synonym("dom7add4"), new Synonym("dominant 7add4"), new Synonym("7/11"), new Synonym("11"), new Synonym("dom7/11"), new Synonym("dominant7/11"))),
    new Form("7sus4", List.of(new Synonym("dom7sus4"), new Synonym("dominant 7sus4"), new Synonym("7sus"))),
    new Form("sus4", List.of(new Synonym("sus"))),
    new Form("sus2", List.of(new Synonym("5add9"), new Synonym("5add2"))),
    new Form("5", List.of()),
    new Form("add9", List.of(new Synonym("maj add9"), new Synonym("major add9"), new Synonym("Madd2", true), new Synonym("Madd9", true), new Synonym("add2"), new Synonym("maj add2"), new Synonym("major add2"))),
    new Form("-7/9", List.of(new Synonym("min7/9"), new Synonym("minor7/9"), new Synonym("m7/9", true), new Synonym("m7add9", true), new Synonym("min7add9"), new Synonym("minor7add9"), new Synonym("mi7/9"), new Synonym("mi7add9"), new Synonym("-7add9"))),
    new Form("maj7/9", List.of(new Synonym("major7/9"), new Synonym("M7/9", true), new Synonym("M7add9", true), new Synonym("∆9"), new Synonym("maj7add9"), new Synonym("major7add9"), new Synonym("maj7add2"), new Synonym("major7add2"))),
    new Form("6", List.of(new Synonym("maj6"), new Synonym("major6"), new Synonym("M6", true))),
    new Form("6/9", List.of(new Synonym("maj6/9"), new Synonym("major6/9"), new Synonym("M6/9", true), new Synonym("M6add9", true), new Synonym("6add9"), new Synonym("maj6add9"), new Synonym("major6add9"))),
    new Form("-6", List.of(new Synonym("min6"), new Synonym("minor6"), new Synonym("m6", true), new Synonym("mi6"))),
    new Form("add4", List.of(new Synonym("maj add4"), new Synonym("major add4"), new Synonym("Madd4", true), new Synonym("Madd11", true), new Synonym("add11"), new Synonym("maj add11"), new Synonym("major add11"))),
    new Form("-7/11", List.of(new Synonym("min7/11"), new Synonym("minor7/11"), new Synonym("m7/11", true), new Synonym("m11", true), new Synonym("-11"), new Synonym("min11"), new Synonym("minor11"), new Synonym("mi11"), new Synonym("mi7/11"))),
    new Form("-7b5/11", List.of(new Synonym("-7b5/11"), new Synonym("minor7b5/11"), new Synonym("m7b5/11", true), new Synonym("Ø11"), new Synonym("half dim 11"), new Synonym("half diminished 11"))),
    new Form("maj7#11", List.of(new Synonym("major7#11"), new Synonym("M7#11", true), new Synonym("∆#11"))),
    new Form("maj7/13", List.of(new Synonym("major7/13"), new Synonym("M7/13", true), new Synonym("∆13"))),
    new Form("dim maj7", List.of(new Synonym("diminished major 7"), new Synonym("dimM7", true), new Synonym("dim♮7"), new Synonym("°M7"))),
    new Form("min/maj7", List.of(new Synonym("-maj7"), new Synonym("minor/maj7"), new Synonym("mM7", true), new Synonym("min∆"), new Synonym("-∆"), new Synonym("mi/maj7"), new Synonym("minor/major7"), new Synonym("minor major 7"))),
    new Form("aug maj7", List.of(new Synonym("+Maj7"), new Synonym("augmented major 7"), new Synonym("+M7", true), new Synonym("augM7", true), new Synonym("aug∆"), new Synonym("+∆"), new Synonym("maj7#5"), new Synonym("major7#5"), new Synonym("+♮7"), new Synonym("∆#5"))),
    new Form("-b6", List.of(new Synonym("minb6"), new Synonym("minor b6"), new Synonym("mb6", true))),
    new Form("-13", List.of(new Synonym("min13"), new Synonym("minor 13"), new Synonym("m13", true), new Synonym("-7/13"), new Synonym("min7/13"), new Synonym("minor7/13")))
  );

  /**
   * Parse a chord description and return its most basic representation from the form dictionary
   *
   * @param input to parse
   * @return most basic synonym, or original if already most basic
   */
  public static String normalize(String input) {
    return forms.stream()
      .filter(form -> form.matches(input))
      .findAny()
      .map(form -> form.description)
      .orElse(input);
  }

  /**
   * One Chord form has a basic description with many potential synonyms
   */
  record Form(String description, List<Synonym> synonyms) {

    public boolean matches(String input) {
      if (StringUtils.isNullOrEmpty(input)) return false;
      return synonyms.parallelStream().anyMatch(synonym -> synonym.matches(input));
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) return true;
      if (obj == null || obj.getClass() != this.getClass()) return false;
      var that = (Form) obj;
      return Objects.equals(this.description, that.description) &&
        Objects.equals(this.synonyms, that.synonyms);
    }

    @Override
    public int hashCode() {
      return Objects.hash(description, synonyms);
    }

    @Override
    public String toString() {
      return "Form[" +
        "description=" + description + ", " +
        "synonyms=" + synonyms + ']';
    }

  }

  /**
   * Each potential synonym
   */
  record Synonym(String match, Boolean caseSensitive) {
    Synonym(String match) {
      this(match, false);
    }

    public boolean matches(String input) {
      return caseSensitive ? Objects.equals(match, input) : Objects.equals(match.toLowerCase(Locale.ROOT), input.toLowerCase(Locale.ROOT));
    }
  }
}
