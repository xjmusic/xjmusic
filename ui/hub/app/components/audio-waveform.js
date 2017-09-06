import Ember from 'ember';

import WaveSurfer from 'WaveSurfer';

const AudioWaveformComponent = Ember.Component.extend(
{
  // Inject: Configuration
  config: Ember.inject.service(),
  audioBaseUrl: null,
  wavesurfer: null,

  didRender()
  {
    let self = this;
    Ember.get(self, 'config').promises.config.then
    (
      (config) =>
      {
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
        self.get('wavesurfer').on('ready', function()
        {
          self.onWavesurferReady();
        });
        self.get('wavesurfer').load(waveformURL);
      },
      (error) =>
      {
        console.error('Failed to load config', error);
      }
    );
  },

  onWavesurferReady()
  {
    let self = this;
    let Wave = self.get('wavesurfer');

    // Zoom slider
    let slider = document.querySelector('#slider');

    slider.oninput = function ()
    {
      let width = self.$().width();
      let zoomRatio = Number(slider.value);
      let pxPerSec = (zoomRatio * ( width / Wave.getDuration() )) / 100;
      Wave.zoom(pxPerSec);
    };

    // Play button
    var button = document.querySelector('[data-action="play"]');
    button.addEventListener('click', Wave.playPause.bind(Wave));
  }
});

/**
 * Usage (e.g, in Handlebars, where link model is "myLinkModel"):
 *
 *   {{link-choices myLinkModel}}
 */
AudioWaveformComponent.reopenClass(
{
  positionalParams: ['model']
});

export default AudioWaveformComponent;
