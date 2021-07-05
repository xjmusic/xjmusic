// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.app;

import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

public class AppConfigurationTest {

  /**
   Attempts to create an AppConfiguration via its private constructor and
   demonstrates that an exception is thrown.
   */
  @Test(expected = IllegalStateException.class)
  @SuppressWarnings("InstantiationOfUtilityClass")
  public void constructorMustFail() {
    new AppConfiguration();
  }

  /**
   Test the injection of the configuration into an injector
   */
  @Test
  public void inject() throws AppException {
    var env = Environment.getDefault();
    Injector result = AppConfiguration.inject(AppConfiguration.getDefault(), env, ImmutableSet.of(new TestModule()));

    Widget innerResult = result.getInstance(Widget.class);
    assertEquals("jim", innerResult.setName("jim").getName());
  }

  /**
   Test the injection of the configuration into an injector
   */
  @Test
  public void inject_okWithNoModules() throws AppException {
    var env = Environment.getDefault();

    var config = AppConfiguration.inject(AppConfiguration.getDefault(), env, ImmutableSet.of());
    assertNotNull(config);
  }

  /**
   Test there's an exception if the config is null
   */
  @Test
  public void inject_exceptionWithNullConfig() {
    var env = Environment.getDefault();

    var e = assertThrows(AppException.class,
      () -> AppConfiguration.inject(null, env, ImmutableSet.of()));
    assertEquals("Config cannot be null!", e.getMessage());
  }

  /**
   Test there's an exception if the config is empty
   */
  @Test
  public void inject_exceptionWithEmptyConfig() {
    var env = Environment.getDefault();

    var e = assertThrows(AppException.class,
      () -> AppConfiguration.inject(ConfigFactory.empty(), env, ImmutableSet.of()));
    assertEquals("Config cannot be empty!", e.getMessage());
  }

  /**
   Check the default configuration
   */
  @Test
  public void getDefault() {
    assertEquals(3002, AppConfiguration.getDefault().getInt("app.port"));
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

  /**
   Module for testing app injection
   */
  public static class TestModule extends AbstractModule {
    @Override
    protected void configure() {
      bind(Widget.class).to(WidgetImpl.class);
    }
  }
}
