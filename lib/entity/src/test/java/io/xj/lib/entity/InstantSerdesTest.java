// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.entity;

import com.google.inject.Guice;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;

import static org.junit.Assert.assertEquals;

public class InstantSerdesTest {
  private Instant subject;
  private String subjectJson;
  private EntityFactory entityFactory;

  @Before
  public void setUp() {
    entityFactory = Guice.createInjector(new EntityModule()).getInstance(EntityFactory.class);
    subject = Instant.parse("2014-09-11T12:17:00.679314Z");
    subjectJson = "\"2014-09-11T12:17:00.679314Z\"";
  }

  @Test
  public void serialize() throws EntityException {
    assertEquals(subjectJson, entityFactory.serialize(subject));
  }

  @Test
  public void deserialize() throws EntityException {
    assertEquals(subject, entityFactory.deserialize(Instant.class, subjectJson));
  }

}
