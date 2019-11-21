//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
import {set} from '@ember/object';
import Controller from '@ember/controller';

export default Controller.extend({

  actions: {

    /**
     * <select> uses this as onChange to set value
     * @param type
     * @returns {*}
     */
    selectMessageType(type) {
      set(this, 'model.type', type);
      return type;
    },

  }

});
