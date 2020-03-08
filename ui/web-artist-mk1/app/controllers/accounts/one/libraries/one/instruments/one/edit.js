/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

import { get } from '@ember/object';
import { inject as service } from '@ember/service';
import Controller from '@ember/controller';

export default Controller.extend({
  config: service(),

  actions: {

    selectInstrumentType(type) {
      get(this, 'model').set('type', type);
    },

    selectInstrumentState(state) {
      get(this, 'model').set('state', state);
    },

  }

});
