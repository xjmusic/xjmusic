// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
import Model, {attr, belongsTo} from '@ember-data/model';

export default Model.extend({
  chain: belongsTo({}),
  type: attr('string'),
  targetId: attr('string')
});
