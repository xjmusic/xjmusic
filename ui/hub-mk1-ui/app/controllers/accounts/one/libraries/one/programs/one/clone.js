// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

import { get } from '@ember/object';
import { inject as service } from '@ember/service';
import Controller from '@ember/controller';

export default Controller.extend({
  config: service(),

  actions: {

    setProgramLibrary(library) {
      let model = get(this, 'model.program');
      model.set('library', library);
    },

  }

});
