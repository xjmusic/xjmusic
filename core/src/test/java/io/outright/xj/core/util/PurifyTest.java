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
  public void testToString() {
    assertEquals("jim", Purify.Slug("jim"));
    assertEquals("jim", Purify.Slug("jim-251"));
    assertEquals("jim", Purify.Slug("j i m - 2 5 1"));
    assertEquals("jim", Purify.Slug("j!i$m%-^2%5*1"));
  }

  @Test
  public void testProperSlug() {
    assertEquals("Jammy", Purify.ProperSlug("jaMMy"));
    assertEquals("Jmmy", Purify.ProperSlug("j#MMy", "neuf"));
    assertEquals("Neuf", Purify.ProperSlug("%&(#", "neuf"));
    assertEquals("P", Purify.ProperSlug("%&(#p"));
    assertEquals("", Purify.ProperSlug("%&(#"));
  }
}
