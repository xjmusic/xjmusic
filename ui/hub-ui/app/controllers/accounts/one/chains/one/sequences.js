//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import { inject as service } from '@ember/service';

import Controller from '@ember/controller';

export default Controller.extend({

  display: service(),

  actions: {

    /**
     * Ember power-select uses this as onChange to set value
     * @param sequence
     * @returns {*}
     */
    setSequenceToAdd(sequence){
      this.set('model.sequenceToAdd', sequence);
      return sequence;
    },

  }

});
