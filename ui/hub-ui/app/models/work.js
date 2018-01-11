// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import DS from "ember-data";
import {computed} from '@ember/object';

export default DS.Model.extend({
  name: DS.attr('string'),
  state: DS.attr('string'),
  type: DS.attr('string'),
  targetId: DS.attr('number'),

  targetType: computed(function () {
    switch (this.get('type')) {
      case 'AudioErase':
        return 'Audio';
      case 'ChainErase':
        return 'Chain';
      case 'ChainFabricate':
        return 'Chain';
      case 'LinkCraft':
        return 'Link';
      case 'LinkDub':
        return 'Link';
    }
  }),

  targetAction: computed(function () {
    switch (this.get('type')) {
      case 'AudioErase':
        return 'Erase';
      case 'ChainErase':
        return 'Erase';
      case 'ChainFabricate':
        return 'Fabricate';
      case 'LinkCraft':
        return 'Craft';
      case 'LinkDub':
        return 'Dub';
    }
  }),

});

