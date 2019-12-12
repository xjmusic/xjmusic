// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
import Model, {attr, hasMany} from '@ember-data/model';

export default Model.extend({
  name: attr('string'),
  users: hasMany('account-user'),
  libraries: hasMany('library'),
  chains: hasMany('chain'),
});
