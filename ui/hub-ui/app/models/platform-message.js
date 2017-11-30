// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import DS from "ember-data";

export default DS.Model.extend({
  body: DS.attr('string'),
  type: DS.attr('string'),
  createdAt: DS.attr('string'),
  updatedAt: DS.attr('string'),

  getTitle() {
    return '[' + this.get("type") + '] ' + this.get("body");
  }

});
