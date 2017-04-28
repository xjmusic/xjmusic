// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Controller.extend({
  config: Ember.inject.service(),

  actions: {

    selectChainState(state) {
      Ember.get(this, 'model.chain').set('state', state);
    },

    selectChainType(type) {
      Ember.get(this, 'model.chain').set('type', type);
    },

  }

});
