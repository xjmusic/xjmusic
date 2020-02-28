/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */
import Model, {attr, belongsTo, hasMany} from '@ember-data/model';
import {computed} from '@ember/object';

export default Model.extend({
  name: attr('string'),
  events: hasMany('event'),
  program: belongsTo('program'),
  programVoice: belongsTo('program-voice'),

  title: computed('name', function () {
    return this.name;
  }),

  toString() {
    return this.title;
  }

});
