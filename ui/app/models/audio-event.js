// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import DS from 'ember-data';

export default DS.Model.extend({
  audio: DS.belongsTo({}),
  duration: DS.attr('number'),
  inflection: DS.attr('string'),
  note: DS.attr('string'),
  position: DS.attr('number'),
  tonality: DS.attr('number'),
  velocity: DS.attr('number'),

  getTitle() {
    return this.get("note") +
      '(' + this.get("inflection") + ')' +
      '@' + this.get("position");
  }

});
