// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import DS from 'ember-data';

export default DS.Model.extend({
  density: DS.attr('number'),
  key: DS.attr('string'),
  idea: DS.belongsTo({}),
  name: DS.attr('string'),
  tempo: DS.attr('number'),
  offset: DS.attr('number'),
  total: DS.attr('number'),

  getTitle() {
    let name = this.get("name");
    let title = name ? name : '';
    title += '@' + this.get("offset");
    return title;
  }

});
