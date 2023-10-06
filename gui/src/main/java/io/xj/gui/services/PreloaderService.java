// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.services;

import javafx.beans.binding.StringBinding;
import javafx.concurrent.Worker;
import javafx.event.EventTarget;

public interface PreloaderService extends Worker<Boolean>, EventTarget {
  void resetAndStart();

  /**
    * The text to display on the button that launches the preloader
   * @return button text
   */
  StringBinding actionTextProperty();
}
