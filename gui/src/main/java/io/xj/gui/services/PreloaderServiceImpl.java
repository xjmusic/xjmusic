// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.services;

import io.xj.hub.HubContent;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.hub.util.StringUtils;
import io.xj.nexus.dub.DubAudioCache;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

import static io.xj.nexus.mixer.FixedSampleBits.FIXED_SAMPLE_BITS;

@org.springframework.stereotype.Service
public class PreloaderServiceImpl extends Service<Boolean> implements PreloaderService {
  static final Logger LOG = LoggerFactory.getLogger(PreloaderServiceImpl.class);
  private final LabService labService;
  private final FabricationService fabricationService;
  private final DubAudioCache dubAudioCache;
  private final BooleanProperty isServiceRunning = new SimpleBooleanProperty(false);

  public PreloaderServiceImpl(
    LabService labService,
    FabricationService fabricationService,
    DubAudioCache dubAudioCache
  ) {
    this.labService = labService;
    this.fabricationService = fabricationService;
    this.dubAudioCache = dubAudioCache;

    isServiceRunning.bind(runningProperty());
  }

  // TODO get this preloading service outside of here and into a background task of the work manager
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
          var instruments = new ArrayList<>(hubContent.getInstruments());
          var audios = new ArrayList<>(hubContent.getInstrumentAudios());
          int loaded = 0;
          for (Instrument instrument : instruments) {
            for (InstrumentAudio audio : audios.stream()
              .filter(a -> Objects.equals(a.getInstrumentId(), instrument.getId()))
              .sorted(Comparator.comparing(InstrumentAudio::getName))
              .toList()) {
              if (!isServiceRunning.get()) {
                return false;
              }
              if (!StringUtils.isNullOrEmpty(audio.getWaveformKey()))
                dubAudioCache.load(
                  fabricationService.contentStoragePathPrefixProperty().get(),
                  labService.hubConfigProperty().get().getAudioBaseUrl(),
                  audio.getInstrumentId(),
                  audio.getWaveformKey(),
                  (int) Double.parseDouble(fabricationService.outputFrameRateProperty().get()),
                  FIXED_SAMPLE_BITS,
                  (int) Double.parseDouble(fabricationService.outputChannelsProperty().get()));
              updateProgress((float) loaded++ / audios.size(), 1.0);
            }
          }
          updateProgress(1.0, 1.0);
          LOG.info("Preloaded {} audios from {} instruments", loaded, instruments.size());

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
