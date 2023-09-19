// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.services;

import javafx.application.HostServices;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GuideServiceImplTest {
  GuideService subject;

  @Mock
  HostServices hostServices;

  @BeforeEach
  void setUp() {
    subject = new GuideServiceImpl(hostServices, "https://guide.test.xj.io/");
  }

  @Test
  void launchGuideInBrowser() {
    subject.launchGuideInBrowser();

    verify(hostServices, times(1)).showDocument(eq("https://guide.test.xj.io/"));
  }
}
