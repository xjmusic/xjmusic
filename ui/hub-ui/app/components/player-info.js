// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import { inject as service } from '@ember/service';

import Component from '@ember/component';

export default Component.extend({

  // Inject: chain-link player service
  player: service(),

  actions: {
    stop() {
      this.get('player').stop();
    }
  }

});
