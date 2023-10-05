// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.services;

import javafx.concurrent.Worker;
import javafx.event.EventTarget;

public interface PreloaderService extends Worker<Boolean>, EventTarget {
  void resetAndStart();

}
