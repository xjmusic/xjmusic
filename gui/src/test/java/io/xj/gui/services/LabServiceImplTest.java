// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.services;

import io.xj.gui.services.impl.LabServiceImpl;
import javafx.application.HostServices;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class LabServiceImplTest {
  LabService subject;

  @Mock
  HostServices hostServices;

  @BeforeEach
  public void setUp() {
    subject = new LabServiceImpl(hostServices,
      "https://lab.test.xj.io/",
      "https://audio.test.xj.io/",
      "https://ship.test.xj.io/",
      "https://stream.test.xj.io/"
    );
  }

  @Test
  public void getAppURI() {
    var result = subject.computeUri("test");
    assertEquals("lab.test.xj.io", result.getHost());
  }

  @Test
  public void getAppUrl() {
    assertEquals("https://lab.test.xj.io/test", subject.computeUrl("test"));
    assertEquals("https://lab.test.xj.io/test/123", subject.computeUrl("test/123"));
    assertEquals("https://lab.test.xj.io/test", subject.computeUrl("/test")); // strips leading slash
  }
}
