// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import DS from 'ember-data';

export default DS.Model.extend({
  chain: DS.belongsTo({}),
  library: DS.belongsTo({})
});
