/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */
import {inject as service} from '@ember/service';

import Controller from '@ember/controller';

export default Controller.extend({

  display: service(),

  actions: {

    /**
     * Ember power-select uses this as onChange to set value
     * @param type
     * @returns {*}
     */
    setBindingType(type) {
      this.set('model.bindingType', type);
    },

    /**
     * Ember power-select uses this as onChange to set value
     * @param library
     * @returns {*}
     */
    setLibraryToAdd(library) {
      this.set('model.bindingTargetId', library.get('id'));
      this.set('model.libraryToAdd', library);
      return library;
    },

    /**
     * Ember power-select uses this as onChange to set value
     * @param program
     * @returns {*}
     */
    setProgramToAdd(program) {
      this.set('model.bindingTargetId', program.get('id'));
      this.set('model.programToAdd', program);
      return program;
    },

    /**
     * Ember power-select uses this as onChange to set value
     * @param instrument
     * @returns {*}
     */
    setInstrumentToAdd(instrument) {
      this.set('model.bindingTargetId', instrument.get('id'));
      this.set('model.instrumentToAdd', instrument);
      return instrument;
    },

  }

});
