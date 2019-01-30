//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

import EmberRouter from '@ember/routing/router';
import config from "./config/environment";
import googlePageView from "./mixins/google-pageview";

const Router = EmberRouter.extend(googlePageView, {
  location: config.locationType
});

Router.map(function () {
  this.route('users', {path: '/u'}, userIndex);
  this.route('accounts', {path: '/a'}, accountIndex);
  this.route('login');
  this.route('logout');
  this.route('unauthorized');
  this.route('platform', platform);
  this.route('go', go);
});

function userIndex() {
  this.route('one', {path: '/:user_id'});
}

function accountIndex() {
  this.route('new');
  this.route('one', {path: '/:account_id'}, accountOne);
}

function accountOne() {
  this.route('edit');
  this.route('destroy');
  this.route('users');
  this.route('libraries', {path: '/lib'}, accountLibraryIndex);
  this.route('chains', accountChainIndex);
}

function accountLibraryIndex() {
  this.route('new');
  this.route('one', {path: '/:library_id'}, accountLibraryOne);
}

function accountLibraryOne() {
  this.route('edit');
  this.route('destroy');
  this.route('sequences', accountLibrarySequenceIndex);
  this.route('instruments', accountLibraryInstrumentIndex);
  this.route('digest');
}

function accountLibrarySequenceIndex() {
  this.route('new');
  this.route('one', {path: '/:sequence_id'}, accountLibrarySequenceOne);
}

function accountLibrarySequenceOne() {
  this.route('edit');
  this.route('clone');
  this.route('destroy');
  this.route('memes');
  this.route('patterns', accountLibrarySequencePatternIndex);
  this.route('voices', accountLibrarySequenceVoiceIndex);
}

function accountLibrarySequenceVoiceIndex() {
  this.route('new');
  this.route('one', {path: '/:voice_id'}, accountLibrarySequenceVoiceOne);
}

function accountLibrarySequenceVoiceOne() {
  this.route('edit');
  this.route('destroy');
}

function accountLibrarySequencePatternIndex() {
  this.route('new');
  this.route('one', {path: '/:pattern_id'}, accountLibrarySequencePatternOne);
}

function accountLibrarySequencePatternOne() {
  this.route('edit');
  this.route('clone');
  this.route('stepmatic');
  this.route('destroy');
  this.route('memes');
  this.route('chords', accountLibrarySequencePatternChordIndex);
  this.route('events', accountLibrarySequencePatternEventIndex);
}

function accountLibrarySequencePatternChordIndex() {
  this.route('new');
  this.route('one', {path: '/:chord_id'}, accountLibrarySequencePatternChordOne);
}

function accountLibrarySequencePatternChordOne() {
  this.route('edit');
  this.route('destroy');
}

function accountLibrarySequencePatternEventIndex() {
  this.route('new');
  this.route('one', {path: '/:event_id'}, accountLibrarySequencePatternEventOne);
}

function accountLibrarySequencePatternEventOne() {
  this.route('edit');
  this.route('clone');
  this.route('destroy');
}

function accountLibraryInstrumentIndex() {
  this.route('new');
  this.route('one', {path: '/:instrument_id'}, accountLibraryInstrumentOne);
}

function accountLibraryInstrumentOne() {
  this.route('edit');
  this.route('clone');
  this.route('destroy');
  this.route('memes');
  this.route('audios', accountLibraryInstrumentAudioIndex);
}

function accountLibraryInstrumentAudioIndex() {
  this.route('new');
  this.route('one', {path: '/:audio_id'}, accountLibraryInstrumentAudioOne);
}

function accountLibraryInstrumentAudioOne() {
  this.route('edit');
  this.route('clone');
  this.route('move');
  this.route('destroy');
  this.route('events', accountLibraryInstrumentAudioEventIndex);
  this.route('chords', accountLibraryInstrumentAudioChordIndex);
}

function accountLibraryInstrumentAudioEventIndex() {
  this.route('new');
  this.route('one', {path: '/:event_id'}, accountLibraryInstrumentAudioEventOne);
}

function accountLibraryInstrumentAudioEventOne() {
  this.route('edit');
  this.route('destroy');
}

function accountLibraryInstrumentAudioChordIndex() {
  this.route('new');
  this.route('one', {path: '/:chord_id'}, accountLibraryInstrumentAudioChordOne);
}

function accountLibraryInstrumentAudioChordOne() {
  this.route('edit');
  this.route('destroy');
}

function accountChainIndex() {
  this.route('new');
  this.route('one', {path: '/:chain_id'}, accountChainOne);
}

function accountChainOne() {
  this.route('edit');
  this.route('destroy');
  this.route('libraries');
  this.route('configs');
  this.route('sequences');
  this.route('instruments');
  this.route('segments', accountChainSegmentIndex);
}

function accountChainSegmentIndex() {
  this.route('one', {path: '/:segment_id'}, accountChainSegmentOne);
}

function accountChainSegmentOne() {
}

function platform() {
  this.route('messages', function () {
    this.route('new');
  });
  this.route('works');
}

function go() {
  this.route('audio', {path: '/audio/:audio_id'});
  this.route('pattern', {path: '/pattern/:pattern_id'});
  this.route('sequence', {path: '/sequence/:sequence_id'});
  this.route('instrument', {path: '/instrument/:instrument_id'});
}

export default Router;
