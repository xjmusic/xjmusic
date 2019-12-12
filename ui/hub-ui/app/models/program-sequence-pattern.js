// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
import Model, {attr, belongsTo, hasMany} from '@ember-data/model';
import {computed} from '@ember/object';

export default Model.extend({
  density: attr('number'),
  type: attr('string'),
  name: attr('string'),
  total: attr('number'),
  events: hasMany('event'),
  voice: belongsTo('program-voice'),
  program: belongsTo('program'),
  sequence: belongsTo('program-sequence'),

  title: computed('name', 'offset', function () {
    let name = this.name;
    let title = name ? name : '';
    return `${title}@${this.offset}`;
  }),

  toString() {
    return this.title;
  }

});
