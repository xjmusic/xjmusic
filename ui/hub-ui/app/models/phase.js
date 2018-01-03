// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
import DS from 'ember-data';

export default DS.Model.extend({
  density: DS.attr('number'),
  key: DS.attr('string'),
  pattern: DS.belongsTo({}),
  name: DS.attr('string'),
  tempo: DS.attr('number'),
  offset: DS.attr('number'),
  total: DS.attr('number'),
  events: DS.hasMany('voice-event'),

  getTitle() {
    let name = this.get("name");
    let title = name ? name : '';
    title += '@' + this.get("offset");
    return title;
  },

  toString() {
    return this.getTitle();
  }

});
