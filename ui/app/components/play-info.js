// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Component.extend({

  // Inject: chain-link player service
  play: Ember.inject.service(),

  actions: {
    stop() {
      this.get('play').stop();
    }
  }

});
