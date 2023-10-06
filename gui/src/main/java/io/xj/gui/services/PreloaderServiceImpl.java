// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.services;

import io.xj.hub.HubContent;
import io.xj.hub.util.StringUtils;
import io.xj.nexus.dub.DubAudioCache;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import static io.xj.nexus.mixer.FixedSampleBits.FIXED_SAMPLE_BITS;

@org.springframework.stereotype.Service
public class PreloaderServiceImpl extends Service<Boolean> implements PreloaderService {
  static final Logger LOG = LoggerFactory.getLogger(PreloaderServiceImpl.class);
  private final LabService labService;
  private final FabricationService fabricationService;
  private final DubAudioCache dubAudioCache;

  public PreloaderServiceImpl(
    LabService labService,
    FabricationService fabricationService,
    DubAudioCache dubAudioCache
  ) {
    this.labService = labService;
    this.fabricationService = fabricationService;
    this.dubAudioCache = dubAudioCache;
  }

  protected Task<Boolean> createTask() {
    return new Task<>() {
      protected Boolean call() {
        HubContent hubContent;
        try {
          hubContent = fabricationService.getHubContentProvider().call();
        } catch (Exception e) {
          LOG.warn("Failed to get hub content", e);
          return false;
        }

        try {
          var audios = new ArrayList<>(hubContent.getInstrumentAudios());
          for (var i = 1; i < audios.size(); i++) {
            if (!StringUtils.isNullOrEmpty(audios.get(i).getWaveformKey()))
              dubAudioCache.load(
                fabricationService.contentStoragePathPrefixProperty().get(),
                labService.hubConfigProperty().get().getAudioBaseUrl(),
                audios.get(i).getInstrumentId(),
                audios.get(i).getWaveformKey(),
                (int) Double.parseDouble(fabricationService.outputFrameRateProperty().get()),
                FIXED_SAMPLE_BITS,
                (int) Double.parseDouble(fabricationService.outputChannelsProperty().get()));
            updateProgress((float) i / audios.size(), 1.0);
          }
          updateProgress(1.0, 1.0);

        } catch (Exception e) {
          LOG.error("Failed to preload", e);
          return false;
        }

        return true;
      }
    };
  }

  @Override
  public void resetAndStart() {
    reset();
    start();
  }

  @Override
  public StringBinding actionTextProperty() {
    return Bindings.createStringBinding(
      () -> {
        if (runningProperty().get())
          return "Cancel";
        else
          return "Preload";
      },
      runningProperty());
  }
}
