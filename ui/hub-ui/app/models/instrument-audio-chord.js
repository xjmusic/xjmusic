// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
import Model, {attr, belongsTo} from '@ember-data/model';
import {computed} from '@ember/object';

export default Model.extend({
  name: attr('string'),
  position: attr('number'),
  instrument: belongsTo('instrument'),
  audio: belongsTo('instrument-audio'),

  title: computed('name', 'position', function () {
    return `${this.name}@${this.position}`;
  }),

});
