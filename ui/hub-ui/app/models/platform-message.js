//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import DS from "ember-data";
import {computed} from '@ember/object';

export default DS.Model.extend({
  body: DS.attr('string'),
  type: DS.attr('string'),
  createdAt: DS.attr('string'),
  updatedAt: DS.attr('string'),

  title: computed('type', 'body', function () {
    return `[${this.type}] ${this.body}`;
  }),

});
