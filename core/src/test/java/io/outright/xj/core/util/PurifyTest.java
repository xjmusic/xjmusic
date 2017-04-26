// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Objects;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class PurifyTest extends Mockito {
  @Test
  public void slug() throws Exception {
    assertEquals("jim", Purify.Slug("jim"));
    assertEquals("jim", Purify.Slug("jim-251"));
    assertEquals("jim", Purify.Slug("j i m - 2 5 1"));
    assertEquals("jim", Purify.Slug("j!i$m%-^2%5*1"));
  }

  @Test
  public void properSlug() throws Exception {
    assertEquals("Jammybiscuit", Purify.ProperSlug("jaMMy bISCUIT"));
    assertEquals("Jammy", Purify.ProperSlug("jaMMy"));
    assertEquals("Jmmy", Purify.ProperSlug("j#MMy", "neuf"));
    assertEquals("Neuf", Purify.ProperSlug("%&(#", "neuf"));
    assertEquals("P", Purify.ProperSlug("%&(#p"));
    assertEquals("", Purify.ProperSlug("%&(#"));
  }

  @Test
  public void lowerSlug() throws Exception {
    assertEquals("hammyjammy", Purify.LowerSlug("H4AMMY jaMMy"));
    assertEquals("jammy", Purify.LowerSlug("jaMMy"));
    assertEquals("jmmy", Purify.LowerSlug("j#MMy", "neuf"));
    assertEquals("neuf", Purify.LowerSlug(null, "neuf"));
    assertEquals("neuf", Purify.LowerSlug("%&(#", "neuf"));
    assertEquals("p", Purify.LowerSlug("%&(#p"));
    assertEquals("", Purify.LowerSlug("%&(#"));
  }

  @Test
  public void upperSlug() throws Exception {
    assertEquals("JAMMYBUNS", Purify.UpperSlug("jaMMy b#!uns"));
    assertEquals("JAMMY", Purify.UpperSlug("jaMMy"));
    assertEquals("JMMY", Purify.UpperSlug("j#MMy", "neuf"));
    assertEquals("NEUF", Purify.UpperSlug(null, "neuf"));
    assertEquals("NEUF", Purify.UpperSlug("%&(#", "neuf"));
    assertEquals("P", Purify.UpperSlug("%&(#p"));
    assertEquals("", Purify.UpperSlug("%&(#"));
  }

  @Test
  public void lowerScored() throws Exception {
    assertEquals("hammy_jammy", Purify.LowerScored("H4AMMY jaMMy"));
    assertEquals("jammy", Purify.LowerScored("jaMMy"));
    assertEquals("jmmy", Purify.LowerScored("j#MMy", "neuf"));
    assertEquals("neuf", Purify.LowerScored(null, "neuf"));
    assertEquals("neuf", Purify.LowerScored("%&(#", "neuf"));
    assertEquals("p", Purify.LowerScored("%&(#p"));
    assertEquals("", Purify.LowerScored("%&(#"));
  }

  @Test
  public void upperScored() throws Exception {
    assertEquals("JAMMY_BUNS", Purify.UpperScored("jaMMy b#!uns"));
    assertEquals("JAMMY_BUNS", Purify.UpperScored("  jaMMy    b#!uns   "));
    assertEquals("JAMMY", Purify.UpperScored("jaMMy"));
    assertEquals("JMMY", Purify.UpperScored("j#MMy", "neuf"));
    assertEquals("NEUF", Purify.UpperScored(null, "neuf"));
    assertEquals("NEUF", Purify.UpperScored("%&(#", "neuf"));
    assertEquals("P", Purify.UpperScored("%&(#p"));
    assertEquals("", Purify.UpperScored("%&(#"));
  }

  @Test
  public void note() throws Exception {
    assertEquals("C# major", Purify.Note("   C# m___ajor "));
  }

}
