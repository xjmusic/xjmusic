// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.filestore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class FileStoreProviderImplTest {
  @Mock
  private FileStoreProvider fileStoreProvider;


  @Before
  public void setUp() throws Exception {
    fileStoreProvider = new FileStoreProviderImpl(
      "", "https://s3.amazonaws.com/test-bucket/", "AKIALKSFDJKGIOURTJ7H", "jhfd897+jkhjHJJDKJF/908090JHKJJHhjhfg78h", "xj-dev-audio", 1, ""
    );
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
