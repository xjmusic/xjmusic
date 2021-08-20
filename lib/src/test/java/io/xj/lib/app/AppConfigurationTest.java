// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.app;

import com.google.inject.Inject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AppConfigurationTest {

  /**
   Attempts to create an AppConfiguration via its private constructor and
   demonstrates that an exception is thrown.
   */
  @Test(expected = IllegalStateException.class)
  public void constructorMustFail() {
    @SuppressWarnings({"InstantiationOfUtilityClass", "unused"}) AppConfiguration config = new AppConfiguration();
  }

  /**
   Check the default configuration
   */
  @Test
  public void getDefault() {
    assertEquals(3001, AppConfiguration.getDefault().getInt("app.port"));
  }

  /**
   Interface for testing app injection
   */
  public interface Widget {
    String getName();

    Widget setName(String name);
  }

  /**
   Implementation for testing app injection
   */
  public static class WidgetImpl implements Widget {

    @Inject
    public WidgetImpl() {
      this.name = "";
    }

    private String name;

    @Override
    public String getName() {
      return name;
    }

    @Override
    public Widget setName(String name) {
      this.name = name;
      return this;
    }
  }

}
