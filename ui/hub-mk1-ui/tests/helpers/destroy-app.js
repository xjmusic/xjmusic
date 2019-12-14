// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

import {run} from '@ember/runloop';

export default function destroyApp(application) {
  run(application, 'destroy');
}
