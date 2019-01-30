//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import DS from 'ember-data';
import {computed} from '@ember/object';

export default DS.Model.extend({
  audio: DS.belongsTo({}),
  name: DS.attr('string'),
  position: DS.attr('number'),

  title: computed('name', 'position', function () {
    return `${this.name}@${this.position}`;
  }),

});
