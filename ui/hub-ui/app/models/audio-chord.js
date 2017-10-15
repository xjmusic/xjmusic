// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import DS from 'ember-data';

export default DS.Model.extend({
  audio: DS.belongsTo({}),
  name: DS.attr('string'),
  position: DS.attr('number'),

  getTitle() {
    return this.get("name") + '@' + this.get("position");
  }

});
