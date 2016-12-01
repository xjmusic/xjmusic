package io.outright.xj.core.application;// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.

public interface Application {
  /**
   * Start Application Server
   */
  void Start();

  /**
   * Stop Application Server
   */
  void Stop();

  /**
   * Base URI of Application Server
   * @return String
   */
  String BaseURI();
}
