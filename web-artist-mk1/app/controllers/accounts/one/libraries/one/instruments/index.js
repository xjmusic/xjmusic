/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

import {computed, set} from '@ember/object';
import {inject as service} from '@ember/service';
import Controller from '@ember/controller';

/**
 [#175544878] Programs and Instruments can be filtered by Type, State, or Name in XJ Mk1 UI
 */
export default Controller.extend({

  config: service(),

  filterInstrumentType: "",

  filterInstrumentState: "",

  filterInstrumentName: "",

  isFoundIn: (name, filterInstrumentName) => {
    return name.toLowerCase().indexOf(filterInstrumentName.toLowerCase()) !== -1;
  },

  valueOrEmpty(name) {
    return null !== name && undefined !== name && "" !== name && name.length > 0 ? name : "";
  },

  filteredInstruments: computed(
    'model.instruments.@each',
    'filterInstrumentType',
    'filterInstrumentState',
    'filterInstrumentName',
    function () {
      let self = this;
      return this.model.instruments
        .filter(instrument => {
          if ("" !== self.filterInstrumentType &&
            self.filterInstrumentType !== instrument.get("type")) return false;
          if ("" !== self.filterInstrumentState &&
            self.filterInstrumentState !== instrument.get("state")) return false;
          if ("" !== self.filterInstrumentName && 1 < self.filterInstrumentName.length &&
            !self.isFoundIn(instrument.get("name"), self.filterInstrumentName)) return false;
          return true;
        });
    }),

  actions: {

    selectFilterInstrumentType(type) {
      set(this, 'filterInstrumentType', this.valueOrEmpty(type));
    },

    selectFilterInstrumentState(state) {
      set(this, 'filterInstrumentState', this.valueOrEmpty(state));
    },

    updateFilterInstrumentName(name) {
      set(this, 'filterInstrumentName', this.valueOrEmpty(name));
    }

  }

});
