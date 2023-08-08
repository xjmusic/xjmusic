// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.lib.filestore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class FileStoreProviderImplTest {
  @Mock
  FileStoreProvider fileStoreProvider;

  @BeforeEach
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
