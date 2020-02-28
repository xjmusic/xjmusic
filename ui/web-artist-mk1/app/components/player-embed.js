/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */
import {inject as service} from '@ember/service';

import Component from '@ember/component';

export default Component.extend({

  // Inject: chain-segment player service
  player: service(),

  actions: {
    stop() {
      this.player.stop();
    }
  }

});

