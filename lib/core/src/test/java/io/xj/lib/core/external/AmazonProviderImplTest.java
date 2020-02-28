// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.external;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueFactory;
import io.xj.lib.core.CoreModule;
import io.xj.lib.core.access.TokenGenerator;
import io.xj.lib.core.app.AppConfiguration;
import io.xj.lib.core.testing.AppTestConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AmazonProviderImplTest {
  @Mock
  private TokenGenerator tokenGenerator;
  private AmazonProvider amazonProvider;

  @Before
  public void setUp() throws Exception {
    Config config = AppTestConfiguration.getDefault()
      .withValue("audio.uploadURL", ConfigValueFactory.fromAnyRef("https://s3.amazonaws.com/test-bucket/"))
      .withValue("aws.accessKeyID", ConfigValueFactory.fromAnyRef("AKIALKSFDJKGIOURTJ7H"))
      .withValue("aws.secretKey", ConfigValueFactory.fromAnyRef("jhfd897+jkhjHJJDKJF/908090JHKJJHhjhfg78h"))
      .withValue("audio.fileBucket", ConfigValueFactory.fromAnyRef("xj-dev-audio"));
    Injector injector = AppConfiguration.inject(config, ImmutableList.of(Modules.override(new CoreModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(TokenGenerator.class).toInstance(tokenGenerator);
        }
      })));

    amazonProvider = injector.getInstance(AmazonProvider.class);
  }

  @Test
  public void generateUploadPolicy() throws Exception {
    S3UploadPolicy policy = amazonProvider.generateAudioUploadPolicy();

    assertNotNull(policy);
  }

  @Test
  public void generateKey() {
    when(tokenGenerator.generateShort())
      .thenReturn("token123");

    String url = amazonProvider.generateKey("file-name", "wav");

    assertEquals("token123-file-name.wav", url);
  }

  @Test
  public void getUploadUrl() throws Exception {
    when(tokenGenerator.generateShort())
      .thenReturn("token123");

    String url = amazonProvider.getUploadURL();

    assertEquals("https://s3.amazonaws.com/test-bucket/", url);
  }

}
