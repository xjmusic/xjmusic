// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.transport;// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.

import io.xj.core.model.chain.Chain;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;

import org.json.JSONObject;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

public class JSONTest {

  @Test
  public void objectFrom() throws Exception {
    Chain source = new Chain(BigInteger.valueOf(25));
    source.setAccountId(BigInteger.valueOf(517));
    source.setEmbedKey("donuts");
    source.setName("gold chain");
    source.setStartAt("2011-08-12 12:17:02.687327");
    source.setStopAt("2012-09-11 12:17:01.989941");
    source.setStateEnum(ChainState.Complete);
    source.setTypeEnum(ChainType.Production);
    source.setCreatedAt("2009-08-12 12:17:02.687327");
    source.setUpdatedAt("2010-09-11 12:17:01.989941");

    JSONObject result = JSON.objectFrom(source);

    assertEquals(517,result.get("accountId"));
    assertEquals("donuts",result.get("embedKey"));
    assertEquals("gold chain",result.get("name"));
    assertEquals("2011-08-12 12:17:02.687327Z",result.get("startAt"));
    assertEquals("2012-09-11 12:17:01.989941Z",result.get("stopAt"));
    assertEquals("Complete",result.get("state"));
    assertEquals("Production",result.get("type"));
    assertEquals("2009-08-12 12:17:02.687327Z",result.get("createdAt"));
    assertEquals("2010-09-11 12:17:01.989941Z",result.get("updatedAt"));
  }
}
