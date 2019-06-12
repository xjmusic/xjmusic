//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import Model, {attr, belongsTo, hasMany} from '@ember-data/model';

export default Model.extend({
  program: belongsTo({}),
  sequence: belongsTo({}),
  offset: attr('number'),
  memes: hasMany('sequence-binding-meme'),
});
