// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

import {get} from '@ember/object';
import {inject as service} from '@ember/service';
import Component from '@ember/component';

import WaveSurfer from 'WaveSurfer';

const AudioWaveformComponent = Component.extend(
  {
    // Inject: Configuration
    config: service(),
    audioBaseUrl: null,
    wavesurfer: null,

    didRender() {
      let self = this;
      get(self, 'config').promises.config.then
      (
        (config) => {
          self.set('audioBaseUrl', config.audioBaseUrl);
          //Create and initialize instance of wavesurfer
          self.set('wavesurfer', WaveSurfer.create(
            {
              container: '#waveform',
              scrollParent: true,
              waveColor: 'violet',
              progressColor: 'purple',
              normalize: true,
              //Waveforms of any number of channels (e.g. Stereo) will be displayed as 1-channel (Mono)
              splitChannels: false
            }));

          let waveformURL = this.get("audioBaseUrl") + self.get("model").get("waveformKey");
          self.get('wavesurfer').on('ready', function () {
            self.onWavesurferReady();
          });
          self.get('wavesurfer').load(waveformURL);
        },
        (error) => {
          console.error('Failed to load config', error);
        }
      );
    },

    onWavesurferReady() {
      let self = this;
      let Wave = self.get('wavesurfer');

      // Zoom slider
      let slider = document.querySelector('#slider');

      // store floor and ceiling of pxPerSec value
      let sampleRate = self.get('wavesurfer.backend.buffer.sampleRate');
      let vCeiling = sampleRate * 0.66;
      let vFloor = self.get('wavesurfer.params.minPxPerSec');

      slider.oninput = function () {
        // let width = self.$().width();
        let zoomRatio = Number(slider.value) / 1000;
        let pxPerSec = vFloor + (vCeiling - vFloor) * zoomRatio;
        Wave.zoom(pxPerSec);
      };

      // Play button
      var button = document.querySelector('[data-action="play"]');
      button.addEventListener('click', Wave.playPause.bind(Wave));
    }
  });

/**
 * Usage (e.g, in Handlebars, where segment model is "mySegmentModel"):
 *
 *   {{segment-choices mySegmentModel}}
 */
AudioWaveformComponent.reopenClass(
  {
    positionalParams: ['model']
  });

export default AudioWaveformComponent;
