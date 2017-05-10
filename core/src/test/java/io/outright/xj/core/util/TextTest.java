// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class TextTest extends Mockito {
  @Test
  public void slug() throws Exception {
    assertEquals("jim", Text.Slug("jim"));
    assertEquals("jim", Text.Slug("jim-251"));
    assertEquals("jim", Text.Slug("j i m - 2 5 1"));
    assertEquals("jim", Text.Slug("j!i$m%-^2%5*1"));
  }

  @Test
  public void properSlug() throws Exception {
    assertEquals("Jammybiscuit", Text.ProperSlug("jaMMy bISCUIT"));
    assertEquals("Jammy", Text.ProperSlug("jaMMy"));
    assertEquals("Jmmy", Text.ProperSlug("j#MMy", "neuf"));
    assertEquals("Neuf", Text.ProperSlug("%&(#", "neuf"));
    assertEquals("P", Text.ProperSlug("%&(#p"));
    assertEquals("", Text.ProperSlug("%&(#"));
  }

  @Test
  public void lowerSlug() throws Exception {
    assertEquals("hammyjammy", Text.LowerSlug("H4AMMY jaMMy"));
    assertEquals("jammy", Text.LowerSlug("jaMMy"));
    assertEquals("jmmy", Text.LowerSlug("j#MMy", "neuf"));
    assertEquals("neuf", Text.LowerSlug(null, "neuf"));
    assertEquals("neuf", Text.LowerSlug("%&(#", "neuf"));
    assertEquals("p", Text.LowerSlug("%&(#p"));
    assertEquals("", Text.LowerSlug("%&(#"));
  }

  @Test
  public void upperSlug() throws Exception {
    assertEquals("JAMMYBUNS", Text.UpperSlug("jaMMy b#!uns"));
    assertEquals("JAMMY", Text.UpperSlug("jaMMy"));
    assertEquals("JMMY", Text.UpperSlug("j#MMy", "neuf"));
    assertEquals("NEUF", Text.UpperSlug(null, "neuf"));
    assertEquals("NEUF", Text.UpperSlug("%&(#", "neuf"));
    assertEquals("P", Text.UpperSlug("%&(#p"));
    assertEquals("", Text.UpperSlug("%&(#"));
  }

  @Test
  public void lowerScored() throws Exception {
    assertEquals("hammy_jammy", Text.LowerScored("H4AMMY jaMMy"));
    assertEquals("jammy", Text.LowerScored("jaMMy"));
    assertEquals("jmmy", Text.LowerScored("j#MMy", "neuf"));
    assertEquals("neuf", Text.LowerScored(null, "neuf"));
    assertEquals("neuf", Text.LowerScored("%&(#", "neuf"));
    assertEquals("p", Text.LowerScored("%&(#p"));
    assertEquals("", Text.LowerScored("%&(#"));
  }

  @Test
  public void upperScored() throws Exception {
    assertEquals("JAMMY_BUNS", Text.UpperScored("jaMMy b#!uns"));
    assertEquals("JAMMY_BUNS", Text.UpperScored("  jaMMy    b#!uns   "));
    assertEquals("JAMMY", Text.UpperScored("jaMMy"));
    assertEquals("JMMY", Text.UpperScored("j#MMy", "neuf"));
    assertEquals("NEUF", Text.UpperScored(null, "neuf"));
    assertEquals("NEUF", Text.UpperScored("%&(#", "neuf"));
    assertEquals("P", Text.UpperScored("%&(#p"));
    assertEquals("", Text.UpperScored("%&(#"));
  }

  @Test
  public void note() throws Exception {
    assertEquals("C# major", Text.Note("   C# m___ajor "));
  }

  @Test
  public void doc() throws Exception {
    assertEquals("chain-info.md", Text.DocKey("  cha$$in-in fo.md"));
    assertEquals(".", Text.DocKey("../../../../"));
  }

  @Test
  public void docNameForKey() throws Exception {
    assertEquals("Chain Info", Text.DocNameForKey("chain-info.md"));
  }

}
