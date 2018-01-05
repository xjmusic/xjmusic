import {get} from '@ember/object';
import {inject as service} from '@ember/service';
import Controller from '@ember/controller';

export default Controller.extend({
  config: service(),

  actions: {

    setEventVoice(voice) {
      let model = get(this, 'model.event');
      model.set('voice', voice);
    },

  }

});
