// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.services;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@org.springframework.stereotype.Service
public class PreloaderServiceImpl extends Service<Boolean> implements PreloaderService {
  static final Logger LOG = LoggerFactory.getLogger(PreloaderServiceImpl.class);
  static final int TOTAL_SECONDS = 5;

  public PreloaderServiceImpl(
  ) {
    // todo something: setOnCancelled((WorkerStateEvent ignored) -> status.set(FabricationStatus.Cancelled));
    // todo something: setOnFailed((WorkerStateEvent ignored) -> status.set(FabricationStatus.Failed));
    // todo something: setOnReady((WorkerStateEvent ignored) -> status.set(FabricationStatus.Standby));
    // todo something: setOnRunning((WorkerStateEvent ignored) -> status.set(FabricationStatus.Active));
    // todo something: setOnScheduled((WorkerStateEvent ignored) -> status.set(FabricationStatus.Starting));
    // todo something: setOnSucceeded((WorkerStateEvent ignored) -> status.set(FabricationStatus.Done));
  }

  protected Task<Boolean> createTask() {
    return new Task<>() {
      protected Boolean call() {
        try {
          for (var i = 1; i < TOTAL_SECONDS; i++) {
            Thread.sleep(1000);
            updateProgress((float) i / TOTAL_SECONDS, 1.0);
          }
        } catch (InterruptedException e) {
          LOG.warn("Interrupted", e);
        }
        // todo preload all audio, update progress, block until done
// todo        (Double ratio) -> updateProgress(ratio, 1.0),
// todo          () -> updateProgress(1.0, 1.0));

        updateProgress(1.0, 1.0);
        return false; // todo return actual success
      }
    };
  }

  @Override
  public void resetAndStart() {
    reset();
    start();
  }
}
