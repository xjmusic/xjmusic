import Ember from 'ember';

export default Ember.Controller.extend({

  display: Ember.inject.service(),

  actions: {

    /**
     * Ember power-select uses this as onChange to set value
     * @param library
     * @returns {*}
     */
    setLibraryToAdd(library){
      this.set('model.libraryToAdd', library);
      return library;
    },

    destroyChainLibrary(model) {
      model.destroyRecord().then(() => {
        Ember.get(this, 'display').success('Removed Library from Chain.');
      }).catch((error) => {
        Ember.get(this, 'display').error(error);
      });
    },

    addLibraryToChain(model) {
      let chainLibrary = this.store.createRecord('chain-library', {
        chain: model.chain,
        library: model.libraryToAdd,
      });
      chainLibrary.save().then(() => {
        Ember.get(this, 'display').success('Added ' + model.libraryToAdd.get('name') + ' to ' + model.chain.get('name') + '.');
        // this.transitionToRoute('chains.one.libraries',model.chain);
        this.send("sessionChanged");
      }).catch((error) => {
        Ember.get(this, 'display').error(error);
      });
    },

  }

});
