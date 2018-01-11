// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
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
