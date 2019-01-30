//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import DS from 'ember-data';
import {computed} from '@ember/object';

export default DS.Model.extend({
  density: DS.attr('number'),
  key: DS.attr('string'),
  type: DS.attr('string'),
  sequence: DS.belongsTo({}),
  name: DS.attr('string'),
  tempo: DS.attr('number'),
  total: DS.attr('number'),
  meterSuper: DS.attr('number'),
  meterSub: DS.attr('number'),
  meterSwing: DS.attr('number'),
  events: DS.hasMany('pattern-event'),
  chords: DS.hasMany('pattern-chord'),

  title: computed('name', 'offset', function () {
    let name = this.get("name");
    let title = name ? name : '';
    return `${title}@${this.offset}`;
  }),

  toString() {
    return this.get('title');
  }

});
