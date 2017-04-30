// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.mix.impl.resource;

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
