// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Route.extend({

  // Inject: authentication service
  auth: Ember.inject.service(),

  // Inject: flash message service
  display: Ember.inject.service(),

  /**
   * Route Model
   * @returns {*|DS.Model|Promise}
   */
  model() {
    let auth = this.get('auth');
    if (auth.isArtist || auth.isAdmin) {
      let voice = this.modelFor('accounts.one.libraries.one.ideas.one.phases.one.voices.one');
      let event = this.modelFor('accounts.one.libraries.one.ideas.one.phases.one.voices.one.events.one');
      event.set('voice', voice);
      return event;
    } else {
      this.transitionTo('accounts.one.libraries.one.ideas.one.phases.one.voices.one.events');
    }
  },

  /**
   * Headline
   */
  afterModel(model) {
    Ember.set(this, 'routeHeadline', {
      title: 'Edit ' + model.getTitle(),
      entity: {
        name: 'Event',
        id: model.get('id')
      }
    });
  },

  /**
   * Route Actions
   */
  actions: {

    saveEvent(model) {
      model.save().then(
        () => {
          Ember.get(this, 'display').success('Updated event "' + model.get('inflection') + '" event in ' + model.get('note') + '.');
          this.transitionTo('accounts.one.libraries.one.ideas.one.phases.one.voices.one.events');
        },
        (error) => {
          Ember.get(this, 'display').error(error);
        });
    },

    destroyEvent(model) {
      model.destroyRecord({}).then(
        () => {
          Ember.get(this, 'display').success('Deleted event "' + model.get('inflection') + '" event in ' + model.get('note') + '.');
          this.transitionTo('accounts.one.libraries.one.ideas.one.phases.one.voices.one.events');
        },
        (error) => {
          Ember.get(this, 'display').error(error);
        });
    },

    willTransition(transition) {
      let model = this.controller.get('model');
      if (model.get('hasDirtyAttributes')) {
        let confirmation = confirm("Your changes haven't saved yet. Would you like to leave this form?");
        if (confirmation) {
          model.rollbackAttributes();
        } else {
          transition.abort();
        }
      }
    }

  }

});
