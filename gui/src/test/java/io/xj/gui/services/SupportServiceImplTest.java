// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.services;

import io.xj.gui.services.impl.SupportServiceImpl;
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
class SupportServiceImplTest {
  SupportService subject;

  @Mock
  HostServices hostServices;

  @BeforeEach
  void setUp() {
    subject = new SupportServiceImpl(
      hostServices,
      "https://guide.test.xj.io/",
      "https://discord.test.xj.io/",
      "https://tutorial.test.xj.io/",
      "https://test.xjmusic.com/"
    );
  }

  @Test
  void launchGuideInBrowser() {
    subject.launchGuideInBrowser();

    verify(hostServices, times(1)).showDocument(eq("https://guide.test.xj.io/"));
  }

  @Test
  void launchTutorialInBrowser() {
    subject.launchTutorialInBrowser();

    verify(hostServices, times(1)).showDocument(eq("https://tutorial.test.xj.io/"));
  }

  @Test
  void launchDiscordInBrowser() {
    subject.launchDiscordInBrowser();

    verify(hostServices, times(1)).showDocument(eq("https://discord.test.xj.io/"));
  }

  @Test
  void launchWebsiteInBrowser() {
    subject.launchWebsiteInBrowser();

    verify(hostServices, times(1)).showDocument(eq("https://test.xjmusic.com/"));
  }
}
