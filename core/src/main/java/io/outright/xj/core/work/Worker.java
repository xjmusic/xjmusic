// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.work;

import org.json.JSONObject;

public interface Worker {
  Runnable getTaskRunnable(JSONObject task) throws Exception;
}
