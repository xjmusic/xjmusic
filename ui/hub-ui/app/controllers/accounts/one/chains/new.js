// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import { get } from '@ember/object';

import { inject as service } from '@ember/service';
import Controller from '@ember/controller';

export default Controller.extend({
  config: service(),

  actions: {

    selectChainState(state) {
      get(this, 'model.chain').set('state', state);
    },

    selectChainType(type) {
      get(this, 'model.chain').set('type', type);
    },

  }

});
