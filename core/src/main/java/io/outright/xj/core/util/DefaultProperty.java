// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.util;

abstract public class DefaultProperty {
  /**
   * Set a System Property if no value has yet been set for it.
   * @param k name of system property
   * @param v default value to set for property
   */
  public static void setIfNotAlready(String k, String v) {
    if (System.getProperty(k)==null) {
      System.setProperty(k, v);
    }
  }
}
