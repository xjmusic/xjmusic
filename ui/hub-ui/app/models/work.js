//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import Model, {attr} from '@ember-data/model';
import {computed} from '@ember/object';

export default Model.extend({
  name: attr('string'),
  state: attr('string'),
  type: attr('string'),
  targetId: attr('number'),

  targetType: computed(function () {
    return this.type;
  }),

  targetAction: computed(function () {
    return this.type;
  }),

});

