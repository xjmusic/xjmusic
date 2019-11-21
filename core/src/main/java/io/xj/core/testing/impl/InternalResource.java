// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.testing.impl;

import java.io.File;
import java.net.URL;

/**
 Get a File() for something in the resources folder
 */
public class InternalResource {

  private File file;

  public InternalResource(String fileName) {
    ClassLoader classLoader = getClass().getClassLoader();
    URL resource = classLoader.getResource(fileName);
    if (resource != null) {
      file = new File(resource.getFile());
    }
  }

  public File getFile() {
    return file;
  }
}
