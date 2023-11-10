package io.xj.nexus.util;

import java.io.File;
import java.net.URL;

public class InternalResource {
  File file;

  public InternalResource(String fileName) {
    URL resource = getClass().getResource(fileName);
    if (resource != null) {
      this.file = new File(resource.getFile());
    }
  }

  public File getFile() {
    return this.file;
  }
}
