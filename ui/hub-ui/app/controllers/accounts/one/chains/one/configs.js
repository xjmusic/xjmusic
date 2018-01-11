// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import { set } from '@ember/object';

import { inject as service } from '@ember/service';
import Controller from '@ember/controller';

export default Controller.extend({

  display: service(),

  actions: {

    /**
     * <select> uses this as onChange to set value
     * @param type
     * @returns {*}
     */
    selectChainConfigToAddType(type){
      set(this, 'model.chainConfigToAdd.type', type);
      return type;
    },

  }

});
