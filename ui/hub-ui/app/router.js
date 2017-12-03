// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import EmberRouter from '@ember/routing/router';
import config from "./config/environment";
import googlePageView from "./mixins/google-pageview";

const Router = EmberRouter.extend(googlePageView, {
  location: config.locationType
});

Router.map(function () {
  this.route('users', {path: '/u'}, users);
  this.route('accounts', {path: '/a'}, accounts);
  this.route('login');
  this.route('logout');
  this.route('unauthorized');
  this.route('platform', platform);
});

function users() {
  this.route('one', {path: '/:user_id'});
}

function accounts() {
  this.route('new'); // New Account
  this.route('one', {path: '/:account_id'}, account); // One Account
}

function platform() {
  this.route('messages', function() {
    this.route('new');
  });
  this.route('works');
}

function account() {
  this.route('edit'); // Edit Account
  this.route('users'); // Users in Account
  this.route('libraries', {path: '/lib'}, accountLibraries); // Libraries in Account
  this.route('chains', accountChains); // Chains in Account
}

function accountLibraries() {
  this.route('new'); // New Library
  this.route('one', {path: '/:library_id'}, accountLibrary); // One Library
}

function accountLibrary() {
  this.route('edit'); // Edit Library
  this.route('patterns', accountLibraryPatterns); // Patterns in Library
  this.route('instruments', accountLibraryInstruments); // Instruments in Library
}

function accountLibraryPatterns() {
  this.route('new'); // New Pattern
  this.route('one', {path: '/:pattern_id'}, accountLibraryPattern); // One Pattern
}

function accountLibraryPattern() {
  this.route('edit'); // Edit Pattern
  this.route('memes'); // Memes in Pattern
  this.route('phases', accountLibraryPatternPhases); // Phases in Pattern
}

function accountLibraryPatternPhases() {
  this.route('new'); // New Phase
  this.route('one', {path: '/:phase_id'}, accountLibraryPatternPhase); // One Phase
}

function accountLibraryPatternPhase() {
  this.route('edit'); // Edit Phase
  this.route('memes'); // Memes in Phase
  this.route('chords', accountLibraryPatternPhaseChords); // Chords in Phase
  this.route('voices', accountLibraryPatternPhaseVoices); // Voices in Phase
}

function accountLibraryPatternPhaseChords() {
  this.route('new'); // New Chord
  this.route('one', {path: '/:chord_id'}, accountLibraryPatternPhaseChord); // One Chord
}

function accountLibraryPatternPhaseChord() {
  this.route('edit'); // Edit Phase
}

function accountLibraryPatternPhaseVoices() {
  this.route('new'); // New Voice
  this.route('one', {path: '/:voice_id'}, accountLibraryPatternPhaseVoice); // One Voice
}

function accountLibraryPatternPhaseVoice() {
  this.route('edit'); // Edit Phase
  this.route('events', accountLibraryPatternPhaseVoiceEvents); // Events in Voice
}

function accountLibraryPatternPhaseVoiceEvents() {
  this.route('new'); // New Event
  this.route('one', {path: '/:event_id'}, accountLibraryPatternPhaseVoiceEvent); // One Event
}

function accountLibraryPatternPhaseVoiceEvent() {
  this.route('edit'); // Edit Event
}

function accountLibraryInstruments() {
  this.route('new'); // New Instrument
  this.route('one', {path: '/:instrument_id'}, accountLibraryInstrument); // One Instrument
}

function accountLibraryInstrument() {
  this.route('edit'); // Edit Instrument
  this.route('memes'); // Memes in Instrument
  this.route('audios', accountLibraryInstrumentAudios);
}

function accountLibraryInstrumentAudios() {
  this.route('new'); // New Audio
  this.route('one', {path: '/:audio_id'}, accountLibraryInstrumentAudio); // One Audio
}

function accountLibraryInstrumentAudio() {
  this.route('edit'); // Edit Audio
  this.route('events', accountLibraryInstrumentAudioEvents); // Events in Audio
  this.route('chords', accountLibraryInstrumentAudioChords); // Chords in Audio
}

function accountLibraryInstrumentAudioEvents() {
  this.route('new'); // New Event
  this.route('one', {path: '/:event_id'}, accountLibraryInstrumentAudioEvent); // One Event
}

function accountLibraryInstrumentAudioEvent() {
  this.route('edit'); // Edit Event
}

function accountLibraryInstrumentAudioChords() {
  this.route('new'); // New Chord
  this.route('one', {path: '/:chord_id'}, accountLibraryInstrumentAudioChord); // One Chord
}

function accountLibraryInstrumentAudioChord() {
  this.route('edit'); // Edit Chord
}

function accountChains() {
  this.route('new'); // New Chain
  this.route('one', {path: '/:chain_id'}, accountChain); // One Chain
}

function accountChain() {
  this.route('edit'); // Edit Chain
  this.route('libraries'); // Libraries in Chain
  this.route('configs'); // Configs in Chain
  this.route('patterns'); // Patterns in Chain
  this.route('instruments'); // instruments in Chain
  this.route('links', accountChainLinks); // Links in Chain
}

function accountChainLinks() {
  this.route('one', {path: '/:link_id'}, accountChainLink); // One Link
}

function accountChainLink() {
}

export default Router;
