//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
import Model, {attr, belongsTo} from '@ember-data/model';
import {computed} from '@ember/object';

export default Model.extend({
  instrument: belongsTo({}),
  audio: belongsTo({}),
  duration: attr('number'),
  name: attr('string'),
  note: attr('string'),
  position: attr('number'),
  velocity: attr('number'),

  title: computed('note', 'name', 'position', function () {
    return `${this.note}(${this.name})@${this.position}`;
  }),

});
