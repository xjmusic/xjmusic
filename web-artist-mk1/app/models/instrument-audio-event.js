/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */
import Model, {attr, belongsTo} from '@ember-data/model';
import {computed} from '@ember/object';

export default Model.extend({
  duration: attr('number'),
  name: attr('string'),
  note: attr('string'),
  position: attr('number'),
  velocity: attr('number'),
  instrument: belongsTo('instrument'),
  instrumentAudio: belongsTo('instrument-audio'),

  title: computed('note', 'name', 'position', function () {
    return `${this.note}(${this.name})@${this.position}`;
  }),

});
