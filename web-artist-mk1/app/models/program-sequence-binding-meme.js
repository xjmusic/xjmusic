/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */
import Model, {attr, belongsTo} from '@ember-data/model';

export default Model.extend({
  name: attr('string'),
  program: belongsTo('program'),
  programSequenceBinding: belongsTo('program-sequence-binding'),
});
