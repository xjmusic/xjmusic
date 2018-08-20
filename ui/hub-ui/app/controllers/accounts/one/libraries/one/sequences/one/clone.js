//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

import { get } from '@ember/object';
import { inject as service } from '@ember/service';
import Controller from '@ember/controller';

export default Controller.extend({
  config: service(),

  actions: {

    setSequenceLibrary(library) {
      let model = get(this, 'model.sequence');
      model.set('library', library);
    },

  }

});
