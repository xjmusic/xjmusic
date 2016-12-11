// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.util;

import org.junit.Test;

public class PurifyTest {

  @Test
  public void ToString() {
    assert Purify.Slug("jim").equals("jim");
    assert Purify.Slug("jim-251").equals("jim251");
    assert Purify.Slug("j i m - 2 5 1").equals("jim251");
    assert Purify.Slug("j!i$m%-^2%5*1").equals("jim251");
  }

  @Test
  public void Properslug() {
    assert Purify.Properslug("jaMMy").equals("Jammy");
    assert Purify.Properslug("j#MMy","neuf").equals("Jmmy");
    assert Purify.Properslug("%&(#","neuf").equals("Neuf");
    assert Purify.Properslug("%&(#p").equals("P");
    assert Purify.Properslug("%&(#").equals("");
  }
}
