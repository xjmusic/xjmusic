// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Objects;

@RunWith(MockitoJUnitRunner.class)
public class PurifyTest extends Mockito {

  @Test
  public void testToString() {
    assert Objects.equals(Purify.Slug("jim"), "jim");
    assert Objects.equals(Purify.Slug("jim-251"), "jim251");
    assert Objects.equals(Purify.Slug("j i m - 2 5 1"), "jim251");
    assert Objects.equals(Purify.Slug("j!i$m%-^2%5*1"), "jim251");
  }

  @Test
  public void testProperSlug() {
    assert Objects.equals(Purify.ProperSlug("jaMMy"), "Jammy");
    assert Objects.equals(Purify.ProperSlug("j#MMy", "neuf"), "Jmmy");
    assert Objects.equals(Purify.ProperSlug("%&(#", "neuf"), "Neuf");
    assert Objects.equals(Purify.ProperSlug("%&(#p"), "P");
    assert Objects.equals(Purify.ProperSlug("%&(#"), "");
  }
}
