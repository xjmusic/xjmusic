// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.services.impl;

import io.xj.gui.services.SupportService;
import javafx.application.HostServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SupportServiceImpl implements SupportService {
  private final HostServices hostServices;
  private final String launchGuideUrl;
  private final String launchDiscordUrl;
  private final String launchTutorialUrl;
  private final String launchWebsiteUrl;

  public SupportServiceImpl(
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") HostServices hostServices,
    @Value("${gui.launch.guide.url}") String launchGuideUrl,
    @Value("${gui.launch.discord.url}") String launchDiscordUrl,
    @Value("${gui.launch.tutorial.url}") String launchTutorialUrl,
    @Value("${gui.launch.website.url}") String launchWebsiteUrl
  ) {
    this.hostServices = hostServices;
    this.launchGuideUrl = launchGuideUrl;
    this.launchDiscordUrl = launchDiscordUrl;
    this.launchTutorialUrl = launchTutorialUrl;
    this.launchWebsiteUrl = launchWebsiteUrl;
  }

  @Override
  public void launchGuideInBrowser() {
    hostServices.showDocument(launchGuideUrl);
  }

  @Override
  public void launchTutorialInBrowser() {
    hostServices.showDocument(launchTutorialUrl);
  }

  @Override
  public void launchDiscordInBrowser() {
    hostServices.showDocument(launchDiscordUrl);
  }

  @Override
  public void launchWebsiteInBrowser() {
    hostServices.showDocument(launchWebsiteUrl);
  }
}
