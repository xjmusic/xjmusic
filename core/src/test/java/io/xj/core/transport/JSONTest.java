// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.transport;// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

import io.xj.core.model.chain.Chain;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import org.json.JSONObject;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class JSONTest {

  @Test
  public void objectFrom() {
    Chain source = new Chain(BigInteger.valueOf(25L));
    source.setAccountId(BigInteger.valueOf(517L));
    source.setEmbedKey("donuts");
    source.setName("gold chain");
    source.setStartAt("2011-08-12 12:17:02.687327");
    source.setStopAt("2012-09-11 12:17:01.989941");
    source.setStateEnum(ChainState.Complete);
    source.setTypeEnum(ChainType.Production);
    source.setCreatedAt("2009-08-12 12:17:02.687327");
    source.setUpdatedAt("2010-09-11 12:17:01.989941");

    JSONObject result = JSON.objectFrom(source);

    assertNotNull(result);
    assertEquals(517, result.get("accountId"));
    assertEquals("donuts", result.get("embedKey"));
    assertEquals("gold chain", result.get("name"));
    assertEquals("2011-08-12 12:17:02.687327Z", result.get("startAt"));
    assertEquals("2012-09-11 12:17:01.989941Z", result.get("stopAt"));
    assertEquals("Complete", result.get("state"));
    assertEquals("Production", result.get("type"));
    assertEquals("2009-08-12 12:17:02.687327Z", result.get("createdAt"));
    assertEquals("2010-09-11 12:17:01.989941Z", result.get("updatedAt"));
  }

  @Test
  public void objectFrom_passThroughNull() {
    assertNull(JSON.objectFrom(null));
  }

  @Test
  public void ensureArrayAt() {
    JSONObject target = new JSONObject("{\"test\":\"value\"}");

    JSON.ensureArrayAt(target, "key2");

    target.getJSONArray("key2").put("val1");
    assertEquals("val1", target.getJSONArray("key2").get(0));
  }

  @Test
  public void ensureObjectAt() {
    JSONObject target = new JSONObject("{\"test\":\"value\"}");

    JSON.ensureObjectAt(target, "key2");

    target.getJSONObject("key2").put("key3", "val5");
    assertEquals("val5", target.getJSONObject("key2").get("key3"));
  }

  @Test
  public void putInSubArray() {
    JSONObject target = new JSONObject("{\"test\":\"value\"}");

    JSON.putInSubArray(target, "key2", "val1");

    assertEquals("val1", target.getJSONArray("key2").get(0));
  }

  @Test
  public void putInSubObject() {
    JSONObject target = new JSONObject("{\"test\":\"value\"}");

    JSON.putInSubObject(target, "key2", "key3", "val5");

    assertEquals("val5", target.getJSONObject("key2").get("key3"));
  }

}
