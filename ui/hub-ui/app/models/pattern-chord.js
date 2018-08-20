//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import DS from 'ember-data';

export default DS.Model.extend({
  pattern: DS.belongsTo({}),
  name: DS.attr('string'),
  position: DS.attr('number'),

  getTitle() {
    return this.get("name") + '@' + this.get("position");
  }

});
