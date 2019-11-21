//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
import {inject as service} from '@ember/service';

import Controller from '@ember/controller';

export default Controller.extend({

  display: service(),

  actions: {

    /**
     * Ember power-select uses this as onChange to set value
     * @param library
     * @returns {*}
     */
    setLibraryToAdd(library) {
      this.set('model.libraryToAdd', library);
      return library;
    },

  }

});
