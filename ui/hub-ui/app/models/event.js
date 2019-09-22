//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import Model, {attr, belongsTo} from '@ember-data/model';
import {computed} from '@ember/object';

export default Model.extend({
  pattern: belongsTo({}),
  track: belongsTo({}),
  duration: attr('number'),
  note: attr('string'),
  position: attr('number'),
  velocity: attr('number'),

  title: computed('note', 'position', function () {
    return `${this.note}@${this.position}`;
  }),

});
