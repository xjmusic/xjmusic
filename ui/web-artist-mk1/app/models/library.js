/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */
import Model, {attr, belongsTo, hasMany} from '@ember-data/model';

export default Model.extend({
  name: attr('string'),
  account: belongsTo('account'),
  programs: hasMany('program'),
  instruments: hasMany('instrument'),
});

