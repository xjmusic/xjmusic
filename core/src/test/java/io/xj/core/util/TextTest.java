// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class TextTest extends Mockito {
  @Test
  public void toAlphabetical() throws Exception {
    assertEquals("Pajamas",Text.toAlphabetical("Pajamas"));
    assertEquals("Pajamas",Text.toAlphabetical("1P34aj2a3ma321s"));
    assertEquals("Pajamas",Text.toAlphabetical("  P#$ aj#$@a   @#$$$$ma         s"));
    assertEquals("Pajamas",Text.toAlphabetical("P_+_+_+_+_(@(#%&!&&&@&%!@)_$*(!_)@()_#()(((a j a m a s "));
    assertEquals("Pajamas",Text.toAlphabetical("Pajamas"));
  }

  @Test
  public void toSlug() throws Exception {
    assertEquals("jim", Text.toSlug("jim"));
    assertEquals("jim", Text.toSlug("jim-251"));
    assertEquals("jim", Text.toSlug("j i m - 2 5 1"));
    assertEquals("jim", Text.toSlug("j!i$m%-^2%5*1"));
  }

  @Test
  public void toProperSlug() throws Exception {
    assertEquals("Jammybiscuit", Text.toProperSlug("jaMMy bISCUIT"));
    assertEquals("Jammy", Text.toProperSlug("jaMMy"));
    assertEquals("Jmmy", Text.toProperSlug("j#MMy", "neuf"));
    assertEquals("Neuf", Text.toProperSlug("%&(#", "neuf"));
    assertEquals("P", Text.toProperSlug("%&(#p"));
    assertEquals("", Text.toProperSlug("%&(#"));
  }

  @Test
  public void toLowerSlug() throws Exception {
    assertEquals("hammyjammy", Text.toLowerSlug("H4AMMY jaMMy"));
    assertEquals("jammy", Text.toLowerSlug("jaMMy"));
    assertEquals("jmmy", Text.toLowerSlug("j#MMy", "neuf"));
    assertEquals("neuf", Text.toLowerSlug(null, "neuf"));
    assertEquals("neuf", Text.toLowerSlug("%&(#", "neuf"));
    assertEquals("p", Text.toLowerSlug("%&(#p"));
    assertEquals("", Text.toLowerSlug("%&(#"));
  }

  @Test
  public void toUpperSlug() throws Exception {
    assertEquals("JAMMYBUNS", Text.toUpperSlug("jaMMy b#!uns"));
    assertEquals("JAMMY", Text.toUpperSlug("jaMMy"));
    assertEquals("JMMY", Text.toUpperSlug("j#MMy", "neuf"));
    assertEquals("NEUF", Text.toUpperSlug(null, "neuf"));
    assertEquals("NEUF", Text.toUpperSlug("%&(#", "neuf"));
    assertEquals("P", Text.toUpperSlug("%&(#p"));
    assertEquals("", Text.toUpperSlug("%&(#"));
  }

  @Test
  public void toLowerScored() throws Exception {
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
  public void toUpperScored() throws Exception {
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
  public void toNote() throws Exception {
    assertEquals("C# major", Text.toNote("   C# m___ajor "));
  }

  @Test
  public void formatStackStrace_nullOutputsEmptyString() throws Exception {
    assertEquals("", Text.formatStackTrace(null));
  }

  @Test
  public void toProper() {
    assertEquals("Jammy biscuit", Text.toProper("jaMMy bISCUIT"));
    assertEquals("Jammy", Text.toProper("jaMMy"));
    assertEquals("J#mmy", Text.toProper("j#MMy"));
    assertEquals("%&(#", Text.toProper("%&(#"));
  }

  @Test
  public void toScored() {
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
  public void isInteger() {
    assertEquals(false, Text.isInteger("a"));
    assertEquals(false, Text.isInteger("125a"));
    assertEquals(true, Text.isInteger("377"));
    assertEquals(false, Text.isInteger("237.1"));
    assertEquals(true, Text.isInteger("100000045"));
    assertEquals(false, Text.isInteger(" 97"));
    assertEquals(false, Text.isInteger(" 27773"));
    assertEquals(true, Text.isInteger("32"));
  }

}
