package io.xj.nexus.util;

import java.io.*;
import java.net.URL;
import java.util.Objects;
import java.util.stream.Collectors;

public class InternalResource {
  File file;

  public InternalResource(String fileName) {
    URL resource = getClass().getResource(fileName);
    if (resource != null) {
      this.file = new File(resource.getFile());
    }
  }

  /**
   Reads given resource file as a string.

   @param fileName path to the resource file
   @return the file's contents
   @throws IOException if read fails for any reason
   */
  public static String readAsString(String fileName) throws IOException {
    File file = new InternalResource(fileName).getFile();
    try (
      InputStream is = new FileInputStream(file);
      InputStreamReader isr = new InputStreamReader(is);
      BufferedReader br = new BufferedReader(isr)
    ) {
      return br.lines().collect(Collectors.joining(System.lineSeparator()));
    }
  }

  public File getFile() {
    return this.file;
  }
}
