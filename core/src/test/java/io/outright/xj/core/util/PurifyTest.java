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
    assertEquals("Jammy", Purify.ProperSlug("jaMMy"));
    assertEquals("Jmmy", Purify.ProperSlug("j#MMy", "neuf"));
    assertEquals("Neuf", Purify.ProperSlug("%&(#", "neuf"));
    assertEquals("P", Purify.ProperSlug("%&(#p"));
    assertEquals("", Purify.ProperSlug("%&(#"));
  }

  @Test
  public void lowerSlug() throws Exception {
    assertEquals("jammy", Purify.LowerSlug("jaMMy"));
    assertEquals("jmmy", Purify.LowerSlug("j#MMy", "neuf"));
    assertEquals("neuf", Purify.LowerSlug(null, "neuf"));
    assertEquals("neuf", Purify.LowerSlug("%&(#", "neuf"));
    assertEquals("p", Purify.LowerSlug("%&(#p"));
    assertEquals("", Purify.LowerSlug("%&(#"));
  }

  @Test
  public void upperSlug() throws Exception {
    assertEquals("JAMMY", Purify.UpperSlug("jaMMy"));
    assertEquals("JMMY", Purify.UpperSlug("j#MMy", "neuf"));
    assertEquals("NEUF", Purify.UpperSlug(null, "neuf"));
    assertEquals("NEUF", Purify.UpperSlug("%&(#", "neuf"));
    assertEquals("P", Purify.UpperSlug("%&(#p"));
    assertEquals("", Purify.UpperSlug("%&(#"));
  }

  @Test
  public void note() throws Exception {
    assertEquals("C# major", Purify.Note("   C# m___ajor "));
  }

}
