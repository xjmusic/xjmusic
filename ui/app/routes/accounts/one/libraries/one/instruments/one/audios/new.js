// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from "ember";
import EmberUploader from "ember-uploader";
import RSVP from "rsvp";

export default Ember.Route.extend({

  // stub for instantiating an uploader
  uploader: {},

  // Inject: authentication service
  auth: Ember.inject.service(),

  // Inject: configuration service
  config: Ember.inject.service(),

  // Inject: flash message service
  display: Ember.inject.service(),

  // audio base url will be set by promise after config resolves
  audioBaseUrl: '',

  /**
   * Route Model
   * @returns {RSVP.Promise}
   */
  model: function () {
    let self = this;
    let auth = this.get('auth');

    if (auth.isArtist || auth.isAdmin) {
      return new RSVP.Promise((resolve, reject) => {
        Ember.get(this, 'config').promises.audioBaseUrl.then(
          (url) => {
            self.audioBaseUrl = url;
          },
          (error) => {
            reject(error);
          }
        );
        resolve(this.store.createRecord('audio', {
          instrument: this.modelFor('accounts.one.libraries.one.instruments.one')
        }));
      });
    } else {
      this.transitionTo('accounts.one.libraries.one.instruments.one.audios');
    }
  },

  /**
   * Headline
   */
  afterModel() {
    let instrument = this.modelFor('accounts.one.libraries.one.instruments.one');
    Ember.set(this, 'routeHeadline', {
      title: 'New Audio',
      entity: {
        name: 'Instrument',
        id: instrument.get('id')
      }
    });
  },

  /**
   * For Uploading a file
   */
  uploadFiles: [],

  /**
   * Route Actions
   */
  actions: {

    filesChanged: function (files) {
      Ember.set(this, 'uploadFiles', files);
    },

    createAudio(model) {
      let self = this;
      model.save().then(
        () => {
          self.afterSave();
        },
        (error) => {
          Ember.get(this, 'display').error(['Failed to save.', error]);
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

  },

  /**
   * Post- model.save() -hook
   */
  afterSave() {
    let self = this;
    let model = this.controller.get('model');
    let audioId = model.get('id');
    Ember.get(this, 'display').success('Created audio "' + model.get('name') + '".');

    Ember.$.ajax({
      url: '/api/1/audios/' + String(audioId) + '/upload'
    }).then(
      (result) => {
        self.didGetUploadAuthorization(result);
      }, (error) => {
        self.failedToGetUploadAuthorization(error);
      });
  },

  /**
   * we have upload authorization
   * @param result
   */
  didGetUploadAuthorization(result) {
    let self = this;
    let waveformKey = result.audio['waveformKey'];
    let uploadUrl = result.audio['uploadUrl'];
    let uploadPolicy = result.audio['uploadPolicy'];

    // TODO: retrieve REAL upload policy, and use it to upload audio file
    console.log('Retrieved upload policy "' + uploadPolicy + '" for uploading ' + waveformKey + ' to ' + uploadUrl);

    self.uploader = EmberUploader.Uploader.create({
      url: uploadUrl,
      method: 'POST'
    });

    self.uploader.on('progress', e => {
      // TODO: display upload progress in UI
      console.log("Upload progress: " + e.percent);
    });

    let files = Ember.get(self, 'uploadFiles');
    if (!Ember.isEmpty(files)) {
      self.uploadFile(files[0], waveformKey, uploadPolicy);
    }
  },

  /**
   * we failed to get upload authorization
   */
  failedToGetUploadAuthorization(error) {
    this.throwError('Failed to get upload authorization.', error);
  },

  /**
   * upload the file to a 3rd party storage resource (e.g. Amazon S3)
   * @param file
   * @param waveformKey
   * @param uploadPolicy
   */
  uploadFile(file, waveformKey, uploadPolicy) {
    let self = this;
    self.uploader.upload(file, {
      key: waveformKey,
      policy: uploadPolicy
    }).then(() => {
      self.didUploadFile(waveformKey);
    }, (error) => {
      self.failedToUploadFile(error, waveformKey);
    });
  },

  /**
   * we have uploaded the file successfully
   * @param waveformKey
   */
  didUploadFile (waveformKey) {
    Ember.get(this, 'display').success('Uploaded "' + this.audioBaseUrl + waveformKey);
    let newAudio = this.controller.get('model');
    this.transitionTo('accounts.one.libraries.one.instruments.one.audios.one', newAudio);
  },

  /**
   * we failed to upload the audio file
   * @param error
   * @param waveformKey
   */
  failedToUploadFile (error, waveformKey) {
    this.throwError('Failed to upload "' + this.audioBaseUrl + waveformKey, error);
    this.transitionTo('accounts.one.libraries.one.instruments.one.audios');
  },

  /**
   * Log errors in console and flash message
   * @param msg
   * @param error
   */
  throwError(msg, error) {
    Ember.get(this, 'display').error([msg]);
    console.error(msg, error);
  }

});
