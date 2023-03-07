// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.filestore;

import io.xj.lib.app.AppEnvironment;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FileStoreProviderImplTest {
  @Mock
  private FileStoreProvider fileStoreProvider;

  @Mock
  private AppEnvironment env;

  @Before
  public void setUp() throws Exception {
    when(env.getAudioUploadURL()).thenReturn("https://s3.amazonaws.com/test-bucket/");
    when(env.getAwsAccessKeyID()).thenReturn("AKIALKSFDJKGIOURTJ7H");
    when(env.getAwsSecretKey()).thenReturn("jhfd897+jkhjHJJDKJF/908090JHKJJHhjhfg78h");
    when(env.getAudioFileBucket()).thenReturn("xj-dev-audio");
    fileStoreProvider = new FileStoreProviderImpl(env);
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
  public void getUploadUrl() throws Exception {
    String url = fileStoreProvider.getUploadURL();

    assertEquals("https://s3.amazonaws.com/test-bucket/", url);
  }
}
