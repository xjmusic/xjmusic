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

  filterProgramType: "",

  filterProgramState: "",

  filterProgramName: "",

  isFoundIn: (name, filterProgramName) => {
    return name.toLowerCase().indexOf(filterProgramName.toLowerCase()) !== -1;
  },

  valueOrEmpty(name) {
    return null !== name && undefined !== name && "" !== name && name.length > 0 ? name : "";
  },

  filteredPrograms: computed(
    'model.programs.@each',
    'filterProgramType',
    'filterProgramState',
    'filterProgramName',
    function () {
      let self = this;
      return this.model.programs
        .filter(program => {
          if ("" !== self.filterProgramType &&
            self.filterProgramType !== program.get("type")) return false;
          if ("" !== self.filterProgramState &&
            self.filterProgramState !== program.get("state")) return false;
          if ("" !== self.filterProgramName && 1 < self.filterProgramName.length &&
            !self.isFoundIn(program.get("name"), self.filterProgramName)) return false;
          return true;
        });
    }),

  actions: {

    selectFilterProgramType(type) {
      set(this, 'filterProgramType', this.valueOrEmpty(type));
    },

    selectFilterProgramState(state) {
      set(this, 'filterProgramState', this.valueOrEmpty(state));
    },

    updateFilterProgramName(name) {
      set(this, 'filterProgramName', this.valueOrEmpty(name));
    }

  }

});
