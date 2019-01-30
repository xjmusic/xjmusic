//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import DS from 'ember-data';
import {computed} from '@ember/object';

export default DS.Model.extend({
  audio: DS.belongsTo({}),
  duration: DS.attr('number'),
  inflection: DS.attr('string'),
  note: DS.attr('string'),
  position: DS.attr('number'),
  tonality: DS.attr('number'),
  velocity: DS.attr('number'),

  title: computed('note', 'inflection', 'position', function () {
    return `${this.note}(${this.inflection})@${this.position}`;
  }),

});
