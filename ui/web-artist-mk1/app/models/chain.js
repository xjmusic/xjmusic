/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */
import Model, {attr, belongsTo, hasMany} from '@ember-data/model';

export default Model.extend({
  name: attr('string'),
  state: attr('string'),
  type: attr('string'),
  startAt: attr('string', {defaultValue: 'now'}),
  stopAt: attr('string', {defaultValue: ''}),
  embedKey: attr('string', {defaultValue: ''}),
  segments: hasMany('segment'),
  account: belongsTo('account'),
  chainConfigs: hasMany('chain-config'),
  chainBindings: hasMany('chain-binding'),
});

