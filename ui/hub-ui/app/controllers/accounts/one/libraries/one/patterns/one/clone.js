import {get} from '@ember/object';
import {inject as service} from '@ember/service';
import Controller from '@ember/controller';

export default Controller.extend({
  config: service(),

  actions: {

    setPatternLibrary(library) {
      let model = get(this, 'model.pattern');
      model.set('library', library);
    },

  }

});
