// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.util;

import com.google.common.collect.ImmutableMap;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import org.junit.Test;

import java.time.Instant;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class TextTest {

  @Test
  public void formatMultiline() {
    assertEquals("Line One\nLine Two\n", Text.formatMultiline(new String[]{"Line One", "Line Two"}));
  }

  @Test
  public void orEmptyQuotes() {
    assertEquals("\"\"", Text.orEmptyQuotes(""));
    assertEquals("\"\"", Text.orEmptyQuotes(null));
    assertEquals("tubs", Text.orEmptyQuotes("tubs"));
  }

  @Test
  public void formatStackTrace() {
    assertEquals("", Text.formatStackTrace(null));
  }

  @Test
  public void getSimpleName() {
    assertEquals("Instant", Text.getSimpleName(Instant.now()));
    assertEquals("Instant", Text.getSimpleName(Instant.class));
  }

  @Test
  public void toAlphabetical() {
    assertEquals("Pajamas", Text.toAlphabetical("Pajamas"));
    assertEquals("Pajamas", Text.toAlphabetical("1P34aj2a3ma321s"));
    assertEquals("Pajamas", Text.toAlphabetical("  P#$ aj#$@a   @#$$$$ma         s"));
    assertEquals("Pajamas", Text.toAlphabetical("P_+_+_+_+_(@(#%&!&&&@&%!@)_$*(!_)@()_#()(((a j a m a s "));
    assertEquals("Pajamas", Text.toAlphabetical("Pajamas"));
  }

  @Test
  public void toAlphanumeric() {
    assertEquals("Pajamas", Text.toAlphanumeric("Pajamas!!!!!!"));
    assertEquals("17Pajamas", Text.toAlphanumeric("17 Pajamas?"));
    assertEquals("Pajamas5", Text.toAlphanumeric("  P#$ aj#$@a   @#$$$$ma         s5"));
    assertEquals("Pajamas25", Text.toAlphanumeric("P_+_+_+_+_(@(#%&!&&&@&%!@)_$*(!_)@()_#()(((a j a m a s 2    5"));
    assertEquals("Pajamas", Text.toAlphanumeric("Pajamas"));
  }

  @Test
  public void toAlphanumericHyphenated() {
    assertEquals("I-Love-My-Pajamas", Text.toAlphanumericHyphenated("I Love My Pajamas"));
    assertEquals("I--Love--My--Pajamas-", Text.toAlphanumericHyphenated("I! Love! My! Pajamas!"));
  }

  @Test
  public void toAlphaSlug() {
    assertEquals("THIS_THING", Text.toAlphaSlug("--- ---  T@@@HIS_@THIN!4 G"));
  }

  @Test
  public void toLowerScored() {
    assertEquals("hammy_jammy", Text.toLowerScored("HAMMY jaMMy"));
    assertEquals("jammy", Text.toLowerScored("jaMMy"));
    assertEquals("jam_42", Text.toLowerScored("jaM &&$ 42"));
    assertEquals("jam_42", Text.toLowerScored("  ## jaM &&$ 42"));
    assertEquals("jam_42", Text.toLowerScored("jaM &&$ 42 !!!!"));
    assertEquals("jmmy", Text.toLowerScored("j#MMy", "neuf"));
    assertEquals("neuf", Text.toLowerScored(null, "neuf"));
    assertEquals("neuf", Text.toLowerScored("%&(#", "neuf"));
    assertEquals("hammy_jammy_bunbuns", Text.toLowerScored("HAMMY $%& jaMMy bun%buns"));
    assertEquals("p", Text.toLowerScored("%&(#p"));
    assertEquals("", Text.toLowerScored("%&(#"));
  }

  @Test
  public void toLowerSlug() {
    assertEquals("h4mmyjammy", Text.toLowerSlug("H4MMY jaMMy"));
    assertEquals("jammy", Text.toLowerSlug("jaMMy"));
    assertEquals("jmmy", Text.toLowerSlug("j#MMy", "neuf"));
    assertEquals("neuf", Text.toLowerSlug(null, "neuf"));
    assertEquals("neuf", Text.toLowerSlug("%&(#", "neuf"));
    assertEquals("p", Text.toLowerSlug("%&(#p"));
    assertEquals("", Text.toLowerSlug("%&(#"));
  }

  @Test
  public void toNote() {
    assertEquals("C# major", Text.toNote("   C# m___ajor "));
  }

  @Test
  public void toNumeric() {
    assertEquals("2", Text.toNumeric("Pajamas!!2!!!!"));
    assertEquals("17", Text.toNumeric("17 Pajamas?"));
    assertEquals("5", Text.toNumeric("  P#$ aj#$@a   @#$$$$ma         s5"));
    assertEquals("25", Text.toNumeric("P_+_+_+_+_(@(#%&!&&&@&%!@)_$*(!_)@()_#()(((a j a m a s 2    5"));
    assertEquals("2", Text.toNumeric("Paja2mas"));
  }

  @Test
  public void toPlural() {
    assertEquals("i", Text.toPlural("i"));
    assertEquals("basses", Text.toPlural("bass"));
    assertEquals("bases", Text.toPlural("base"));
    assertEquals("flasks", Text.toPlural("flask"));
    assertEquals("kitties", Text.toPlural("kitty"));
    assertEquals("cats", Text.toPlural("cat"));
    assertEquals("superwidgets", Text.toPlural("superwidget"));
    assertEquals("account-users", Text.toPlural("account-user"));
    assertEquals("accounts", Text.toPlural("account"));
    assertEquals("accounts", Text.toPlural("accounts"));
  }

  @Test
  public void toSingular() {
    assertEquals("i", Text.toSingular("i"));
    assertEquals("base", Text.toSingular("bases"));
    assertEquals("flask", Text.toSingular("flasks"));
    assertEquals("kitty", Text.toSingular("kitties"));
    assertEquals("cat", Text.toSingular("cats"));
    assertEquals("superwidget", Text.toSingular("superwidgets"));
    assertEquals("account-user", Text.toSingular("account-users"));
    assertEquals("account", Text.toSingular("accounts"));
  }

  @Test
  public void toProper() {
    assertEquals("Jammy biscuit", Text.toProper("jammy biscuit"));
    assertEquals("Jammy", Text.toProper("jammy"));
    assertEquals("J#mmy", Text.toProper("j#mmy"));
    assertEquals("%&(#", Text.toProper("%&(#"));
  }

  @Test
  public void toProperSlug() {
    assertEquals("Jammybiscuit", Text.toProperSlug("jammy biscuit"));
    assertEquals("Jammy", Text.toProperSlug("jammy"));
    assertEquals("Jmmy", Text.toProperSlug("j#mmy", "neuf"));
    assertEquals("Neuf", Text.toProperSlug("%&(#", "neuf"));
    assertEquals("P", Text.toProperSlug("%&(#p"));
    assertEquals("", Text.toProperSlug("%&(#"));
    assertEquals("NextMain", Text.toProperSlug("NextMain"));
  }

  @Test
  public void toScored() {
    assertEquals("", Text.toScored(null));
    assertEquals("HAMMY_jaMMy", Text.toScored("HAMMY jaMMy"));
    assertEquals("jaMMy", Text.toScored("jaMMy"));
    assertEquals("jaM_42", Text.toScored("jaM &&$ 42"));
    assertEquals("jaM_42", Text.toScored("  ## jaM &&$ 42"));
    assertEquals("jaM_42", Text.toScored("jaM &&$ 42 !!!!"));
    assertEquals("HAMMY_jaMMy_bunbuns", Text.toScored("HAMMY $%& jaMMy bun%buns"));
    assertEquals("p", Text.toScored("%&(#p"));
    assertEquals("", Text.toScored("%&(#"));
  }

  @Test
  public void singleQuoted() {
    assertEquals("'stones'", Text.singleQuoted("stones"));
  }

  @Test
  public void doubleQuoted() {
    assertEquals("\"stones\"", Text.doubleQuoted("stones"));
  }

  @Test
  public void toSlug() {
    assertEquals("jim", Text.toSlug("jim"));
    assertEquals("jim251", Text.toSlug("jim-251"));
    assertEquals("jim251", Text.toSlug("j i m - 2 5 1"));
    assertEquals("jm251", Text.toSlug("j!$m%-^2%5*1"));
  }

  @Test
  public void toStrings() {
    // FUTURE test Text.toStrings()
  }

  @Test
  public void toUpperScored() {
    assertEquals("JAMMY_BUNS", Text.toUpperScored("jaMMy b#!uns"));
    assertEquals("JAMMY_BUNS", Text.toUpperScored("  jaMMy    b#!uns   "));
    assertEquals("JAMMY", Text.toUpperScored("jaMMy"));
    assertEquals("JMMY", Text.toUpperScored("j#MMy", "neuf"));
    assertEquals("NEUF", Text.toUpperScored(null, "neuf"));
    assertEquals("NEUF", Text.toUpperScored("%&(#", "neuf"));
    assertEquals("P", Text.toUpperScored("%&(#p"));
    assertEquals("", Text.toUpperScored("%&(#"));
  }

  @Test
  public void toMeme() {
    assertEquals("JAMMYB!NS", Text.toMeme("jaMMy b#!ns"));
    assertEquals("JAMMY", Text.toMeme("jaMMy"));
    assertEquals("JMMY", Text.toMeme("j#MMy", "neuf"));
    assertEquals("NEUF", Text.toMeme(null, "neuf"));
    assertEquals("NEUF", Text.toMeme("%&(#", "neuf"));
    assertEquals("P", Text.toMeme("%&(#p"));
    assertEquals("", Text.toMeme("%&(#"));
    assertEquals("$UNIQUE", Text.toMeme("$UNIQUE"));
  }

  @Test
  public void toEvent() {
    assertEquals("JAMMYBNS", Text.toEvent("jaMMy b#!ns"));
    assertEquals("JAMMY", Text.toEvent("jaMMy"));
    assertEquals("JAMMY", Text.toEvent("jaMMy55"));
    assertEquals("P", Text.toEvent("%&(#p"));
    assertEquals("", Text.toEvent("%&(#"));
  }

  @Test
  public void formatConfig() {
    String result = Text.format(ConfigFactory.empty()
      .withValue("outputFrameRate", ConfigValueFactory.fromAnyRef(35))
      .withValue("outputChannels", ConfigValueFactory.fromAnyRef(4)));

    assertTrue(result.contains("outputChannels=4"));
    assertTrue(result.contains("outputFrameRate=35"));
  }

  @Test
  public void parseEnvironmentVariableKeyPairs() {
    assertEquals(ImmutableMap.of(
        "YO", "MA",
        "HELLA", "BEANS"
      ),
      Text.parseEnvironmentVariableKeyPairs("YO=MA\nHELLA=BEANS"));
  }

  @Test
  public void splitLines() {
    assertArrayEquals(
      new String[]{"One", "Two", "Three"},
      Text.splitLines("One\nTwo\nThree"));
  }

  @Test
  public void incrementIntegerSuffix() {
    assertEquals("b5", Text.incrementIntegerSuffix("b4"));
    assertEquals("b2", Text.incrementIntegerSuffix("b"));
    assertEquals("coolair125", Text.incrementIntegerSuffix("coolair124"));
    assertEquals("2", Text.incrementIntegerSuffix(""));
    assertEquals("2", Text.incrementIntegerSuffix(null));
  }

  @Test
  public void stripExtraSpaces() {
    assertEquals("just a shadow", Text.stripExtraSpaces(" just   a     shadow   "));
  }

  @Test
  public void match() {
    Pattern abc = Pattern.compile("^([ABC]+)$");

    assertTrue(Text.match(abc, "123").isEmpty());
    assertEquals("A", Text.match(abc, "A").orElseThrow());
  }

  @Test
  public void reverseLines() {
    assertEquals("Three\nTwo\nOne", Text.reverseLines("One\nTwo\nThree"));
  }

  @Test
  public void beginsWith() {
    assertTrue(Text.beginsWith("yellow", "yellow"));
    assertTrue(Text.beginsWith("yellow", "yel"));
    assertTrue(Text.beginsWith("yellow", "y"));
    assertFalse(Text.beginsWith("yellow", "ell"));
    assertFalse(Text.beginsWith("yel", "yellow"));
    assertFalse(Text.beginsWith("yellow", "e"));
  }
}
