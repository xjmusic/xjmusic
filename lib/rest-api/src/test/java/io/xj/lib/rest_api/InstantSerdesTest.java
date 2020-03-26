// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.rest_api;

import com.google.inject.Guice;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;

import static org.junit.Assert.assertEquals;

public class InstantSerdesTest {
  private Instant subject;
  private String subjectJson;
  private PayloadFactory payloadFactory;

  @Before
  public void setUp() {
    payloadFactory = Guice.createInjector(new RestApiModule()).getInstance(PayloadFactory.class);
    subject = Instant.parse("2014-09-11T12:17:00.679314Z");
    subjectJson = "\"2014-09-11T12:17:00.679314Z\"";
  }

  @Test
  public void serialize() throws RestApiException {
    assertEquals(subjectJson, payloadFactory.serialize(subject));
  }

  @Test
  public void deserialize() throws RestApiException {
    assertEquals(subject, payloadFactory.deserialize(subjectJson, Instant.class));
  }

}
