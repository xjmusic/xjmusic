/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

import {get} from '@ember/object';
import {inject as service} from '@ember/service';
import Controller from '@ember/controller';

export default Controller.extend({
  config: service(),

  actions: {

    resetParameters() {
      get(this, 'model').set('config', get(this, 'config').defaultProgramConfig);
    },

    selectProgramType(type) {
      get(this, 'model').set('type', type);
    },

    selectProgramState(state) {
      get(this, 'model').set('state', state);
    },

  }

});
