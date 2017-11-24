// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
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
