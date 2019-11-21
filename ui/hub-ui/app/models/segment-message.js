//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
import Model, {attr, belongsTo} from '@ember-data/model';
import {computed} from '@ember/object';

export default Model.extend({
  segment: belongsTo({}),
  body: attr('string'),
  type: attr('string'),

  title: computed('type', 'body', function () {
    return `[${this.type}] ${this.body}`;
  }),

});
