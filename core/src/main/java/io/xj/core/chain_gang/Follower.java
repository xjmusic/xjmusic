// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.chain_gang;

import org.json.JSONObject;

public interface Follower {
  Runnable getTaskRunnable(JSONObject task) throws Exception;
}
