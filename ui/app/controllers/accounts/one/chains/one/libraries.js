// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Controller.extend({

  display: Ember.inject.service(),

  actions: {

    /**
     * Ember power-select uses this as onChange to set value
     * @param library
     * @returns {*}
     */
    setLibraryToAdd(library){
      this.set('model.libraryToAdd', library);
      return library;
    },

  }

});
