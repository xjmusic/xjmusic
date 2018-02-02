// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import DS from "ember-data";
import {computed} from '@ember/object';

export default DS.Model.extend({
  name: DS.attr('string'),
  state: DS.attr('string'),
  type: DS.attr('string'),
  targetId: DS.attr('number'),

  targetType: computed(function () {
    return this.get('type');
  }),

  targetAction: computed(function () {
    return this.get('type');
  }),

});

