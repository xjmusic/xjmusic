// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
(function() {
  /* globals define, wavesurfer */

  function wavesurferJSModule() {
    'use strict';

    return { 'default': WaveSurfer };
  }

  define('WaveSurfer', [], wavesurferJSModule);

})();
