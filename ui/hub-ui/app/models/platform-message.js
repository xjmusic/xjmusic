//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
import Model, {attr} from '@ember-data/model';
import {computed} from '@ember/object';

export default Model.extend({
  body: attr('string'),
  type: attr('string'),
  createdAt: attr('string'),
  updatedAt: attr('string'),

  title: computed('type', 'body', function () {
    return `[${this.type}] ${this.body}`;
  }),

});
