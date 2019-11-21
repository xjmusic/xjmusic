//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
(function () {
  /* globals define, wavesurfer */

  function wavesurferJSModule() {
    'use strict';

    return {'default': WaveSurfer};
  }

  define('WaveSurfer', [], wavesurferJSModule);

})();
