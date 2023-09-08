// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

class VersionServiceTest {
  final Pattern rgxThreeMonotonicDigits = Pattern.compile("^[0-9]*\\.[0-9]*\\.[0-9]*$");
  private VersionService subject;

  @BeforeEach
  void setUp() {
    subject = new VersionService();
    subject.init();
  }

  @Test
  void getVersion() {
    assertTrue(rgxThreeMonotonicDigits.matcher(subject.getVersion()).matches());
  }
}
