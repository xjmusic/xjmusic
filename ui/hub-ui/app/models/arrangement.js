// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
import Model, {belongsTo} from '@ember-data/model';

export default Model.extend({
  segment: belongsTo({}),
  choice: belongsTo({}),
  voice: belongsTo({}),
  instrument: belongsTo({}),
});










