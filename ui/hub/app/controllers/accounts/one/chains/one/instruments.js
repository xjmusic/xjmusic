// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Controller.extend({

  display: Ember.inject.service(),

  actions: {

    /**
     * Ember power-select uses this as onChange to set value
     * @param instrument
     * @returns {*}
     */
    setInstrumentToAdd(instrument){
      this.set('model.instrumentToAdd', instrument);
      return instrument;
    },

  }

});
