// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.service;

import com.google.inject.Inject;
import io.outright.xj.core.engine.Engine;

public class ServiceImpl implements Service {

  @Inject
  private Engine engine;

  @Override
  public String get(String path) {
    if (path.startsWith("engines")) {
      return "{engine:" + engine.Status() + "}";
    } else {
      return "{}";
    }
  }
}
