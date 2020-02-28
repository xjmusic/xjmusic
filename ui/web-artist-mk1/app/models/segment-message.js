/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */
import Model, {attr, belongsTo} from '@ember-data/model';
import {computed} from '@ember/object';

export default Model.extend({
  body: attr('string'),
  type: attr('string'),
  segment: belongsTo('segment'),

  title: computed('type', 'body', function () {
    return `[${this.type}] ${this.body}`;
  }),

});
