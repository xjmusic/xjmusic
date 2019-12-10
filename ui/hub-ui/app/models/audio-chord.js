// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
import Model, {attr, belongsTo} from '@ember-data/model';
import {computed} from '@ember/object';

export default Model.extend({
  instrument: belongsTo({}),
  audio: belongsTo({}),
  name: attr('string'),
  position: attr('number'),

  title: computed('name', 'position', function () {
    return `${this.name}@${this.position}`;
  }),

});
