/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

import $ from 'jquery';
import {inject as service} from '@ember/service';
import Component from '@ember/component';

import WaveSurfer from 'WaveSurfer';

const AudioWaveformComponent = Component.extend({
  // Inject: Configuration
  config: service(),
  audioBaseUrl: null,
  wavesurfer: null,

  didRender() {
    let self = this;
    this.config.getConfig().then(
      () => {
        self.set('audioBaseUrl', self.config.audioBaseUrl);
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

        self.get('wavesurfer').on('ready', function () {
          self.onWavesurferReady();
        });
        const waveformUrl = this.audioBaseUrl + self.get("model").get("waveformKey");
        $("#waveform-header").html(`<a href="${waveformUrl}" target="_blank">${waveformUrl}</a>`);
        self.get('wavesurfer').load(waveformUrl);
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
      let zoomRatio = Number(slider.value) / 1000;
      let pxPerSec = vFloor + (vCeiling - vFloor) * zoomRatio;
      Wave.zoom(pxPerSec);
    };

    // Follow button
    var button = document.querySelector('[data-action="follow"]');
    button.addEventListener('click', Wave.playPause.bind(Wave));
  }
});

/**
 * Usage (e.g, in Handlebars, where audio model is "myAudio"):
 *
 *   {{audio-waveform myAudio}}
 */
AudioWaveformComponent.reopenClass(
  {
    positionalParams: ['model']
  });

export default AudioWaveformComponent;
