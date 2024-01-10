package io.xj.nexus.hub_client;

import io.xj.hub.HubConfiguration;
import io.xj.hub.HubContent;
import io.xj.nexus.InputMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.Callable;

public class HubContentProvider implements Callable<HubContent> {
  private static final Logger LOG = LoggerFactory.getLogger(HubContentProvider.class);
  private final HubClient hubClient;
  private final HubConfiguration hubConfig;
  private final HubClientAccess hubAccess;
  private final InputMode inputMode;
  private final String inputTemplateKey;

  public HubContentProvider(HubClient hubClient, HubConfiguration hubConfig, HubClientAccess hubAccess, InputMode inputMode, String inputTemplateKey) {
    this.hubClient = hubClient;
    this.hubConfig = hubConfig;
    this.hubAccess = hubAccess;
    this.inputMode = inputMode;
    this.inputTemplateKey = inputTemplateKey;
    LOG.debug("Did instantiate HubContentProvider with inputMode: {}, inputTemplateKey: {}", inputMode, inputTemplateKey);
  }

  @Override
  public HubContent call() throws Exception {
    return switch (inputMode) {
      case PREVIEW -> hubClient.ingest(hubConfig.getBaseUrl(), hubAccess, UUID.fromString(inputTemplateKey));
      case PRODUCTION -> {
        var material = hubClient.load(inputTemplateKey, hubConfig.getAudioBaseUrl());
        yield material;
      }
    };
  }
}
