//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

import DS from 'ember-data';

export default DS.RESTSerializer.extend({
  attrs: {
    name: { serialize: false },
    email: { serialize: false },
    avatarUrl: { serialize: false }
  }
});
