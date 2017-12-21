import {get} from '@ember/object';
import {inject as service} from '@ember/service';
import Controller from '@ember/controller';

export default Controller.extend({
  config: service(),

  actions: {

    setAudioInstrument(instrument) {
      let model = get(this, 'model.audio');
      model.set('instrument', instrument);
    },

  }

});
