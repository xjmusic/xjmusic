// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import { inject as service } from '@ember/service';

import Controller from '@ember/controller';

export default Controller.extend({

  /**
   Inject: chain-link player service, implemented directly by the template in order to
   [#279] Now-playing Chain or Link appears highlighted
   */
  player: service(),

});
