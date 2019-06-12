//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import Model, {attr} from '@ember-data/model';

export default Model.extend({
  name: attr('string'),
  avatarUrl: attr('string'),
  email: attr('string'),
  roles: attr('string')
});
