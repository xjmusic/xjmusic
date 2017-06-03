// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.chain_gang;

import org.json.JSONObject;

public interface Follower {
  Runnable getTaskRunnable(JSONObject task) throws Exception;
}
