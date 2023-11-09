// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.jsonapi;


import io.xj.nexus.entity.EntityException;
import io.xj.nexus.entity.EntityFactory;
import io.xj.nexus.entity.EntityFactoryImpl;
import io.xj.nexus.json.JsonProviderImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InstantSerDesTest {
  Instant subject;
  String subjectJson;
  EntityFactory entityFactory;

  @BeforeEach
  public void setUp() {
    var jsonProvider = new JsonProviderImpl();
    entityFactory = new EntityFactoryImpl(jsonProvider);
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
