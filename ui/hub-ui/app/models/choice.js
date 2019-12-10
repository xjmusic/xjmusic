// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
import Model, {attr, belongsTo, hasMany} from '@ember-data/model';

export default Model.extend({
  segment: belongsTo({}),
  sequenceBinding: belongsTo({}),
  transpose: attr('number'),
  type: attr('string'),
  program: belongsTo({}),
  arrangements: hasMany('arrangement'),
});










