//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import Model, {attr, belongsTo, hasMany} from '@ember-data/model';
import {computed} from '@ember/object';

export default Model.extend({
  program: belongsTo({}),
  voice: belongsTo({}),
  name: attr('string'),
  events: hasMany('event'),

  title: computed('name', function () {
    return this.name;
  }),

  toString() {
    return this.title;
  }

});
