// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Controller.extend({

  display: Ember.inject.service(),

  actions: {

    /**
     * <select> uses this as onChange to set value
     * @param type
     * @returns {*}
     */
    selectChainConfigToAddType(type){
      Ember.set(this, 'model.chainConfigToAdd.type', type);
      return type;
    },

  }

});
