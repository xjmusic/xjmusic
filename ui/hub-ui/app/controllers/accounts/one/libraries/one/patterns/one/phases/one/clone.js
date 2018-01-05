import {get} from '@ember/object';
import {inject as service} from '@ember/service';
import Controller from '@ember/controller';

export default Controller.extend({
  config: service(),

  actions: {

    setPhasePattern(pattern) {
      let model = get(this, 'model.phase');
      model.set('pattern', pattern);
      let patternType = pattern.get('type');
      switch (patternType) {
        case 'Macro':
        case 'Main':
          this.send('selectPhaseType', patternType);
          break;

        default:
          this.send('selectPhaseType', 'Loop');
          break;
      }
    },

    selectPhaseType(type) {
      get(this, 'model.phase').set('type', type);
    },

  }

});
