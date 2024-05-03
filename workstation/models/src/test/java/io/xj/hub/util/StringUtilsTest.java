// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.util;

import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class StringUtilsTest {

  @Test
  public void formatMultiline() {
    assertEquals("Line One" + System.lineSeparator() + "Line Two" + System.lineSeparator(), StringUtils.formatMultiline(new String[]{"Line One", "Line Two"}));
  }

  @Test
  public void orEmptyQuotes() {
    assertEquals("\"\"", StringUtils.orEmptyQuotes(""));
    assertEquals("\"\"", StringUtils.orEmptyQuotes(null));
    assertEquals("tubs", StringUtils.orEmptyQuotes("tubs"));
  }

  @Test
  public void formatStackTrace() {
    assertEquals("", StringUtils.formatStackTrace(null));
  }

  @Test
  public void getSimpleName() {
    assertEquals("Instant", StringUtils.getSimpleName(Instant.now()));
    assertEquals("Instant", StringUtils.getSimpleName(Instant.class));
  }

  @Test
  public void toAlphabetical() {
    assertEquals("Pajamas", StringUtils.toAlphabetical("Pajamas"));
    assertEquals("Pajamas", StringUtils.toAlphabetical("1P34aj2a3ma321s"));
    assertEquals("Pajamas", StringUtils.toAlphabetical("  P#$ aj#$@a   @#$$$$ma         s"));
    assertEquals("Pajamas", StringUtils.toAlphabetical("P_+_+_+_+_(@(#%&!&&&@&%!@)_$*(!_)@()_#()(((a j a m a s "));
    assertEquals("Pajamas", StringUtils.toAlphabetical("Pajamas"));
  }

  @Test
  public void toAlphanumeric() {
    assertEquals("Pajamas", StringUtils.toAlphanumeric("Pajamas!!!!!!"));
    assertEquals("17Pajamas", StringUtils.toAlphanumeric("17 Pajamas?"));
    assertEquals("Pajamas5", StringUtils.toAlphanumeric("  P#$ aj#$@a   @#$$$$ma         s5"));
    assertEquals("Pajamas25", StringUtils.toAlphanumeric("P_+_+_+_+_(@(#%&!&&&@&%!@)_$*(!_)@()_#()(((a j a m a s 2    5"));
    assertEquals("Pajamas", StringUtils.toAlphanumeric("Pajamas"));
  }

  @Test
  public void toAlphanumericHyphenated() {
    assertEquals("I-Love-My-Pajamas", StringUtils.toAlphanumericHyphenated("I Love My Pajamas"));
    assertEquals("I--Love--My--Pajamas-", StringUtils.toAlphanumericHyphenated("I! Love! My! Pajamas!"));
  }

  @Test
  public void toAlphaSlug() {
    assertEquals("THIS_THING", StringUtils.toAlphaSlug("--- ---  T@@@HIS_@THIN!4 G"));
  }

  @Test
  public void toLowerScored() {
    assertEquals("hammy_jammy", StringUtils.toLowerScored("HAMMY jaMMy"));
    assertEquals("jammy", StringUtils.toLowerScored("jaMMy"));
    assertEquals("jam_42", StringUtils.toLowerScored("jaM &&$ 42"));
    assertEquals("jam_42", StringUtils.toLowerScored("  ## jaM &&$ 42"));
    assertEquals("jam_42", StringUtils.toLowerScored("jaM &&$ 42 !!!!"));
    assertEquals("jmmy", StringUtils.toLowerScored("j#MMy", "neuf"));
    assertEquals("neuf", StringUtils.toLowerScored(null, "neuf"));
    assertEquals("neuf", StringUtils.toLowerScored("%&(#", "neuf"));
    assertEquals("hammy_jammy_bunbuns", StringUtils.toLowerScored("HAMMY $%& jaMMy bun%buns"));
    assertEquals("p", StringUtils.toLowerScored("%&(#p"));
    assertEquals("", StringUtils.toLowerScored("%&(#"));
  }

  @Test
  public void toLowerSlug() {
    assertEquals("h4mmyjammy", StringUtils.toLowerSlug("H4MMY jaMMy"));
    assertEquals("jammy", StringUtils.toLowerSlug("jaMMy"));
    assertEquals("jmmy", StringUtils.toLowerSlug("j#MMy", "neuf"));
    assertEquals("neuf", StringUtils.toLowerSlug(null, "neuf"));
    assertEquals("neuf", StringUtils.toLowerSlug("%&(#", "neuf"));
    assertEquals("p", StringUtils.toLowerSlug("%&(#p"));
    assertEquals("", StringUtils.toLowerSlug("%&(#"));
  }

  @Test
  public void toNote() {
    assertEquals("C# major", StringUtils.toNote("   C# m___ajor "));
  }

  @Test
  public void toNumeric() {
    assertEquals("2", StringUtils.toNumeric("Pajamas!!2!!!!"));
    assertEquals("17", StringUtils.toNumeric("17 Pajamas?"));
    assertEquals("5", StringUtils.toNumeric("  P#$ aj#$@a   @#$$$$ma         s5"));
    assertEquals("25", StringUtils.toNumeric("P_+_+_+_+_(@(#%&!&&&@&%!@)_$*(!_)@()_#()(((a j a m a s 2    5"));
    assertEquals("2", StringUtils.toNumeric("Paja2mas"));
  }

  @Test
  public void toPlural() {
    assertEquals("i", StringUtils.toPlural("i"));
    assertEquals("basses", StringUtils.toPlural("bass"));
    assertEquals("bases", StringUtils.toPlural("base"));
    assertEquals("flasks", StringUtils.toPlural("flask"));
    assertEquals("kitties", StringUtils.toPlural("kitty"));
    assertEquals("cats", StringUtils.toPlural("cat"));
    assertEquals("superwidgets", StringUtils.toPlural("superwidget"));
    assertEquals("project-users", StringUtils.toPlural("project-user"));
    assertEquals("projects", StringUtils.toPlural("project"));
    assertEquals("projects", StringUtils.toPlural("projects"));
  }

  @Test
  public void toSingular() {
    assertEquals("i", StringUtils.toSingular("i"));
    assertEquals("base", StringUtils.toSingular("bases"));
    assertEquals("flask", StringUtils.toSingular("flasks"));
    assertEquals("kitty", StringUtils.toSingular("kitties"));
    assertEquals("cat", StringUtils.toSingular("cats"));
    assertEquals("superwidget", StringUtils.toSingular("superwidgets"));
    assertEquals("project-user", StringUtils.toSingular("project-users"));
    assertEquals("project", StringUtils.toSingular("projects"));
  }

  @Test
  public void toProper() {
    assertEquals("Jammy biscuit", StringUtils.toProper("jammy biscuit"));
    assertEquals("Jammy", StringUtils.toProper("jammy"));
    assertEquals("J#mmy", StringUtils.toProper("j#mmy"));
    assertEquals("%&(#", StringUtils.toProper("%&(#"));
  }

  @Test
  public void toProperSlug() {
    assertEquals("Jammybiscuit", StringUtils.toProperSlug("jammy biscuit"));
    assertEquals("Jammy", StringUtils.toProperSlug("jammy"));
    assertEquals("Jmmy", StringUtils.toProperSlug("j#mmy", "neuf"));
    assertEquals("Neuf", StringUtils.toProperSlug("%&(#", "neuf"));
    assertEquals("P", StringUtils.toProperSlug("%&(#p"));
    assertEquals("", StringUtils.toProperSlug("%&(#"));
    assertEquals("NextMain", StringUtils.toProperSlug("NextMain"));
  }

  @Test
  public void toScored() {
    assertEquals("", StringUtils.toScored(null));
    assertEquals("HAMMY_jaMMy", StringUtils.toScored("HAMMY jaMMy"));
    assertEquals("jaMMy", StringUtils.toScored("jaMMy"));
    assertEquals("jaM_42", StringUtils.toScored("jaM &&$ 42"));
    assertEquals("jaM_42", StringUtils.toScored("  ## jaM &&$ 42"));
    assertEquals("jaM_42", StringUtils.toScored("jaM &&$ 42 !!!!"));
    assertEquals("HAMMY_jaMMy_bunbuns", StringUtils.toScored("HAMMY $%& jaMMy bun%buns"));
    assertEquals("p", StringUtils.toScored("%&(#p"));
    assertEquals("", StringUtils.toScored("%&(#"));
  }

  @Test
  public void singleQuoted() {
    assertEquals("'stones'", StringUtils.singleQuoted("stones"));
  }

  @Test
  public void doubleQuoted() {
    assertEquals("\"stones\"", StringUtils.doubleQuoted("stones"));
  }

  @Test
  public void toSlug() {
    assertEquals("jim", StringUtils.toSlug("jim"));
    assertEquals("jim251", StringUtils.toSlug("jim-251"));
    assertEquals("jim251", StringUtils.toSlug("j i m - 2 5 1"));
    assertEquals("jm251", StringUtils.toSlug("j!$m%-^2%5*1"));
  }

  @Test
  public void toStrings() {
    // FUTURE test Text.toStrings()
  }

  @Test
  public void toUpperScored() {
    assertEquals("JAMMY_BUNS", StringUtils.toUpperScored("jaMMy b#!uns"));
    assertEquals("JAMMY_BUNS", StringUtils.toUpperScored("  jaMMy    b#!uns   "));
    assertEquals("JAMMY", StringUtils.toUpperScored("jaMMy"));
    assertEquals("JMMY", StringUtils.toUpperScored("j#MMy", "neuf"));
    assertEquals("NEUF", StringUtils.toUpperScored(null, "neuf"));
    assertEquals("NEUF", StringUtils.toUpperScored("%&(#", "neuf"));
    assertEquals("P", StringUtils.toUpperScored("%&(#p"));
    assertEquals("", StringUtils.toUpperScored("%&(#"));
  }

  @Test
  public void toMeme() {
    assertEquals("JAMMYB!NS", StringUtils.toMeme("jaMMy b#!ns"));
    assertEquals("JAMMY", StringUtils.toMeme("jaMMy"));
    assertEquals("JMMY", StringUtils.toMeme("j#MMy", "neuf"));
    assertEquals("NEUF", StringUtils.toMeme(null, "neuf"));
    assertEquals("NEUF", StringUtils.toMeme("%&(#", "neuf"));
    assertEquals("P", StringUtils.toMeme("%&(#p"));
    assertEquals("", StringUtils.toMeme("%&(#"));
    assertEquals("$UNIQUE", StringUtils.toMeme("$UNIQUE"));
  }

  @Test
  public void toEvent() {
    assertEquals("JAMMYBNS", StringUtils.toEvent("jaMMy b#!ns"));
    assertEquals("JAMMY", StringUtils.toEvent("jaMMy"));
    assertEquals("JAMMY", StringUtils.toEvent("jaMMy55"));
    assertEquals("P", StringUtils.toEvent("%&(#p"));
    assertEquals("", StringUtils.toEvent("%&(#"));
  }

  @Test
  public void formatConfig() {
    String result = StringUtils.format(ConfigFactory.empty()
      .withValue("choiceMuteProbabilityInstrumentTypeNoise", ConfigValueFactory.fromAnyRef(35))
      .withValue("deltaArcBeatLayersIncoming", ConfigValueFactory.fromAnyRef(4)));

    assertTrue(result.contains("choiceMuteProbabilityInstrumentTypeNoise=35"));
    assertTrue(result.contains("deltaArcBeatLayersIncoming=4"));
  }

  @Test
  public void parseEnvironmentVariableKeyPairs() {
    assertEquals(Map.of(
        "YO", "MA",
        "HELLA", "BEANS"
      ),
      StringUtils.parseEnvironmentVariableKeyPairs("YO=MA" + System.lineSeparator() + "HELLA=BEANS"));
  }

  @Test
  public void splitLines() {
    assertArrayEquals(
      new String[]{"One", "Two", "Three"},
      StringUtils.splitLines("One" + System.lineSeparator() + "Two" + System.lineSeparator() + "Three"));
  }

  @Test
  public void incrementIntegerSuffix() {
    assertEquals("b5", StringUtils.incrementIntegerSuffix("b4"));
    assertEquals("b2", StringUtils.incrementIntegerSuffix("b"));
    assertEquals("coolair125", StringUtils.incrementIntegerSuffix("coolair124"));
    assertEquals("2", StringUtils.incrementIntegerSuffix(""));
    assertEquals("2", StringUtils.incrementIntegerSuffix(null));
  }

  @Test
  public void stripExtraSpaces() {
    assertEquals("just a shadow", StringUtils.stripExtraSpaces(" just   a     shadow   "));
  }

  @Test
  public void match() {
    Pattern abc = Pattern.compile("^([ABC]+)$");

    assertTrue(StringUtils.match(abc, "123").isEmpty());
    assertEquals("A", StringUtils.match(abc, "A").orElseThrow());
  }

  @Test
  public void reverseLines() {
    assertEquals("Three\nTwo\nOne", StringUtils.reverseLines("One\nTwo\nThree"));
  }

  @Test
  public void beginsWith() {
    assertTrue(StringUtils.beginsWith("yellow", "yellow"));
    assertTrue(StringUtils.beginsWith("yellow", "yel"));
    assertTrue(StringUtils.beginsWith("yellow", "y"));
    assertFalse(StringUtils.beginsWith("yellow", "ell"));
    assertFalse(StringUtils.beginsWith("yel", "yellow"));
    assertFalse(StringUtils.beginsWith("yellow", "e"));
  }

  @Test
  public void toLowerHyphenatedSlug() {
    assertEquals("jammy-buns", StringUtils.toLowerHyphenatedSlug("jaMMy b#!uns"));
    assertEquals("jammy", StringUtils.toLowerHyphenatedSlug("jaMMy"));
  }

  @Test
  public void snakeToUpperCamelCase() {
    assertEquals("One", StringUtils.snakeToUpperCamelCase("one"));
    assertEquals("OneTwo", StringUtils.snakeToUpperCamelCase("one_two"));
    assertEquals("OneTwoThree", StringUtils.snakeToUpperCamelCase("one_two_three"));
  }

  @Test
  public void snakeToLowerCamelCase() {
    assertEquals("one", StringUtils.snakeToLowerCamelCase("one"));
    assertEquals("oneTwo", StringUtils.snakeToLowerCamelCase("one_two"));
    assertEquals("oneTwoThree", StringUtils.snakeToLowerCamelCase("one_two_three"));
  }

  @Test
  public void kebabToUpperCamelCase() {
    assertEquals("One", StringUtils.kebabToUpperCamelCase("one"));
    assertEquals("OneTwo", StringUtils.kebabToUpperCamelCase("one-two"));
    assertEquals("OneTwoThree", StringUtils.kebabToUpperCamelCase("one-two-three"));
  }

  @Test
  public void kebabToLowerCamelCase() {
    assertEquals("one", StringUtils.kebabToLowerCamelCase("one"));
    assertEquals("oneTwo", StringUtils.kebabToLowerCamelCase("one-two"));
    assertEquals("oneTwoThree", StringUtils.kebabToLowerCamelCase("one-two-three"));
  }

  @Test
  public void camelToSnakeCase() {
    assertEquals("one", StringUtils.camelToSnakeCase("One"));
    assertEquals("one_two", StringUtils.camelToSnakeCase("OneTwo"));
    assertEquals("one_two_three", StringUtils.camelToSnakeCase("OneTwoThree"));
    assertEquals("one", StringUtils.camelToSnakeCase("one"));
    assertEquals("one_two", StringUtils.camelToSnakeCase("oneTwo"));
    assertEquals("one_two_three", StringUtils.camelToSnakeCase("oneTwoThree"));
  }

  @Test
  public void camelToKebabCase() {
    assertEquals("one", StringUtils.camelToKebabCase("One"));
    assertEquals("one-two", StringUtils.camelToKebabCase("OneTwo"));
    assertEquals("one-two-three", StringUtils.camelToKebabCase("OneTwoThree"));
    assertEquals("one", StringUtils.camelToKebabCase("one"));
    assertEquals("one-two", StringUtils.camelToKebabCase("oneTwo"));
    assertEquals("one-two-three", StringUtils.camelToKebabCase("oneTwoThree"));
  }

  @Test
  public void splitCamelCase() {
    assertArrayEquals(new String[]{"one"}, StringUtils.splitCamelCase("one"));
    assertArrayEquals(new String[]{"one", "Two"}, StringUtils.splitCamelCase("oneTwo"));
    assertArrayEquals(new String[]{"one", "Two", "Three"}, StringUtils.splitCamelCase("oneTwoThree"));
    assertArrayEquals(new String[]{"One"}, StringUtils.splitCamelCase("One"));
    assertArrayEquals(new String[]{"One", "Two"}, StringUtils.splitCamelCase("OneTwo"));
    assertArrayEquals(new String[]{"One", "Two", "Three"}, StringUtils.splitCamelCase("OneTwoThree"));
  }

  @Test
  public void firstLetterToUpperCase() {
    assertEquals("One", StringUtils.firstLetterToUpperCase("one"));
    assertEquals("OneTwo", StringUtils.firstLetterToUpperCase("oneTwo"));
    assertEquals("OneTwoThree", StringUtils.firstLetterToUpperCase("oneTwoThree"));
  }

  @Test
  public void firstLetterToLowerCase() {
    assertEquals("one", StringUtils.firstLetterToLowerCase("One"));
    assertEquals("oneTwo", StringUtils.firstLetterToLowerCase("OneTwo"));
    assertEquals("oneTwoThree", StringUtils.firstLetterToLowerCase("OneTwoThree"));
  }

  @Test
  public void stringOrEmpty() {
    assertEquals("", StringUtils.stringOrEmpty(null));
    assertEquals("4797a799-827f-4543-9e0e-65a6cb6b382f",
      StringUtils.stringOrEmpty(UUID.fromString("4797a799-827f-4543-9e0e-65a6cb6b382f")));
  }

  @Test
  public void getLastCharacters() {
    assertEquals("123", StringUtils.getLastCharacters("123", 3));
    assertEquals("house", StringUtils.getLastCharacters("greenhouse", 5));
  }

  @Test
  void toProperCsvAnd() {
    assertEquals("One", StringUtils.toProperCsvAnd(List.of("One")));
    assertEquals("One and Two", StringUtils.toProperCsvAnd(List.of("One", "Two")));
    assertEquals("One, Two, and Three", StringUtils.toProperCsvAnd(List.of("One", "Two", "Three")));
  }

  @Test
  void toProperCsvOr() {
    assertEquals("One", StringUtils.toProperCsvOr(List.of("One")));
    assertEquals("One or Two", StringUtils.toProperCsvOr(List.of("One", "Two")));
    assertEquals("One, Two, or Three", StringUtils.toProperCsvOr(List.of("One", "Two", "Three")));
  }

  @Test
  void toProperCsv() {
    assertEquals("One", StringUtils.toProperCsv(List.of("One"), "or maybe"));
    assertEquals("One or maybe Two", StringUtils.toProperCsv(List.of("One", "Two"), "or maybe"));
    assertEquals("One, Two, or maybe Three", StringUtils.toProperCsv(List.of("One", "Two", "Three"), "or maybe"));
  }

  @Test
  void isNullOrEmpty() {
    assertTrue(StringUtils.isNullOrEmpty(null));
    assertTrue(StringUtils.isNullOrEmpty(""));
    assertFalse(StringUtils.isNullOrEmpty(" "));
    assertFalse(StringUtils.isNullOrEmpty("a"));
  }

  @Test
  void isNotNullOrEmpty() {
    assertFalse(StringUtils.isNotNullOrEmpty(null));
    assertFalse(StringUtils.isNotNullOrEmpty(""));
    assertTrue(StringUtils.isNotNullOrEmpty(" "));
    assertTrue(StringUtils.isNotNullOrEmpty("a"));
  }

  @Test
  void format() {
    var input = ConfigFactory.parseString("""
      barBeats = 4
      cutoffMinimumBars = 2
      doPatternRestartOnChord = false
          """);

    assertEquals("barBeats=4\ncutoffMinimumBars=2\ndoPatternRestartOnChord=false", StringUtils.format(input));
  }

  @Test
  void toShipKey() {
    assertEquals("one_two", StringUtils.toShipKey("One Two"));
  }

  @Test
  void percentage() {
    assertEquals("53%", StringUtils.percentage(0.53f));
  }

  @Test
  void formatFloatMinimumDigits() {
    assertEquals("0.53", StringUtils.formatMinimumDigits(0.53));
    assertEquals("0.0", StringUtils.formatMinimumDigits(0.0));
    assertEquals("0.00001", StringUtils.formatMinimumDigits(0.00001));
  }

  @Test
  void find() {
    Pattern abc = Pattern.compile("^([ABC]+)$");

    assertTrue(StringUtils.find(abc, "A"));
    assertTrue(StringUtils.find(abc, "B"));
    assertFalse(StringUtils.find(abc, "123"));
    assertFalse(StringUtils.find(abc, "AD"));
  }

  @Test
  void zeroPadded() {
    assertEquals("00000053", StringUtils.zeroPadded(53, 8));
    assertEquals("00000174", StringUtils.zeroPadded(174, 8));
    assertEquals("00002385", StringUtils.zeroPadded(2385, 8));
  }

  @Test
  void stringOrDefault() {
    assertEquals("default", StringUtils.stringOrDefault(null, "default"));
    assertEquals("actual", StringUtils.stringOrDefault("actual", "default"));
  }
}
