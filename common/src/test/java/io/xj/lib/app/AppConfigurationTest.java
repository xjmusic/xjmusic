// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.app;

import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.io.FileNotFoundException;
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
   Reads the file **valid.conf** from the **test/resources** folder
   as if it were passed in as the first argument when starting a XJ Music app
   */
  @Test
  public void parseArgs() throws AppException {
    String configFilePath = absolutePathOfResource("valid.conf");

    Config result = AppConfiguration.parseArgs(new String[]{configFilePath}, AppConfiguration.getDefault());

    assertEquals("testing", result.getString("app.name"));
    assertEquals(8784, result.getInt("app.port"));
  }

  /**
   Returns default config if given no path to a config file
   */
  @Test
  public void parseArgs_returnsDefaultsIfNoArgsGiven() throws AppException {
    Config result = AppConfiguration.parseArgs(new String[]{}, AppConfiguration.getDefault().withValue("check", ConfigValueFactory.fromAnyRef(1234)));

    assertEquals(1234, result.getInt("check"));
  }

  /**
   Reads the file **invalid.conf** from the **test/resources** folder
   as if it were passed in as the first argument when starting a XJ Music app
   and confirms that an exception is thrown with failure to process
   */
  @Test
  public void parseArgs_throwsExceptionOnInvalidConfigFile() throws AppException {
    String configFilePath = absolutePathOfResource("invalid.conf");

    failure.expect(AppException.class);
    failure.expectCause(IsInstanceOf.instanceOf(ConfigException.class));
    failure.expectMessage("Unable to parse configuration");

    AppConfiguration.parseArgs(new String[]{configFilePath}, AppConfiguration.getDefault());
  }

  /**
   Reads the file **nonexistent.conf** from the **test/resources** folder
   as if it were passed in as the first argument when starting a XJ Music app
   and confirms that an exception is thrown with failure to find the file
   */
  @Test
  public void parseArgs_throwsExceptionOnNonexistentConfigFile() throws AppException {
    String configFilePath = "/nonexistent.conf";

    failure.expect(AppException.class);
    failure.expectCause(IsInstanceOf.instanceOf(FileNotFoundException.class));
    failure.expectMessage("Error reading configuration file");

    AppConfiguration.parseArgs(new String[]{configFilePath}, AppConfiguration.getDefault());
  }

  /**
   Test the injection of the configuration into an injector
   */
  @Test
  public void inject() throws AppException {
    Injector result = AppConfiguration.inject(AppConfiguration.getDefault(), ImmutableSet.of(new TestModule()));

    Widget innerResult = result.getInstance(Widget.class);
    assertEquals("jim", innerResult.setName("jim").getName());
  }

  /**
   Test the injection of the configuration into an injector
   */
  @Test
  public void inject_okWithNoModules() throws AppException {
    AppConfiguration.inject(AppConfiguration.getDefault(), ImmutableSet.of());
  }

  /**
   Test there's an exception if the config is null
   */
  @Test
  public void inject_exceptionWithNullConfig() throws AppException {
    failure.expect(AppException.class);
    failure.expectMessage("Config cannot be null!");

    AppConfiguration.inject(null, ImmutableSet.of());
  }

  /**
   Test there's an exception if the config is empty
   */
  @Test
  public void inject_exceptionWithEmptyConfig() throws AppException {
    failure.expect(AppException.class);
    failure.expectMessage("Config cannot be empty!");

    AppConfiguration.inject(ConfigFactory.empty(), ImmutableSet.of());
  }

  /**
   Check the default configuration
   */
  @Test
  public void getDefault() {
    assertEquals(3000, AppConfiguration.getDefault().getInt("app.port"));
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
