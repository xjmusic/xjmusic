// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.services;

import javafx.application.HostServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GuideServiceImpl implements GuideService {
  final HostServices hostServices;
  final String launchGuideUrl;

  public GuideServiceImpl(
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") HostServices hostServices,
    @Value("${gui.launch.guide.url}") String launchGuideUrl
  ) {
    this.hostServices = hostServices;
    this.launchGuideUrl = launchGuideUrl;
  }

  @Override
  public void launchGuideInBrowser() {
    hostServices.showDocument(launchGuideUrl);
  }
}
