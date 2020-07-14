// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.filestore;

import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class FileStoreProviderImplTest {
  @Mock
  private FileStoreProvider fileStoreProvider;

  @Before
  public void setUp() throws Exception {
    Config config = ConfigFactory.parseResources("default.conf")
      .withValue("audio.uploadUrl", ConfigValueFactory.fromAnyRef("https://s3.amazonaws.com/test-bucket/"))
      .withValue("aws.accessKeyID", ConfigValueFactory.fromAnyRef("AKIALKSFDJKGIOURTJ7H"))
      .withValue("aws.secretKey", ConfigValueFactory.fromAnyRef("jhfd897+jkhjHJJDKJF/908090JHKJJHhjhfg78h"))
      .withValue("audio.fileBucket", ConfigValueFactory.fromAnyRef("xj-dev-audio"));
    Injector injector = Guice.createInjector(ImmutableSet.of(Modules.override(new FileStoreModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(Config.class).toInstance(config);
        }
      })));

    fileStoreProvider = injector.getInstance(FileStoreProvider.class);
  }

  @Test
  public void generateUploadPolicy() throws Exception {
    S3UploadPolicy policy = fileStoreProvider.generateAudioUploadPolicy();

    assertNotNull(policy);
  }

  @Test
  public void generateKey() {
    String url = fileStoreProvider.generateKey("file-name", "wav");

    assertContains("-file-name.wav", url);
  }

  @Test
  public void getUploadUrl() throws Exception {
    String url = fileStoreProvider.getUploadURL();

    assertEquals("https://s3.amazonaws.com/test-bucket/", url);
  }

  /**
   Assert result contains

   @param expectedContains to be contained within actual result
   @param actual           result to search for contained
   */
  private void assertContains(String expectedContains, String actual) {
    assertTrue(String.format("Contains '%s'", expectedContains), actual.contains(expectedContains));
  }

}
