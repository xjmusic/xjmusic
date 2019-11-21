//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
import Model, {attr, belongsTo} from '@ember-data/model';

export default Model.extend({
  chain: belongsTo({}),
  targetClass: attr('string'),
  targetId: attr('number')
});
