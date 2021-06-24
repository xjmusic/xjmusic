// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.app;

import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.typesafe.config.ConfigFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.util.Objects;

import static org.junit.Assert.assertEquals;

public class AppConfigurationTest {

  @Rule
  public ExpectedException failure = ExpectedException.none();

  /**
   Attempts to create an AppConfiguration via its private constructor and
   demonstrates that an exception is thrown.
   */
  @Test(expected = IllegalStateException.class)
  public void constructorMustFail() {
    AppConfiguration config = new AppConfiguration();
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
    AppConfiguration.inject(AppConfiguration.getDefault(), env, ImmutableSet.of());
  }

  /**
   Test there's an exception if the config is null
   */
  @Test
  public void inject_exceptionWithNullConfig() throws AppException {
    failure.expect(AppException.class);
    failure.expectMessage("Config cannot be null!");
    var env = Environment.getDefault();

    AppConfiguration.inject(null, env, ImmutableSet.of());
  }

  /**
   Test there's an exception if the config is empty
   */
  @Test
  public void inject_exceptionWithEmptyConfig() throws AppException {
    failure.expect(AppException.class);
    failure.expectMessage("Config cannot be empty!");
    var env = Environment.getDefault();

    AppConfiguration.inject(ConfigFactory.empty(), env, ImmutableSet.of());
  }

  /**
   Check the default configuration
   */
  @Test
  public void getDefault() {
    assertEquals(3002, AppConfiguration.getDefault().getInt("app.port"));
  }

  /**
   Get the absolute path to a file based on its resource name

   @param name of resource to get absolute path for
   @return absolute path of resource
   */
  private String absolutePathOfResource(String name) {
    ClassLoader classLoader = getClass().getClassLoader();
    File file = new File(Objects.requireNonNull(classLoader.getResource(name)).getFile());
    return file.getAbsolutePath();
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
