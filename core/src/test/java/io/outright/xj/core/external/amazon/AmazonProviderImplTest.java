package io.outright.xj.core.external.amazon;// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.util.token.TokenGenerator;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AmazonProviderImplTest {
  @Mock private TokenGenerator tokenGenerator;
  private Injector injector;
  private AmazonProvider amazonProvider;

  @Before
  public void setUp() throws Exception {
    System.setProperty("aws.file.upload.url", "https://s3.amazonaws.com/test-bucket/");
    System.setProperty("aws.file.upload.key", "AKIALKSFDJKGIOURTJ7H");
    System.setProperty("aws.file.upload.secret", "jhfd897+jkhjHJJDKJF/908090JHKJJHhjhfg78h");
    createInjector();
    amazonProvider = injector.getInstance(AmazonProvider.class);
  }

  @After
  public void tearDown() throws Exception {
    amazonProvider = null;
    System.clearProperty("aws.file.upload.url");
    System.clearProperty("aws.file.upload.key");
    System.clearProperty("aws.file.upload.secret");
  }

  @Test
  public void generateUploadPolicy() throws Exception {
    String url = "https://s3.amazonaws.com/test-bucket/file-name-token123.wav";
    String policy = amazonProvider.generateUploadPolicy(url);

    // TODO test policy generation
//    assertEquals("eh.... my balls", policy);
  }

  @Test
  public void generateKey() throws Exception {
    when(tokenGenerator.generateShort())
      .thenReturn("token123");

    String url = amazonProvider.generateKey("file-name", "wav");

    assertEquals("file-name-token123.wav", url);
  }

  @Test
  public void getUploadUrl() throws Exception {
    when(tokenGenerator.generateShort())
      .thenReturn("token123");

    String url = amazonProvider.getUploadURL();

    assertEquals("https://s3.amazonaws.com/test-bucket/", url);
  }

  private void createInjector() {
    injector = Guice.createInjector(Modules.override(new CoreModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(TokenGenerator.class).toInstance(tokenGenerator);
        }
      }));
  }

}
