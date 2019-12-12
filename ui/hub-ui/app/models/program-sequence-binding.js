// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
import Model, {attr, belongsTo, hasMany} from '@ember-data/model';

export default Model.extend({
  offset: attr('number'),
  program: belongsTo('program'),
  programSequence: belongsTo('program-sequence'),
  programSequenceBindingMemes: hasMany('program-sequence-binding-meme'),
});
