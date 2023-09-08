// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

class VersionServiceTest {
  final Pattern rgxThreeMonotonicDigits = Pattern.compile("^[0-9]*\\.[0-9]*\\.[0-9]*$");
  private VersionService subject;

  @BeforeEach
  void setUp() {
    // load resource from "version.properties" file
    Resource versionProperties = new Resource() {
      @Override
      public boolean exists() {
        return true;
      }

      @Override
      public boolean isReadable() {
        return true;
      }

      @Override
      public boolean isOpen() {
        return false;
      }

      @Override
      public URL getURL() throws IOException {
        return null;
      }

      @Override
      public URI getURI() throws IOException {
        return null;
      }

      @Override
      public File getFile() throws IOException {
        return null;
      }

      @Override
      public long contentLength() throws IOException {
        return 0;
      }

      @Override
      public long lastModified() throws IOException {
        return 0;
      }

      @Override
      public Resource createRelative(String relativePath) throws IOException {
        return null;
      }

      @Override
      public String getFilename() {
        return "version.properties";
      }

      @Override
      public String getDescription() {
        return null;
      }

      @Override
      public InputStream getInputStream() throws IOException {
        return getClass().getClassLoader().getResourceAsStream("version.properties");
      }
    };
    subject = new VersionService(versionProperties);
    subject.init();
  }

  @Test
  void getVersion() {
    assertTrue(rgxThreeMonotonicDigits.matcher(subject.getVersion()).matches());
  }
}
