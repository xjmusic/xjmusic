//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.transport.impl;

import com.google.gson.Gson;
import com.google.inject.Guice;
import io.xj.core.CoreModule;
import io.xj.core.transport.GsonProvider;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;

import static org.junit.Assert.assertEquals;

public class TimestampSerializationTest {
  Gson gson = Guice.createInjector(new CoreModule()).getInstance(GsonProvider.class).gson();
  private Timestamp subject;
  private String subjectJson;

  @Before
  public void setUp() {
    subject = Timestamp.valueOf("2014-09-11 12:17:00.00");
    subjectJson = "\"2014-09-11 12:17:00.0Z\"";
  }

  @Test
  public void serialize() {
    String result = gson.toJson(subject);

    assertEquals(subjectJson, result);
  }

  @Test
  public void deserialize() {
    Timestamp result = gson.fromJson(subjectJson, Timestamp.class);

    assertEquals(subject, result);
  }

}
