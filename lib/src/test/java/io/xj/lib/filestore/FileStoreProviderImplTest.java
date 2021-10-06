// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.filestore;

import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.lib.app.Environment;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FileStoreProviderImplTest {
  @Mock
  private FileStoreProvider fileStoreProvider;

  @Mock
  private Environment env;

  @Before
  public void setUp() throws Exception {
    var injector = Guice.createInjector(ImmutableSet.of(Modules.override(new FileStoreModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(Environment.class).toInstance(env);
          }
      })));
    when(env.getAudioUploadURL()).thenReturn("https://s3.amazonaws.com/test-bucket/");
    when(env.getAwsAccessKeyID()).thenReturn("AKIALKSFDJKGIOURTJ7H");
    when(env.getAwsSecretKey()).thenReturn("jhfd897+jkhjHJJDKJF/908090JHKJJHhjhfg78h");
    when(env.getAudioFileBucket()).thenReturn("xj-dev-audio");
    fileStoreProvider = injector.getInstance(FileStoreProvider.class);
  }

  @Test
  public void jsonExtension() {
    assertEquals("json", FileStoreProvider.EXTENSION_JSON);
  }

  @Test
  public void generateUploadPolicy() throws Exception {
    S3UploadPolicy policy = fileStoreProvider.generateAudioUploadPolicy();

    assertNotNull(policy);
  }

  @Test
  public void generateKey() {
    String url = fileStoreProvider.generateKey("file-name");

    assertContains(url);
  }

  @Test
  public void getUploadUrl() throws Exception {
    String url = fileStoreProvider.getUploadURL();

    assertEquals("https://s3.amazonaws.com/test-bucket/", url);
  }

  /**
   Assert result contains@param actual           result to search for contained
   */
  private void assertContains(String actual) {
    assertTrue(String.format("Contains '%s'", "file-name-"), actual.contains("file-name-"));
  }

}
